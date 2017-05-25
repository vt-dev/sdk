package com.visualthreat.api;

import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.exception.APIException;

import java.util.Collection;
import java.util.Iterator;

/**
 * Work with connected CAN bus device.
 * Able to send and receive CAN frames.
 */
public interface VTCloud {
  /**
   * Sends CAN frames with CAN filter on responses.
   *
   * @param requests          Will be sent to a device. Collection size should be less or equal to 4090.
   * @param canResponseFilter Filter for received CAN frames.
   * @return Returns asynchronous iterator with responses.
   * @throws APIException Will be thrown if VTCloud is closed, have no internet connection or requests size
   *                      is bigger than 4098.
   */
  Iterator<Response> sendCANFrames(final Collection<Request> requests,
                                   final CANResponseFilter canResponseFilter)
      throws APIException;

  /**
   * Runs sniff.
   *
   * @param interval          Sniff interval in milliseconds.
   * @param canResponseFilter Filter for received CAN frames.
   * @return All sniffed messages.
   */
  Iterator<CANFrame> sniff(final long interval,
                           final CANResponseFilter canResponseFilter);

  /**
   * Cancels requests in CAN device (current and all queued).
   */
  void cancelRequest();

  /**
   * Closes the connection and cleanup resources.
   */
  void close();
}
