from sdk.data.CANResponseFilter import CANResponseFilter
from sdk.data.Mappable import Mappable


class Query(Mappable):
    """Raw query with serialization routine"""
    __slots__ = ('requests', 'canResponseFilter')

    def __init__(self, requests, can_response_filter):
        """
        :param list of Request requests: 
        :param CANResponseFilter can_response_filter: 
        """
        self.requests = requests
        self.canResponseFilter = can_response_filter

    def to_map(self):
        requests = []
        for request in self.requests:
            requests.append(request.to_map())
        return {'requests': requests, 'canResponseFilter': self.canResponseFilter.to_map()}
