package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;

@Slf4j
public class Sniff extends AbstractScenario {
  private static final long SAVE_INTERVAL = 1000;

  private String outputPath = "";
  private int sniffLength = 0;

  public Sniff(VTCloud cloud,
               TestPoints testPoints) {
    super(cloud, testPoints);
  }

  public Sniff(VTCloud cloud, TestPoints testPoints, String outputPath, int sniffLength) {
    super(cloud, testPoints);
    this.outputPath = outputPath;
    this.sniffLength = sniffLength;
  }

  @Override
  public void run() {
    final Iterator<CANFrame> frames = cloud.sniff(sniffLength, CANResponseFilter.NONE);
    saveSniffResult(frames);
  }

  private void saveSniffResult(Iterator<CANFrame> frames) {
    long time = System.currentTimeMillis();
    File output = new File(outputPath + "/sniffOutput.traffic");
    try {
      PrintWriter printWriter = new PrintWriter(output);
      while (frames.hasNext()) {
        final CANFrame frame = frames.next();
        printWriter.println(frame);
        logResponseFrame(frame);
        final long now = System.currentTimeMillis();
        if (now - time >= SAVE_INTERVAL) {
          printWriter.flush();
        }
      }
      printWriter.close();
    } catch (FileNotFoundException e) {
      log.error("Failed to write sniff result to output file:" + outputPath, e);
    }
  }
}
