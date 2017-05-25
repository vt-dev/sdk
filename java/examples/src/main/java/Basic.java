import com.visualthreat.api.API;
import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.*;
import com.visualthreat.api.v1.Token;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Basic {
  public static void main(String[] args) {
    // create API object
    final API api = API.get();

    // use test api key and secret
    final String key = "700fb92b-1505-4a39-9b08-927cbe0257d6";
    final String secret = "47ac4af9-ed2a-4ed0-8d59-d431162a3032";

    // authenticate
    final Token token = api.authenticate(key, secret);
    Device device = null;
    for (final Device d : api.getConnectedDevices(token)) {
      // get available devices, Device.TEST_DEVICE is always available
      // compare with Device.getName if you want to use specific one
      if (d.isAvailable()) {
        device = d;
        break;
      }
    }

    // connect to found device and get VTCloud object
    final VTCloud cloud = api.connectToDevice(device, token);

    // create CAN frame, with id 0x700 and data
    final CANFrame canFrame = new CANFrame(0x700, new byte[]{(byte) 0x01});
    // create request to send the can frame
    // and collect all responses from CAN bus for 300 ms
    final Request request = new Request(canFrame, 300);
    // send it, without any response filter
    System.out.println("Sending CAN request...");
    final Iterator<Response> responses = cloud.sendCANFrames(
        Collections.singletonList(request), CANResponseFilter.NONE);

    final Set<String> receivedIDs = new HashSet<>();
    // iterate through all request-responses pairs
    while (responses.hasNext()) {
      final Response response = responses.next();
      final Iterator<CANFrame> responseFrames = response.getResponses();
      // iterate through response frames (you can use Response.getRequest to see request)
      while (responseFrames.hasNext()) {
        // get all unique IDs from received can frames
        receivedIDs.add("0x" + Integer.toHexString(responseFrames.next().getId()));
      }
    }
    cloud.close();

    // print the result
    System.out.println(String.format("%d different ids received: %s",
        receivedIDs.size(), receivedIDs.toString()));
  }
}
