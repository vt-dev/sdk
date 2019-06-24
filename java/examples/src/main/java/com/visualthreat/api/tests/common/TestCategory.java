package com.visualthreat.api.tests.common;

public enum TestCategory {
  SYSTEM_SCAN("System Scan"),
  FLOODING("Flooding"),
  FUZZING("Fuzzing"),
  TAMPERING("Tampering"),
  REVERSING("Reversing"),
  SPOOFING("Spoofing"),
  OTHER("Others"),
  INTERNAL("Internal"),
  PROTOCOLS("Protocols");

  private final String value;

  public String toString() {
    return this.value;
  }

  public static TestCategory getEnum(String value) {
    TestCategory[] var1 = values();
    int var2 = var1.length;

    for (int var3 = 0; var3 < var2; ++var3) {
      TestCategory category = var1[var3];
      if (category.value.equalsIgnoreCase(value)) {
        return category;
      }
    }

    return null;
  }

  private TestCategory(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
