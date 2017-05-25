import base64
import time

import sdk.util as util
from sdk.data.Mappable import Mappable


class CANFrame(Mappable):
    """CAN Frame data class"""

    __slots__ = ('timestamp', 'frame_id', 'data', 'response')

    def __init__(self, frame_id, data):
        """
        :param int frame_id:
        :param bytearray data:
        """
        self.timestamp = int(round(time.time() * 1000))
        self.frame_id = frame_id
        self.data = data
        self.response = False

    def __eq__(self, other):
        if isinstance(other, CANFrame):
            return self.frame_id == other.frame_id and self.data == other.data
        else:
            return False

    def __repr__(self):
        return 'CANFrame(time=%s, frame_id=%s, data=%s, response=%s)' % \
               (self.timestamp, hex(self.frame_id), CANFrame.marshalling_data(self.data), self.response)

    def set_timestamp(self, t):
        self.timestamp = t

    def to_map(self):
        return {
            'timestamp': self.timestamp,
            'id': self.frame_id,
            'data': CANFrame.marshalling_data(self.data)
        }

    @staticmethod
    def marshalling_data(data):
        result = base64.b64encode(data)
        if util.P3:
            result = result.decode('UTF-8')
        return result

    @staticmethod
    def unmarshalling_data(str):
        return base64.b64decode(str)

    @staticmethod
    def from_obj(obj):
        can_frame = CANFrame(obj['id'], CANFrame.unmarshalling_data(obj['data']))
        can_frame.set_timestamp(obj['timestamp'])
        can_frame.response = obj['response']

        return can_frame
