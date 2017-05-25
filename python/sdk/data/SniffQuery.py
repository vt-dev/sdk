from sdk.data.CANResponseFilter import CANResponseFilter
from sdk.data.Mappable import Mappable


class SniffQuery(Mappable):
    """Sniff query with serialization routine"""
    __slots__ = ('interval', 'canResponseFilter')

    def __init__(self, interval, can_response_filter):
        """
        :param int interval: 
        :param CANResponseFilter can_response_filter: 
        """
        self.interval = interval
        self.canResponseFilter = can_response_filter

    def to_map(self):
        return {'interval': self.interval, 'canResponseFilter': self.canResponseFilter.to_map()}
