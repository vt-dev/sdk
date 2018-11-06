package com.visualthreat.api.tests.common;

public class TestConst {

  // UDS SIDs
  public static int UDS_SID_DIAGNOSTIC_CONTROL        = 0x10;
  public static int UDS_SID_ECU_RESET                 = 0x11;
  public static int UDS_SID_GM_READ_FAILURE_RECORD    = 0x12;
  public static int UDS_SID_CLEAR_DTC                 = 0x14;
  public static int UDS_SID_READ_DTC                  = 0x19;
  public static int UDS_SID_GM_READ_DID_BY_ID         = 0x1A;
  public static int UDS_SID_RESTART_COMMUNICATIONS    = 0x20;
  public static int UDS_SID_READ_DATA_BY_ID           = 0x22;
  public static int UDS_SID_READ_MEM_BY_ADDRESS       = 0x23;
  public static int UDS_SID_READ_SCALING_BY_ID        = 0x24;
  public static int UDS_SID_SECURITY_ACCESS           = 0x27;
  public static int UDS_SID_COMMUNICATION_CONTROL     = 0x28;
  public static int UDS_SID_READ_DATA_BY_ID_PERIODIC  = 0x2A;
  public static int UDS_SID_DEFINE_DATA_ID            = 0x2C;
  public static int UDS_SID_WRITE_DATA_BY_ID          = 0x2E;
  public static int UDS_SID_IO_CONTROL_BY_ID          = 0x2F;
  public static int UDS_SID_ROUTINE_CONTROL           = 0x31;
  public static int UDS_SID_REQUEST_DOWNLOAD          = 0x34;
  public static int UDS_SID_REQUEST_UPLOAD            = 0x35;
  public static int UDS_SID_TRANSFER_DATA             = 0x36;
  public static int UDS_SID_REQUEST_XFER_EXIT         = 0x37;
  public static int UDS_SID_REQUEST_XFER_FILE         = 0x38;
  public static int UDS_SID_WRITE_MEM_BY_ADDRESS      = 0x3D;
  public static int UDS_SID_TESTER_PRESENT            = 0x3E;
  public static int UDS_SID_ACCESS_TIMING             = 0x83;
  public static int UDS_SID_SECURED_DATA_TRANS        = 0x84;
  public static int UDS_SID_CONTROL_DTC_SETTINGS      = 0x85;
  public static int UDS_SID_RESPONSE_ON_EVENT         = 0x86;
  public static int UDS_SID_LINK_CONTROL              = 0x87;
  public static int UDS_SID_GM_PROGRAMMED_STATE       = 0xA2;
  public static int UDS_SID_GM_PROGRAMMING_MODE       = 0xA5;
  public static int UDS_SID_GM_READ_DIAG_INFO         = 0xA9;
  public static int UDS_SID_GM_READ_DATA_BY_ID        = 0xAA;
  public static int UDS_SID_GM_DEVICE_CONTROL         = 0xAE;

  public enum DiagnosticSession {
    DEFAULT(((byte) 0x01)),
    PROGRAMMING(((byte) 0x02)),
    EXTENDED(((byte) 0x03));

    private final byte value;
    private DiagnosticSession(byte sessionVal) {
      this.value = sessionVal;
    }

    public byte getSessionValue() {
      return this.value;
    }
  }
}
