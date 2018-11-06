package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.*;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public class ScanECUs extends AbstractScenario{
  private static final byte[] ECU_PAYLOAD = {0x02, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
  private static final List<Byte> BYTE_ONE = Arrays.asList((byte) 0x50, (byte) 0x7f);

  public ScanECUs(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    // connect to found device and get VTCloud object
    try {
      final Collection<String> ecuIDs = new HashSet<>();
      final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

      final Collection<Request> requests = new ArrayList<>();
      for (int i = MIN_ID; i <= MAX_ID; i++) {
        requests.add(Request.Builder.newBuilder()
            .id(i)
            .data(ECU_PAYLOAD)
            .waitTime(getResponseWaitTime(testPoints))
            .build()
        );
      }

      final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
      while (responses.hasNext()) {
        final Response response = responses.next();
        logRequestFrame(response.getRequest());
        final Iterator<CANFrame> frames = response.getResponses();
        while (frames.hasNext()) {
          final CANFrame frame = frames.next();
          logResponseFrame(frame);
          if (BYTE_ONE.contains(frame.getData()[1])) {
            ecuIDs.add(String.format("\t0x%x -> 0x%x", response.getRequest().getId(), frame.getId()));
          }
        }
      }
      log.info("Found {} ECU ID pairs", ecuIDs.size());
      for (final String s : ecuIDs) {
        log.info(s);
      }
    } catch (final Exception e) {
      log.error("Critical Exception", e);
    }
  }
}
