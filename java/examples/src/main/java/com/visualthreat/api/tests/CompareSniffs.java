package com.visualthreat.api.tests;

import com.visualthreat.api.API;
import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Device;
import com.visualthreat.api.v1.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class CompareSniffs {
  private static final int SNIFF_LENGTH = 10000; // in milliseconds
  private static final Options options = new Options();

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
      log.info("First Sniff. {} seconds...", SNIFF_LENGTH / 1000);
      final Set<String> uniqueIDs1 = collectSniffIDs(cloud);
      log.info("Prepare to do an action your want to reverse and press any key");
      System.in.read();
      log.info("Second Sniff. {} seconds...", SNIFF_LENGTH / 1000);
      final Set<String> uniqueIDs2 = collectSniffIDs(cloud);
      log.info("Done");
      final Set<String> newIDs = diff(uniqueIDs1, uniqueIDs2);
      log.info("New IDs in Second Sniff: {}", newIDs);
    } catch (final Exception e) {
      log.error("Critical Exception", e);
    }
  }

  private static Set<String> collectSniffIDs(final VTCloud cloud) {
    final Set<String> uniqueIDs = new HashSet<>();
    final Iterator<CANFrame> frames = cloud.sniff(SNIFF_LENGTH, CANResponseFilter.NONE);
    while (frames.hasNext()) {
      final CANFrame frame = frames.next();
      logResponseFrame(frame);
      uniqueIDs.add("0x" + Integer.toHexString(frame.getId()));
    }
    return uniqueIDs;
  }

  private static <T> Set<T> diff(Set<T> base, Set<T> set) {
    final Set<T> result = new HashSet<>();
    for (final T element : set) {
      if (!base.contains(element)) {
        result.add(element);
      }
    }
    return result;
  }

  private static void logRequestFrame(final CANFrame frame) {
    log.info("===>{}", frame);
  }

  private static void logResponseFrame(final CANFrame frame) {
    log.info("<==={}", frame);
  }
}
