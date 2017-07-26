package com.visualthreat.api.v1;

import com.visualthreat.api.API;
import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.Device;
import com.visualthreat.api.exception.APIAuthException;
import com.visualthreat.api.exception.APIException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.ext.client.java8.SessionBuilder;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static com.visualthreat.api.v1.Utils.decodeJson;

@Slf4j
@RequiredArgsConstructor
public class APIImpl implements API {
  private static final Collection<Integer> SUCCESS_CODES = Arrays.asList(200, 204);
  private static final String AUTH_HEADER = "Authorization";
  private static final String AUTH = "/auth/realms/Services/protocol/openid-connect/token";
  private static final String GET_DEVICES = "/service/autox/api/remote/devices/reserved";
  private static final String CONNECT_TO = "/public/notify/device/";
  private static final int MAX_MESSAGE_SIZE = 2 * 1024 * 1024;

  private final String hostname;
  private final int port;

  private static String buildUrl(final String secure, final String host, final int port, final String path) {
    return secure + host + ":" + port + path;
  }

  @Override
  public Token authenticate(final String apiKey, final String secret) throws APIAuthException {
    final HttpURLConnection con = postRequest(AUTH, buildAuthCredentials(apiKey, secret));
    if (con != null) {
      final String response = readResponse(con);
      if (response != null) {
        return Token.parse(response);
      }
    }

    throw new APIAuthException("Invalid key and secret, can't authenticate");
  }

  @Override
  public Collection<Device> getConnectedDevices(final Token token) throws APIException {
    final HttpURLConnection con = getRequest(GET_DEVICES);
    if (con != null) {
      final String response = readResponse(addToken(con, token));
      if (response != null) {
        final Device[] devices = decodeJson(response, Device[].class);
        if (devices != null) {
          return Arrays.asList(devices);
        }
      }
    }

    return Collections.emptyList();
  }

  @Override
  public VTCloud connectToDevice(final Device device, final Token token) throws APIException {
    try {

      final Session wsSession = getSession(device, token);
      return new VTCloudImpl(wsSession);
    } catch (final IOException | DeploymentException e) {
      log.error("Can't connect to a device. Check that you reserved it.", e);
      throw new APIException(e);
    }
  }

  private Session getSession(final Device device, final Token token) throws IOException, DeploymentException {
    final String url = buildUrl("wss://", hostname, port, CONNECT_TO) + device.getDeviceId();

    final ClientManager client = ClientManager.createClient();
    final SSLContextConfigurator defaultConfig = new SSLContextConfigurator();
    defaultConfig.retrieve(System.getProperties());
    final SSLEngineConfigurator sslEngineConfigurator =
        new SSLEngineConfigurator(defaultConfig, true, false, false);
    client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
    client.getProperties().put(ClientProperties.REDIRECT_ENABLED, Boolean.TRUE);
    final ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {
      @Override
      public boolean onDisconnect(CloseReason reason) {
        log.trace("Disconnected from VT Cloud");
        return false;
      }
    };
    client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
    client.setDefaultMaxBinaryMessageBufferSize(MAX_MESSAGE_SIZE);
    client.setDefaultMaxTextMessageBufferSize(MAX_MESSAGE_SIZE);

    final Session session = new SessionBuilder(client)
        .clientEndpointConfig(ClientEndpointConfig.Builder.create()
            .configurator(new ClientEndpointConfig.Configurator() {
              @Override
              public void beforeRequest(final Map<String, List<String>> headers) {
                headers.put(AUTH_HEADER, Collections.singletonList(token.getSecret()));
                headers.put("Origin", Collections.singletonList("wss://" + hostname));
              }
            })
            .build())
        .uri(URI.create(url))
        .connect();
    log.trace("Connected to VT Cloud");
    return session;
  }

  private HttpURLConnection getRequest(final String request) {
    return request(request, "GET");
  }

  private HttpURLConnection postRequest(final String request, final String data) {
    final HttpURLConnection con = request(request, "POST");
    if (con != null) {
      con.setDoOutput(true);
      try (final DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
        wr.writeBytes(data);
        wr.flush();
        return con;
      } catch (final IOException e) {
        log.error("Can't post request to: " + request, e);
      }
    }

    return null;
  }

  private HttpURLConnection request(final String request, final String method) {
    try {
      final URL url = new URL(buildUrl("https://", hostname, 443, request));

      final HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod(method);
      con.setRequestProperty("Origin", buildUrl("https://", hostname, 443, ""));
      return con;
    } catch (final IOException e) {
      log.error("Can't make request to: " + hostname, e);
    }

    return null;
  }

  private String readResponse(final HttpURLConnection con) {
    try {
      final int responseCode = con.getResponseCode();

      if (SUCCESS_CODES.contains(responseCode)) {
        final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        final StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }

        return response.toString();
      } else {
        log.warn("Incorrect response code for {}: {}", con.getURL(), responseCode);
      }
    } catch (final IOException e) {
      log.error("Can't read http response", e);
    }

    return null;
  }

  private String buildAuthCredentials(final String apiKey, final String secret) {
    return String.format("grant_type=key&username=%s&password=%s", apiKey, secret);
  }

  private HttpURLConnection addToken(final HttpURLConnection con, final Token token) {
    con.setRequestProperty(AUTH_HEADER, token.getSecret());
    return con;
  }
}
