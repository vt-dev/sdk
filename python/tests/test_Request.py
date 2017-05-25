from sdk.data.CANFrame import CANFrame
from sdk.data.Request import Request


def test_request_serialization():
    wait_time = 1000
    can_frame = CANFrame(1, bytearray([0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]))
    request = Request(can_frame, wait_time)
    reference_map = {'canFrame': can_frame.to_map(), 'waitTime': wait_time}
    assert reference_map == request.to_map()
