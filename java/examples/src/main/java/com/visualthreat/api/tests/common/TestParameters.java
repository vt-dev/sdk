package com.visualthreat.api.tests.common;

public class TestParameters {
  public static final TestParameter<Boolean> ERASE_MEMORY_ONLY =
      new TestParameter<>(
          "em",
          "Only use Erase Memory Option",
          Boolean.class, false);
  public static final TestParameter<Boolean> MODIFY_VIN_ONLY =
      new TestParameter<>(
          "vn",
          "Modify VIN number only",
          Boolean.class, false);
  public static final TestParameter<Integer> REQUEST_ID =
      new TestParameter<>(
          "reqId",
          "Request ID",
          Integer.class,
          -1);
  public static final TestParameter<Integer> RESPONSE_ID =
      new TestParameter<>(
          "respId",
          "Response ID",
          Integer.class,
          -1);
  public static final TestParameter<Integer> RESPONSE_WAIT_TIME =
      new TestParameter<>(
          "response_wait_time",
          "Response Wait Time (ms)",
          Integer.class, 25);
  public static final TestParameter<Long> START_ADDRESS =
      new TestParameter<>(
          "start_address",
          "Start Address",
          Long.class, 0L);
  public static final TestParameter<Long> STOP_ADDRESS =
      new TestParameter<>(
          "stop_address",
          "Stop Address",
          Long.class, 0xFFFFFFFFL);
  public static final TestParameter<Boolean> USE_DISCOVERED_IDS =
      new TestParameter<>(
          "udi",
          "Whether to use service ids discovered by previous test",
          Boolean.class, false);
  public static final TestParameter<TestConst.DiagnosticSession> ENTER_SESSION =
      new TestParameter<>(
          "enter_session",
          "Enter Session",
          TestConst.DiagnosticSession.class, TestConst.DiagnosticSession.PROGRAMMING);
  public static final TestParameter<Boolean> EXTENSIVE_EXECUTION_MODE =
      new TestParameter<>(
          "ext",
          "Extensive scanning all possible vulnerabilities of ECUs",
          Boolean.class, false);
  public static final TestParameter<Boolean> DUMP_READ_MEMORY =
      new TestParameter<>(
          "drm",
          "Whether to dump readable memory into a file",
          Boolean.class, false);
}
