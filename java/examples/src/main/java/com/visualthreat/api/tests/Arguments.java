package com.visualthreat.api.tests;

enum Arguments {
  DEVICE("d"),
  KEY("k"),
  SECRET("s"),
  TEST_TYPE("t"),
  SNIFF_OUTPUT_PATH("p"),
  SNIFF_LENGTH("l");

  final String type;

  Arguments(final String type) {
    this.type = type;
  }
}
