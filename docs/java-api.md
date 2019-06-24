## Java API

### API

Can be used to authenticate yourself, get available devices and connect to some of them.

#### Static Constructor

- get()  
  Return API object
  
  Usage:
  
```java
final API api = API.get();
```

#### Methods

- Token authenticate(final String apiKey, final String secret) throws APIAuthException  
  Authenticates in VT Cloud.
  
  Parameters:  
    
  - key  
    Your API key
  - secret  
    You API secret
    
  Returns token, which is valid for few minutes for further calls in API class.
  You don't need it for VTCloud object, only to acquire it.
  
  Usage:

```java
final Token token = api.authenticate(key, secret);
```

- Collection<Device> getConnectedDevices(final Token token) throws APIException  
  Get all available devices.
  
  Parameters:  

  - token  
    Token, which you get from `API.authenticate` call.
    
  Returns collection of `Device` objects.
  
  Usage:

```java
for (final Device d : api.getConnectedDevices(token)) {
  if (d.isAvailable()) {
    log.info("Available device: {}", d.getName())
    break;
  }
}
```

- VTCloud connectToDevice(final Device device, final Token token) throws APIException  
  Connects to specific device.

  Parameters:  

  - token  
    Token, which you get from `API.authenticate` call.
  - device  
    Device object, you can acquire it from `API.getConnectedDevices`.
    
  Returns `VTCloud` object. You can send/receive CAN frames with it.
  
  Usage:

```java
final VTCloud cloud = api.connectToDevice(device, token);
final Request request = new Request(new CANFrame(0x700, new byte[]{(byte) 0x01}), 300);
final Iterator<Response> responses = cloud.sendCANFrames(
    Collections.singletonList(request), CANResponseFilter.NONE);
cloud.close();
```

### VTCloud

Can be used to send and receive CAN frames.

Instantiated via call to `API.connectToDevice`.

#### Methods

- Iterator<Response> sendCANFrames(final Collection<Request> requests, final CANResponseFilter canResponseFilter) throws APIException  
  Sends CAN Frames and returns responses.
  
  Parameters:  

  - requests  
    collection of Request objects
  - canResponseFilter  
    `CANResponseFilter` object. Specify valid filters to get your result faster.
    
  Returns iterator of Responses.
  
  Usage:

```java
final Iterator<Response> responses = cloud.sendCANFrames(
    Collections.singletonList(generateRequest()), CANResponseFilter.filterIds(0, 0x300));

while (responses.hasNext()) {
  final Response response = responses.next();
  handleResponse(response.getResponses());
}
```
    
- Iterator<CANFrame> sniff(final long interval, final CANResponseFilter canResponseFilter)  
  Sniffs all CAN frames in device for given time interval.
  
  Parameters:  

  - interval  
    Time interval for Sniff, in milliseconds.
  - canResponseFilter  
    `CANResponseFilter` object. Specify valid filters to get your result faster.

  Returns iterator of sniffed CAN frames 

  Usage:

```java
final Iterator<CANFrame> frames = cloud.sniff(10000, CANResponseFilter.NONE);
while (frames.hasNext()) {
  uniqueIDs.add("0x" + Integer.toHexString(frames1.next().getId()));
}
```

- close()  
  Closes current device connection. Always call it, better to do it in try/finally block.
  Otherwise, your connection could hang for some time. In this time you won't be available to
  connect to this device again (up to a minute).

  No parameters.

  Usage:

```java
cloud = api.connectToDevice(device, token);
try {
  for (count = 0; count < maxCount; count++) {
    makeRequest(cloud);
  }
} finally {
  cloud.close();
}
```

### Device
Describes available device.

#### Methods

- String getDeviceId()  
  Device identifier. Could be used to search for specific device.
- String getName()  
  Device name. Could be used to search for specific device.
- boolean isAvailable()  
  Returns true if you can connect to Device right now.

### Response
Value class, for CAN Request-Responses pair. Returned by `VTCloud.sendCANFrames call`.

#### Methods
- CANFrame getRequest()  
  CANFrame for request
- Iterator<CANFrame> getResponses  
  Returns iterator for all responses for current `CANResponse.getRequest()`
  
Usage:

```java
final Iterator<Response> responses = cloud.sendCANFrames(
    Collections.singletonList(request), CANResponseFilter.NONE);

while (responses.hasNext()) {
  final Response response = responses.next();
  final Iterator<CANFrame> responseFrames = response.getResponses();
  while (responseFrames.hasNext()) {
    // handle response frames
  }
}
cloud.close();
```

### CANFrame
Value class, for CAN Frame. Collection of CANFrame return by `VTCloud.sniff`. Used in `Response` class.

#### Constructor
Parameters:  

- id  
  int, frame id
- data  
  byte[], byte values of CAN frame data

#### Methods
- long getTimestamp()  
  CAN Frame receive time (epoch time)
- int getId()  
  int, Value of CAN Frame id.
- byte[] getData()  
  CAN Frame data bytes.

Usage:

```java
final Iterator<CANFrame> frames = cloud.sniff(10000, CANResponseFilter.NONE);
while (frames.hasNext()) {
  uniqueIDs.add("0x" + Integer.toHexString(frames.next().getId()));
}
```

### Request
Value class, for Request. Contains CANFrame and time to collect responses.
  
Usage:

```java
final CANFrame canFrame = new CANFrame(0x700, new byte[]{(byte) 0x01});
// create request to send the can frame
// and collect all responses from CAN bus for 300 ms
final Request request = new Request(canFrame, 300);
```

### CANResponseFilter
Value class, for CAN Filter.

#### Static constructors
- filterIds(Set<Integer> ids)  
  Pass set of IDs (int) values to allow responses with those IDs only.
- filterIds(Integer minId, Integer maxId)  
  Pass min and max IDs values (inclusive) to filter responses IDs.
- filterBytes(Set<ByteArrayFilter> byteFilters)  
  Pass set of ByteArrayFilter to filter responses by byte values.
- NONE 
  Returns default empty filter. In this case responses won't be filtered at all.

Usage:

```java
final CANResponseFilter filter = CANResponseFilter.filterIds(0, 0x300);
final Iterator<Response> responses = cloud.sendCANFrames(
    Collections.singletonList(generateRequest()), filter);
```
