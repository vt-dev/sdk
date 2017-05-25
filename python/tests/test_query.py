from sdk.data.CANFrame import CANFrame
from sdk.data.CANResponseFilter import CANResponseFilter
from sdk.data.Query import Query
from sdk.data.Request import Request
from sdk.data.SniffQuery import SniffQuery


def test_query_serialization():
    wait_time = 1000
    can_frame = CANFrame(1, bytearray([0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]))
    request = Request(can_frame, wait_time)
    query = Query([request], CANResponseFilter.NONE())
    query_as_map = query.to_map()
    reference_map = {'requests': [request.to_map()], 'canResponseFilter': CANResponseFilter.NONE().to_map()}
    assert reference_map == query_as_map


def test_sniff_query():
    interval = 1000
    sniff_query = SniffQuery(interval, CANResponseFilter.NONE())
    sniff_query_as_map = sniff_query.to_map()
    reference_map = {'interval': interval, 'canResponseFilter': CANResponseFilter.NONE().to_map()}
    assert reference_map == sniff_query_as_map
