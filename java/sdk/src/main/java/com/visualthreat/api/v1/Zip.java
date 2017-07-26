package com.visualthreat.api.v1;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public final class Zip {
  private static final int CHUNK = 65536; // 64K

  public static List<byte[]> decompress(byte[] compressedData) {
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(compressedData));
    List<byte[]> result = new LinkedList<>();

    Pattern p = Pattern.compile("^.*\\.(log|txt|traffic)$");

    try {
      ZipEntry entry = zis.getNextEntry();

      while (entry != null) {

        Matcher m = p.matcher(entry.getName());
        if (!entry.isDirectory() && m.matches()) {
          try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copyStream(zis, out, entry);
            result.add(out.toByteArray());
          } catch (IOException e) {
            log.error("Error on zip entry", e);
          }
        }
        entry = zis.getNextEntry();
      }
    } catch (IOException exception) {
      log.error("Error on zip reading", exception);
    }
    return result;
  }

  public static byte[] compress(final String content, final String fileName) {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    compress(content, fileName, os);
    return os.toByteArray();
  }

  private static void copyStream(InputStream in, OutputStream out,
                                 ZipEntry entry) throws IOException {
    byte[] buffer = new byte[1024 * 4];
    long count = 0;
    int n;
    long size = entry.getSize();
    while (-1 != (n = in.read(buffer)) && ((count < size) || (size == -1))) {
      out.write(buffer, 0, n);
      count += n;
    }
  }

  private static void compress(final String content, final String fileName, final OutputStream os) {
    try (ZipOutputStream zos = new ZipOutputStream(os)) {
      ZipEntry entry = new ZipEntry(fileName);

      zos.putNextEntry(entry);
      byte[] data = content.getBytes();
      int size = data.length;
      for (int i = 0; i < size; i += CHUNK) {
        int len = Math.min(CHUNK, size - i);
        zos.write(content.getBytes(), i, len);
        os.flush();
      }

      zos.closeEntry();
      zos.close();
      os.close();

    } catch (final IOException ioe) {
      log.error("Error while zipping traffic", ioe);
    }
  }
}
