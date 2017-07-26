import com.visualthreat.api.API;
import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.*;
import com.visualthreat.api.v1.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public final class Fuzzing {
  // CLI arguments parsing init
  private static final Options options = new Options();
  private static final Random rnd = new SecureRandom();
  private static final List<CANFrame> frames = new LinkedList<>();
  private static final long WAIT_TIME = 100;
  // Amount of requests to send
  private static final int maxCount = 1000;
  private static VTCloud cloud = null;
  private static int count = 0;

  static {
    options.addOption(Arguments.DEVICE.type, "device", true, "Device ID");
    options.addOption(Arguments.KEY.type, "key", true, "API key");
    options.addOption(Arguments.SECRET.type, "secret", true, "Secret");
  }

  private static void showUsage() {
    log.error("Usage: -k <key> -s <secret> [-d <\"device id\">]");
    die();
  }

  private static void die() {
    if (cloud != null) {
      cloud.close();
    }
    System.exit(3);
  }

  public static void main(final String[] args) throws Exception {
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

  /**
   * Generating random Request, with random data, data length and random id.
   */
  private static Request generateRequest() {
    final int frameId = rnd.nextInt(0x800);
    final int dataLength = rnd.nextInt(8) + 1;
    final byte[] frameData = new byte[dataLength];
    rnd.nextBytes(frameData);
    return new Request(new CANFrame(frameId, frameData), WAIT_TIME);
  }

  private static void makeRequest(final VTCloud cloud) {
    log.info("Iteration # " + count);
    try {
      // Send requests to device and wait for a response
      final Collection<Request> requests = IntStream.range(0, 100)
          .mapToObj(i -> generateRequest())
          .collect(Collectors.toList());
      final Iterator<Response> responses = cloud.sendCANFrames(
          Collections.singletonList(generateRequest()), CANResponseFilter.filterIds(0, 0x300));

      while (responses.hasNext()) {
        final Response response = responses.next();
        handleResponse(response.getResponses());
      }
    } catch (final Exception e) {
      log.error("Something went wrong", e);
      die();
    }
  }

  /**
   * Counting simple stats from all responses.
   * Count unique ids, and count of concrete id.
   */
  private static void handleData(final List<CANFrame> canFrames) {
    final Map<Integer, Long> ids = canFrames.stream().collect(
        Collectors.groupingBy(CANFrame::getId, Collectors.counting()));
    final int uniqueKeys = ids.size();
    log.info("Amount of unique keys: " + uniqueKeys);
    ids.forEach((id, count) ->
        log.info("ID 0x" + Integer.toHexString(id) + ": " + count + " responses"));
  }

  private static void handleResponse(Iterator<CANFrame> responses) {
    while (responses.hasNext()) {
      frames.add(responses.next());
    }
  }

  private static void runFuzzing(final Device device, final API api, final Token token) {
    // open a connection to cloud
    cloud = api.connectToDevice(device, token);
    for (count = 0; count < maxCount; count++) {
      makeRequest(cloud);
    }
    // handle collected frames to handler
    handleData(frames);

    cloud.close();
  }

  private static void start(final String key, final String secret, final String deviceId) {
    // Create API connection class
    final API api = API.get();
    // Get authentication token
    final Token token = api.authenticate(key, secret);
    if (token != null) {
      // Get devices list available for this token
      final Collection<Device> devices = api.getConnectedDevices(token);

      Device device = null;
      for (final Device d : devices) {
        if (deviceId == null ||
            (d.isAvailable() && d.getDeviceId().equalsIgnoreCase(deviceId))) {
          device = d;
          break;
        }
      }

      if (device != null) {
        log.info("Using device: {}", device.getName());
        runFuzzing(device, api, token);
      } else {
        log.error("Couldn't find a device, please use correct device id and/or reserve it");
        die();
      }
    } else {
      log.error("Couldn't authenticate in VT cloud");
    }
  }
}
