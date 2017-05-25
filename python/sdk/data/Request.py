from sdk.data.CANFrame import CANFrame
from sdk.data.Mappable import Mappable


class Request(Mappable):
    """ Request to device representation"""
    __slots__ = ('frame', 'wait_time')

    def __init__(self, frame, wait_time):
        """
        :param CANFrame frame: 
        :param int wait_time: 
        """
        self.frame = frame
        self.wait_time = wait_time

    def __repr__(self):
        return 'Request(frame=%s, wait_time=%s)' % (self.frame, self.wait_time)

    def to_map(self):
        return {'canFrame': self.frame.to_map(), 'waitTime': self.wait_time}
