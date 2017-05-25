from sdk.data import CANResponseFilter
from sdk.data.CANFrame import CANFrame
from sdk.data.Mappable import ListAsMappable, NoneAsMappable
from sdk.data.Messages import Message, Sniff, CANRequest, CANCancel, Disconnect, CANFrames
from sdk.data.Query import Query
from sdk.data.Request import Request
from sdk.data.SniffQuery import SniffQuery


def decode(s):
    import json
    return json.loads(s, object_hook=Message.object_decoder)


def encode(obj):
    import json
    return json.dumps(obj, sort_keys=True)


reference_can_frames_json = \
    r'{"id":2,"type":"CAN_FRAMES","message":"[' \
    r'{\"timestamp\":1494429336205,\"id\":530,\"data\":\"MTIzYXNk\",\"response\":false},' \
    r'{\"timestamp\":1494429336206,\"id\":533,\"data\":\"YXNkcXdl\",\"response\":false}]"}'


def get_reference_can_frame():
    can_frame_1 = CANFrame(530, bytearray('123asd', 'utf8'))
    can_frame_1.set_timestamp(1494429336205)
    can_frame_2 = CANFrame(533, bytearray('asdqwe', 'utf8'))
    can_frame_2.set_timestamp(1494429336206)
    list_of_can_frames = ListAsMappable(lambda x: x)
    list_of_can_frames.populate([can_frame_1, can_frame_2])
    frames = CANFrames(list_of_can_frames)
    frames.set_id(2)
    return frames


reference_disconnected_json = \
    r'{"id":2,"type":"DISCONNECTED","message":""}'


def get_reference_disconnected():
    disconnected = Disconnect()
    disconnected.set_id(8)
    return disconnected


reference_can_cancel_json = \
    '{"id": 3, "type": "CAN_CANCEL"}'


def get_reference_can_cancel():
    can_cancell = CANCancel(NoneAsMappable())
    can_cancell.set_id(3)
    return can_cancell


reference_can_request_json = \
    r'{"id": 2, "message": "{\"canResponseFilter\": {}, \"requests\": [{\"canFrame\": ' \
    r'{\"data\": \"dGVzdC10ZXN0\", \"id\": 1792, \"timestamp\": 1494508949810}, \"waitTime\": 100}, ' \
    r'{\"canFrame\": ' \
    r'{\"data\": \"YXNkcXdlenhj\", \"id\": 1792, \"timestamp\": 1494508949811}, \"waitTime\": 100}]}", ' \
    r'"type": "CAN_REQUEST"}'


def get_reference_can_request():
    ecu_id = 0x700
    can_frame_1 = CANFrame(ecu_id, bytearray('test-test', 'utf8'))
    can_frame_2 = CANFrame(ecu_id, bytearray('asdqwezxc', 'utf8'))
    can_frame_1.set_timestamp(1494508949810)
    can_frame_2.set_timestamp(1494508949811)
    requests = [Request(can_frame_1, 100),
                Request(can_frame_2, 100)]

    can_request = CANRequest(1, Query(requests, CANResponseFilter.CANResponseFilter.NONE()))
    can_request.set_id(2)
    return can_request


reference_sniff_json = \
    r'{"id": 1, "message": "{\"canResponseFilter\": {}, \"interval\": 6000}", "type": "CAN_SNIFF"}'


def get_reference_sniff():
    sniff = Sniff(1, SniffQuery(6000, CANResponseFilter.CANResponseFilter.NONE()))
    sniff.set_id(1)
    return sniff


def test_can_frames_should_be_deserialized_correct():
    deserialized_can_frames = decode(reference_can_frames_json)
    assert deserialized_can_frames == get_reference_can_frame()


def test_disconnected_should_be_deserialized_correct():
    deserialized_disconnected = decode(reference_disconnected_json)
    assert deserialized_disconnected == get_reference_disconnected()


def test_can_cancel_should_be_serialized_correctly():
    assert get_reference_can_cancel().to_json() == reference_can_cancel_json


def test_sniff_request_should_be_serialized_correctly():
    assert get_reference_sniff().to_json() == reference_sniff_json


def test_request_should_be_serialized_correctly():
    assert get_reference_can_request().to_json() == reference_can_request_json
