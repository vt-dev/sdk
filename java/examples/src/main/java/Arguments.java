enum Arguments {
  DEVICE("d"),
  KEY("k"),
  SECRET("s");

  final String type;

  Arguments(final String type) {
    this.type = type;
  }
}
