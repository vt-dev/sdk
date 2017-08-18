package com.visualthreat.api;

import com.visualthreat.api.data.Device;
import com.visualthreat.api.exception.APIAuthException;
import com.visualthreat.api.exception.APIException;
import com.visualthreat.api.v1.APIImpl;
import com.visualthreat.api.v1.Token;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface for 3rd party API.
 * Connects to CAN bus devices via VT cloud.
 */
public interface API {
  static API get() {
    return new APIImpl();
  }

  /**
   * Authenticates application in VT Cloud. Should be ran before other methods.
   *
   * @param apiKey Your API key.
   * @param secret Your secret.
   * @return Your access token.
   * @throws APIAuthException Thrown if apiKey and secret aren't correct.
   */
  Token authenticate(final String apiKey, final String secret) throws APIAuthException;

  /**
   * @param token You access token.
   * @return Connected devices, available for your API key (for current and future time slots).
   */
  Collection<Device> getConnectedDevices(final Token token) throws APIException;

  /**
   * Tries to connect to a device.
   *
   * @param device Device to connect to.
   * @param token  You access token.
   * @return API connection to send and receive CAN frames from/to device.
   */
  VTCloud connectToDevice(final Device device, final Token token) throws APIException;
}
