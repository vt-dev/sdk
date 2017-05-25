from sdk.data.ByteArrayFilter import ByteArrayFilter
from sdk.data.CANResponseFilter import CANResponseFilter


def get_byte_array_filter():
    byte_array_filter = ByteArrayFilter()
    byte_array_filter.set_byte(2, 20)
    byte_array_filter.set_byte(1, 21)
    byte_array_filter.set_byte(0, 22)
    return byte_array_filter


def test_static_filter_id_constructor():
    can_response_filter = CANResponseFilter.filter_id({1})
    assert CANResponseFilter({1}, set([])) == can_response_filter


def test_static_filter_bytes_constructor():
    byte_array_filters_1 = {get_byte_array_filter()}
    byte_array_filters_2 = {get_byte_array_filter()}
    can_response_filter = CANResponseFilter.filter_bytes(byte_array_filters_1)
    assert CANResponseFilter(set(), byte_array_filters_2) == can_response_filter


def test_static_none_constructor():
    can_response_filter = CANResponseFilter.NONE()
    assert CANResponseFilter(set(), set([])) == can_response_filter


def test_can_response_filter_serialization():
    can_response_filter = CANResponseFilter({2}, {get_byte_array_filter()})
    can_response_filter_as_map = can_response_filter.to_map()
    reference_map = {'ids': [2], 'byteFilters': [get_byte_array_filter().to_map()]}
    assert reference_map == can_response_filter_as_map


def test_can_response_filter_equatily_fail():
    can_response_filter = CANResponseFilter({2}, {get_byte_array_filter()})
    assert not can_response_filter == {}

