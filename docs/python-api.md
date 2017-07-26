## Python API

### API
Can be used to authenticate yourself, get available devices and connect to some of them.

#### Constructor
No parameters.  

Usage:

```python
api = API()
```  

#### Methods
- authenticate(key, secret)  
  Authenticates in VT Cloud.

  Parameters:  

  - key  
    Your API key
  - secret  
    You API secret

  Returns token, which is valid for few minutes for further calls in API class.
  You don't need it for VTCloud object, only to acquire it.

  Usage:

```python
token = api.authenticate("my-key", "my-secret")
```

- get_connected_devices(token)  
  Get all available devices.
  
  Parameters:  

  - token  
    Token, which you get from `API.authenticate` call.
    
  Returns list of `Device` objects.
  
  Usage:

```python
devices = api.get_connected_devices(token)
for device in devices:
    if dev.is_available_now():
        print("Available device: " + device.name)
        break
```

- connect_to_device(token, device)  
  Connects to specific device.

  Parameters:  

  - token  
    Token, which you get from `API.authenticate` call.
  - device  
    Device object, you can acquire it from `API.get_connected_devices`.
    
  Returns `VTCloud` object. You can send/receive CAN frames with it.
  
  Usage:

```python
cloud = api.connect_to_device(token, device)
request = Request(CANFrame(0x700, bytearray([0x1])), 300)
cloud.send_can_frames([request], CANResponseFilter.NONE())
cloud.close()
```

### VTCloud
Can be used to send and receive CAN frames.

Instantiated via call to `API.connect_to_device`.

#### Methods
- send_can_frames(requests, can_response_filter)  
  Sends CAN Frames and returns responses.
  
  Parameters:  

  - requests  
    list of Request objects
  - can_response_filter  
    `CANResponseFilter` object. Specify valid filters to get your result faster.
    
  Returns generator of CANResponses.
  
  Usage:

```python
responses = cloud.send_can_frames(requests, CANResponseFilter.filter_min_max_ids(0x600, 0x7FF))
for response in responses:
    print('Request: ' + response.request.to_json())
    for frame in response.iterator():
        print(frame.to_json())
        if frame.data[1] in found:
            found_ids.add(hex(frame.frame_id))
```
    
- sniff(interval, can_response_filter)  
  Sniffs all CAN frames in device for given time interval.
  
  Parameters:  

  - interval  
    Time interval for Sniff, in milliseconds.
  - can_response_filter  
    `CANResponseFilter` object. Specify valid filters to get your result faster.

  Returns generator of sniffed CAN frames 

  Usage:

```python
for frame in cloud.sniff(sniff_time, CANResponseFilter.NONE()):
    print(frame.to_json())
```

- close()  
  Closes current device connection. Always call it, better to do it in try/finally block.
  Otherwise, your connection could hang for some time. In this time you won't be available to
  connect to this device again (up to a minute).

  No parameters.

  Usage:

```python
cloud = api.connect_to_device(token, device)
try:
    responses = cloud.send_can_frames(requests, can_response_filter)
    # handle responses
finally:
    cloud.close()
```

### Device
Describes available device.

#### Fields
- device_id  
  Device identifier. Could be used to search for specific device.
- name  
  Device name. Could be used to search for specific device.

#### Methods
- is_available_now()
  
  Returns true if you can connect to Device right now.

### CANResponse
Value class, for CAN Request-Responses pair. Returned by `VTCloud.send_can_frames call`.

#### Fields
- request  
  CANFrame for request

#### Methods
- iterator()  
  Returns iterator for all responses for current `CANResponse.request`
  
Usage:

```python
responses = cloud.send_can_frames(requests, CANResponseFilter.filter_min_max_ids(0x600, 0x7FF))
for response in responses:
    print('Request: ' + response.request.to_json())
    for frame in response.iterator():
        print(frame.to_json())
        if frame.data[1] in found:
            found_ids.add(hex(frame.frame_id))
```

### CANFrame
Value class, for CAN Frame. Collection of CANFrame return by `VTCloud.sniff`. Used in `CANResponse` class.

#### Constructor
Parameters:  

- frame_id  
  int, frame id
- data  
  bytearray of CAN frame data bytes

#### Fields
- timestamp  
  CAN Frame receive time (epoch time)
- frame_id  
  int, Value of CAN Frame id.
- data  
  bytearray, CAN Frame data bytes.

#### Methods
- to_json()  
  Return str JSON representation

Usage:

```python
for frame in cloud.sniff(sniff_time, CANResponseFilter.NONE()):
    print("Received at: " + str(frame.timestamp))
    print("Frame ID: " + hex(frame.frame_id))
    print("Bytes: " + byteascii.hexlify(frame.data))
```

### Request
Value class, for Request. Contains CANFrame and time to collect responses.

#### Constructor
Parameters:  

- can_frame  
  `CANFrame` object. CAN Frame to send.
- wait_time  
  `int`, time in milliseconds to wait for responses (for this frame)
  
Usage:

```python
can_frame = CANFrame(0x700, bytearray([0x5, 0x30, 0x10]))
responses = cloud.send_can_frames([Request(can_frame, 300)], CANResponseFilter.NONE())
```

### CANResponseFilter
Value class, for CAN Filter.

#### Static constructors
- filter_ids(ids)  
  Pass set of IDs (int) values to allow responses with those IDs only.
- filter_min_max_ids(min_id, max_id)  
  Pass min and max IDs values (inclusive) to filter responses IDs.
- filter_bytes(byte_filters)  
  Pass set of ByteArrayFilter to filter responses by byte values.
- NONE()  
  Returns default empty filter. In this case responses won't be filtered at all.

Usage:

```python
can_response_filter = CANResponseFilter.filter_min_max_ids(0x600, 0x7FF)
cloud.send_can_frames([Request(can_frame, 300)], can_response_filter)
```
