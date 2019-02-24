package com.visualthreat.api.tests;

import com.visualthreat.api.API;
import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.Device;
import com.visualthreat.api.tests.common.TestPoints;
import com.visualthreat.api.v1.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

@Slf4j
public class TestRunner {
  private static final Options options = new Options();
  private static String sniffOutputPath = "";
  private static int sniffLength = 0;
  static {
    options.addOption(Arguments.DEVICE.type, "device", true, "Device ID");
    options.addOption(Arguments.KEY.type, "key", true, "API key");
    options.addOption(Arguments.SECRET.type, "secret", true, "Secret");
    options.addOption(Arguments.TEST_TYPE.type, "test_type", true, "Test Point Name");
    options.addOption("h", "help", false, "help information");
    options.addOption(Arguments.SNIFF_OUTPUT_PATH.type, "sniffOutput", true, "SniffOutput");
    options.addOption(Arguments.SNIFF_LENGTH.type, "sniffLength", true, "SniffLength");
  }

  public static void main(String[] args) throws Exception {
    // parsing CLI arguments
    final CommandLineParser parser = new DefaultParser();
    final CommandLine cmd = parser.parse(options, args);
    if(cmd.hasOption("h")){
      showUsage();
      System.exit(0);
    }
    if(cmd.hasOption(Arguments.SNIFF_OUTPUT_PATH.type)
        && cmd.hasOption(Arguments.SNIFF_LENGTH.type)){
      sniffOutputPath = cmd.getOptionValue(Arguments.SNIFF_OUTPUT_PATH.type);
      sniffLength = Integer.parseInt(cmd.getOptionValue(Arguments.SNIFF_LENGTH.type));
    }
    if (cmd.hasOption(Arguments.KEY.type)
        && cmd.hasOption(Arguments.SECRET.type)
        && cmd.hasOption(Arguments.TEST_TYPE.type)) {
      final String key = cmd.getOptionValue(Arguments.KEY.type);
      final String secret = cmd.getOptionValue(Arguments.SECRET.type);
      final String deviceId = cmd.hasOption(Arguments.DEVICE.type) ?
          cmd.getOptionValue(Arguments.DEVICE.type) : null;
      final String testPointID = cmd.hasOption(Arguments.TEST_TYPE.type) ?
          cmd.getOptionValue(Arguments.TEST_TYPE.type) : null;
      // Starting real work
      start(key, secret, deviceId, testPointID);
    } else {
      showUsage();
    }
  }

  private static void showUsage() {
    log.error("Usage: -k <key> -s <secret> -t <test point name> [-d <\"device id\">] [-h <\"help information\">]"
        + getSupportTestPointName());
    System.exit(1);
  }

  private static String getSupportTestPointName(){
    StringBuilder sb = new StringBuilder();
    sb.append("Supported TestPoint Name:\n");
    for(TestPoints testPoints: TestPoints.values()){
      sb.append(testPoints.getTestPoint().getId()).append("\n");
    }
    return sb.toString();
  }

  private static void start(String key, String secret, String deviceId, String testPointID) {
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
      AbstractScenario scenario = runTestCase(cloud, testPointID);
      scenario.run();

    } catch (final Exception e) {
      log.error("Critical Exception", e);
    }
  }

  private static AbstractScenario runTestCase(VTCloud cloud, String testPointId){
    TestPoints testPoint = TestPoints.getEnum(testPointId);

    if (testPoint == null) {
      return null;
    }

    switch (testPoint) {
      case SECURITY_ACCESS:
        return new ScanSecurityAccess(cloud, testPoint);

      case UDS_DISCOVER_IDS:
        return new ScanECUs(cloud, testPoint);

      case UDS_DISCOVER_SERVICES:
        return new ScanECUServices(cloud, testPoint);

      case CAN_FUZZ:
        return new CANFuzz(cloud, testPoint);

      case DOS_FLOOD:
        return new DOSFlood(cloud, testPoint);

      case CAN_TRAFFIC_RESPONSE_RATE:
        return new DetectResponseRate(cloud, testPoint);

      case SNIFF:
        return new Sniff(cloud, testPoint, sniffOutputPath, sniffLength);

      case READ_MEMORY_ADDRESS:
        return new ReadMemoryAddress(cloud, testPoint);

      case SCAN_ROUTINE_CONTROL_VULNERABILITIES:
        return new ScanRoutineControlVulnerabilities(cloud, testPoint);

      case SCAN_UDS_SERVICE_VULNERABILITIES:
        return new ScanUDSServiceVulnerabilities(cloud, testPoint);

      case SCAN_UDS_SUB_FUNCTION_VULNERABILITIES:
        return new ScanUDSSubFunctionVulnerabilities(cloud, testPoint);

      case SIMULATE_REPROGRAMMING:
        return new SimulateReprogramming(cloud, testPoint);

      case UDS_DISCOVER_SUBFUNCTIONS:
        return new ECUSubFunctionsDiscovery(cloud, testPoint);

      case VARY_DLC:
        return new VaryDLC(cloud, testPoint);

      case WRITE_DATA_BY_IDENTIFIER:
        return new WriteDataByIdentifier(cloud, testPoint);

      case WRITE_MEMORY_ADDRESS:
        return new WriteMemoryAddress(cloud, testPoint);

      case GMLAN_READ_DATA_BY_IDENTIFIER:
        return new GMLANReadDataByIdentifier(cloud, testPoint);

      case GMLAN_READ_FAILURE_RECORD:
        return new GMLANReadFailureRecord(cloud, testPoint);

      case GMLAN_WRITE_IDENTIFIER:
        return new GMLANWriteIdentifier(cloud, testPoint);

      case GMLAN_DEVICE_CONTROL:
        return new GMLANDeviceControl(cloud, testPoint);

      case REQUEST_UPLOAD:
        return new RequestUploadTest(cloud, testPoint);

      case XCP_DISCOVER_IDS:
        return new XCPIdsDiscovery(cloud, testPoint);

      case MANIPULATE_COMMUNICATION:
        return new ManipulateCommunication(cloud, testPoint);

      case IO_CONTROL_BY_IDENTIFIER:
        return new IOControlByIdentifier(cloud, testPoint);

      case XCP_MODIFY_MEMORY:
        return new XCPModifyMemory(cloud, testPoint);
      case XCP_DISCOVER_SERVICES:
        return new XCPServicesDiscovery(cloud, testPoint);
      case XCP_SECURITY_ACCESS:
        return new XCPSecurityAccess(cloud, testPoint);
      case LINK_CONTROL:
        return new LinkControl(cloud, testPoint);
    }

    return null;
  }
}
