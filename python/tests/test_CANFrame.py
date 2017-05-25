import pytest

from sdk.data.CANFrame import CANFrame


def reference_obj(id, data, timestamp, response):
    return {
        'data': data,
        'id': id,
        'timestamp': timestamp,
        'response': response
    }


def reference_map(id, data, timestamp):
    return {
        'data': data,
        'id': id,
        'timestamp': timestamp
    }


@pytest.mark.parametrize("id,bytes,data", [
    (1, '123asd', 'MTIzYXNk'),
    (2, 'asdqwe', 'YXNkcXdl'),
    (3, '000111', 'MDAwMTEx'),
    (4, [0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00], 'AgAAAAAAAAA='),
    (5, [0xDE, 0xAD, 0xBE, 0xEF, 0xDE, 0xAD, 0xBA, 0xBA], '3q2+796turo=')])
def test_data_serialization_CANFrame(id, bytes, data):

    bytes_as_array = None
    if isinstance(bytes, str):
        bytes_as_array = bytearray(bytes, 'utf8')
    else:
        bytes_as_array = bytearray(bytes)

    can_frame = CANFrame(id, bytes_as_array)
    can_frame_map = can_frame.to_map()
    assert can_frame_map == reference_map(
        id, data, can_frame.timestamp)


@pytest.mark.parametrize("id,in_bytes,data", [
    (1, '123asd', 'MTIzYXNk'),
    (2, 'asdqwe', 'YXNkcXdl'),
    (3, '000111', 'MDAwMTEx'),
    (4, [0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00], 'AgAAAAAAAAA='),
    (5, [0xDE, 0xAD, 0xBE, 0xEF, 0xDE, 0xAD, 0xBA, 0xBA], '3q2+796turo=')])
def test_extract_CANFrame_from_obj(id, in_bytes, data):

    bytes_as_array = None
    if isinstance(in_bytes, str):
        bytes_as_array = bytearray(in_bytes, 'utf8')
    else:
        bytes_as_array = bytearray(in_bytes)

    actual_frame = CANFrame(id, bytes_as_array)
    expected_frame = CANFrame.from_obj(
        reference_obj(id, data, actual_frame.timestamp, actual_frame.response))
    assert expected_frame == actual_frame


@pytest.mark.parametrize('data', [
    'test',
    'abcd1abcd',
    'deadbeef',
    [0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00],
    [0xDE, 0xAD, 0xBE, 0xEF, 0xDE, 0xAD, 0xBA, 0xBA]
])
def test_dummy_data_conversion(data):

    bytes_as_array = None
    if isinstance(data, str):
        bytes_as_array = bytearray(data, 'utf8')
    else:
        bytes_as_array = bytearray(data)

    assert CANFrame.unmarshalling_data(CANFrame.marshalling_data(bytes_as_array)) == bytes_as_array


def test_custom_equality_fail():
    can_frame = CANFrame(1, bytearray([0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]))
    assert not can_frame == {}
