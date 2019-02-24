package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
public class XCPSecurityAccess extends AbstractScenario {
  private static final int NUMBER_OF_REQUESTS = 50;
  private static final int SEEDS_UNPREDICTABILITY_THREAHOLD = 10;
  private static final byte[] xcpConnectQueryPayload = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00};
  private static byte[] initialRequestSeedPacket = new byte[]{(byte)0xF8, 0, 1, 0, 0, 0, 0, 0};
  private static byte[] requestRemainingSeedPacket = new byte[]{(byte)0xF8, 1, 1, 0, 0, 0, 0, 0};
  private static byte[] defaultUnlockPacket = new byte[]{(byte)0xF7, 0, 0, 0, 0, 0, 0, 0};


  private Map<Integer, Integer> seedLengthMap = new HashMap<>();
  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();

  public XCPSecurityAccess(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIds = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

    for (Integer id : ecuIds.keySet()) {
      for(DiagnosticSession session : sessionList){
        internalSecurityAccessTest(id, session,filter);
      }
    }
  }

  private void internalSecurityAccessTest(int requestId, DiagnosticSession session,CANResponseFilter filter) {
    log.info(String.format("Starts testing ECU=0x%X", requestId));
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    // Test 1: Send 50 requestSeed only requestsSeed Unpredictability
    //    1.1 Seed unpredictability
    //    1.2 Get seed length
    // Send traffic with requestSeed message and measure the response, should not equal
    checkSeedUnpredictabilityTests(requests, filter, requestId, ecuIds.get(requestId));
    // Test 2: Send 50 requestSeed and Unlock requests and check the following cases one by one
    //    1.1 Unlocking
    checkUnlockingAndTimeDelayTests(requests, filter, requestId, ecuIds.get(requestId));
  }

  private void checkSeedUnpredictabilityTests(Collection<Request> requests, CANResponseFilter filter,
      int requestId, Set<Integer> responseIds) {

    List<List<Byte>> seedsList = new LinkedList<>();
    // We send and analyse the response one by one, because XCP use MASTER_BLOCK_MODE by default for
    // the communication mode, which means the next request will be send after receiving the
    // previous response for previous request
    for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
      if(isXcpRequestSendingSuccess(requests, filter, requestId, responseIds, xcpConnectQueryPayload, true)){
        int seedLength = -1;
        // Send first round GET_SEED request
        requests.add(createRequest(requestId, initialRequestSeedPacket));
        // send traffic
        final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
        // analyze logs
        List<Byte> seed = new LinkedList<>();
        while (responses.hasNext()){
          final Response response = responses.next();
          final Iterator<CANFrame> frames = response.getResponses();
          while (frames.hasNext()){
            final CANFrame frame = frames.next();
            if(responseIds.contains(frame.getId())){
              byte[] data = frame.getData();
              if((data[0] & 0xFF) == 0xFF){
                seedLength = data[1] & 0xFF;
                int len = seedLength;
                if(seedLength == 0){
                  log.info(String.format("No seeds needed, the resource is not protected for ECU=0x%X", requestId));
                }else{
                  seedLengthMap.put(requestId, data[1] & 0xFF);
                  for(int j = 2; j < 8; j++){
                    if(len == 0){
                      break;
                    }
                    seed.add(data[j]);
                    len--;
                  }
                }
                break;
              }else if((data[0] & 0xFF) == 0xFE){
                log.info(String.format("Security access not supported for ECU=0x%X", requestId));
                break;
              }
            }
          }
        }
        // Get the remaining seeds if seed length greater than MAX_CTO-2
        if (seedLength > 6){
          seed = getRemainingSeeds(requests, filter, seed, requestId, responseIds);
        }
        seedsList.add(seed);
      }
    }
    // Seed Length
    int seedLength = seedLengthMap.containsKey(requestId) ? seedLengthMap.get(requestId) : 0;
    log.info(String.format("Seed length for ECU=0x%X is %d\n", requestId, seedLength));

    // Seed unpredictability
    if(!seedsList.isEmpty()){
      Set<String> seedsSet = new HashSet<>();
      for(List list : seedsList){
        seedsSet.add(Arrays.toString(list.toArray()));
      }
      if(seedsSet.size() <= SEEDS_UNPREDICTABILITY_THREAHOLD){
        log.info(String.format("Seed Unpredictability test failed for ECU=0x%X because the seeds randomness is %d of %d\n",
            requestId, seedsSet.size(), NUMBER_OF_REQUESTS));

      }else if(seedsSet.size() > SEEDS_UNPREDICTABILITY_THREAHOLD){
        log.info(String.format("Seed Unpredictability test passed for ECU=0x%X because the seeds randomness is %d of %d\n",
            requestId, seedsSet.size(),NUMBER_OF_REQUESTS));
      }
    }else {
      log.info(String.format("Seed Unpredictability test failed for ECU=0x%X because no seeds received\n",
          requestId));
    }
  }

  private void checkUnlockingAndTimeDelayTests(Collection<Request> requests, CANResponseFilter filter,
      int requestId, Set<Integer> responseIds){
    Random rn = new Random();
    for(int i = 0; i < NUMBER_OF_REQUESTS; i++){
      // we will check unlocking only if the security access is supported
      if(isXcpRequestSendingSuccess(requests, filter, requestId, responseIds, xcpConnectQueryPayload, true)
          && isXcpRequestSendingSuccess(requests, filter, requestId, responseIds, initialRequestSeedPacket, true)){
        byte[] curPayLoad = Arrays.copyOf(defaultUnlockPacket, defaultUnlockPacket.length);
        // Set the seed length
        curPayLoad[1] = seedLengthMap.containsKey(requestId) ? (byte) seedLengthMap.get(requestId).intValue() : 0;
        if(!seedLengthMap.containsKey(requestId)){
          log.info("No seeds needed, the resource is not protected");
        }else {
          // fill the seed content
          int len = seedLengthMap.get(requestId);
          for(int j = 2; j < 8; j++){
            if(len == 0){
              break;
            }
            curPayLoad[j] = (byte) (rn.nextInt(256) & 0xFF);
            len--;
          }
          requests.add(createRequest(requestId, curPayLoad));
          // send traffic
          final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
        }
      }
    }
  }

  private List<Byte> getRemainingSeeds(Collection<Request> requests, CANResponseFilter filter,
      List<Byte> seedsList, Integer requestId, Set<Integer> responseIds){
    int remainingSeedLength = -1;
    while(remainingSeedLength != 0){
      requests.add(createRequest(requestId, requestRemainingSeedPacket));
      // send traffic
      final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
      // analyze logs
      while (responses.hasNext()){
        final Response response = responses.next();
        final Iterator<CANFrame> frames = response.getResponses();
        while (frames.hasNext()){
          final CANFrame frame = frames.next();
          if(responseIds.contains(frame.getId())){
            byte[] data = frame.getData();
            if((data[0] & 0xFF) == 0xFF){
              remainingSeedLength = data[1] & 0xFF;
              int len = remainingSeedLength;
              for(int k = 2; k < 8; k++){
                if(len == 0){
                  break;
                }
                seedsList.add(data[k]);
                len--;
              }
              break;
            }else if((data[0] & 0xFF) == 0xFE){
              remainingSeedLength = 0;
              log.info(String.format("Can't find remaining seeds for ECU=0x%X because %s : %s\n",
                  requestId, XCPIdsDiscovery.XCP_ERROR_CODES.get(data[1] & 0xFF)[0]
                  ,XCPIdsDiscovery.XCP_ERROR_CODES.get(data[1] & 0xFF)[1]));
            }
          }
        }
      }
    }
    return seedsList;
  }

}
