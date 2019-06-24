package com.visualthreat.api.tests.common;

import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class ECUServices {
  public final static HashMap<Integer, String> SERVICE_NAMES = new HashMap<Integer, String>() {{
    put(0x10, "DIAGNOSTIC_SESSION_CONTROL");
    put(0x14, "CLEAR_DIAGNOSTIC_INFORMATION");
    put(0x19, "READ_DTC_INFORMATION");
    put(0x20, "RETURN_TO_NORMAL");
    put(0x22, "READ_DATA_BY_IDENTIFIER");
    put(0x23, "READ_MEMORY_BY_ADDRESS");
    put(0x24, "READ_SCALING_DATA_BY_IDENTIFIER");
    put(0x27, "SECURITY_ACCESS");
    put(0x28, "COMMUNICATION_CONTROL");
    put(0x2A, "READ_DATA_BY_PERIODIC_IDENTIFIER");
    put(0x2C, "DYNAMICALLY_DEFINE_DATA_IDENTIFIER");
    put(0x2D, "DEFINE_PID_BY_MEMORY_ADDRESS");
    put(0x2E, "WRITE_DATA_BY_IDENTIFIER");
    put(0x2F, "INPUT_OUTPUT_CONTROL_BY_IDENTIFIER");
    put(0x31, "ROUTINE_CONTROL");
    put(0x34, "REQUEST_DOWNLOAD");
    put(0x35, "REQUEST_UPLOAD");
    put(0x36, "TRANSFER_DATA");
    put(0x37, "REQUEST_TRANSFER_EXIT");
    put(0x38, "REQUEST_FILE_TRANSFER");
    put(0x3D, "WRITE_MEMORY_BY_ADDRESS");
    put(0x3E, "TESTER_PRESENT");
    put(0x7F, "NEGATIVE_RESPONSE");
    put(0x83, "ACCESS_TIMING_PARAMETER");
    put(0x84, "SECURED_DATA_TRANSMISSION");
    put(0x85, "CONTROL_DTC_SETTING");
    put(0x86, "RESPONSE_ON_EVENT");
    put(0x87, "LINK_CONTROL");
  }};

  public static final String UNKNOWN_SERVICE_NAME = "UNKNOWN_SERVICE";

  Map<Integer, List<SupportedService>> ECUServices;
}

