import com.visualthreat.api.API;
import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.*;
import com.visualthreat.api.v1.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.*;

@Slf4j
public class ScanECUs {
  private static final Options options = new Options();
  private static final int MIN_ID = 0x600;
  private static final int MAX_ID = 0x7ff;
  private static final byte[] ECU_PAYLOAD = {0x02, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
  private static final List<Byte> BYTE_ONE = Arrays.asList((byte) 0x50, (byte) 0x7f);
  private static final long WAIT_TIME = 10;

  static {
    options.addOption(Arguments.DEVICE.type, "device", true, "Device ID");
    options.addOption(Arguments.KEY.type, "key", true, "API key");
    options.addOption(Arguments.SECRET.type, "secret", true, "Secret");
  }

  public static void main(String[] args) throws Exception {
    // parsing CLI arguments
    final CommandLineParser parser = new DefaultParser();
    final CommandLine cmd = parser.parse(options, args);
    if (cmd.hasOption(Arguments.KEY.type) &&
        cmd.hasOption(Arguments.SECRET.type)) {
      final String key = cmd.getOptionValue(Arguments.KEY.type);
      final String secret = cmd.getOptionValue(Arguments.SECRET.type);
      final String deviceId = cmd.hasOption(Arguments.DEVICE.type) ?
          cmd.getOptionValue(Arguments.DEVICE.type) : null;
      // Starting real work
      start(key, secret, deviceId);
    } else {
      showUsage();
    }
  }

  private static void showUsage() {
    log.error("Usage: -k <key> -s <secret> [-d <\"device id\">]");
    System.exit(1);
  }

  private static void start(String key, String secret, String deviceId) {
    // create API object
    final API api = API.get();

    // authenticate
    final Token token = api.authenticate(key, secret);
    Device device = null;
    for (final Device d : api.getConnectedDevices(token)) {
      if (d.isAvailable() && (deviceId == null || d.getDeviceId().equalsIgnoreCase(deviceId))) {
        device = d;
        break;
      }
    }

    // connect to found device and get VTCloud object
    try (final VTCloud cloud = api.connectToDevice(device, token)) {
      log.info("Connected to {}", device.getName());

      final Collection<String> ecuIDs = new HashSet<>();
      final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

      final Collection<Request> requests = new ArrayList<>();
      for (int i = MIN_ID; i <= MAX_ID; i++) {
        requests.add(Request.Builder.newBuilder()
            .id(i)
            .data(ECU_PAYLOAD)
            .waitTime(WAIT_TIME)
            .build()
        );
      }

      final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
      while (responses.hasNext()) {
        final Response response = responses.next();
        logRequestFrame(response.getRequest());
        final Iterator<CANFrame> frames = response.getResponses();
        while (frames.hasNext()) {
          final CANFrame frame = frames.next();
          logResponseFrame(frame);
          if (BYTE_ONE.contains(frame.getData()[1])) {
            ecuIDs.add(String.format("\t0x%x -> 0x%x", response.getRequest().getId(), frame.getId()));
          }
        }
      }

      log.info("Found {} ECU ID pairs", ecuIDs.size());
      for (final String s : ecuIDs) {
        log.info(s);
      }
    } catch (final Exception e) {
      log.error("Critical Exception", e);
    }
  }

  private static void logRequestFrame(final CANFrame frame) {
    log.info("===>{}", frame);
  }

  private static void logResponseFrame(final CANFrame frame) {
    log.info("<==={}", frame);
  }
}
