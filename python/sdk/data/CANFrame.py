import base64
import json
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

    def to_json(self):
        return json.dumps({
            'timestamp': self.timestamp,
            'id': hex(self.frame_id),
            'data': [hex(b) for b in bytearray(self.data)]
        })

    @staticmethod
    def from_json(json_str):
        if util.P3 and 'decode' in json_str:
            json_str = json_str.decode('utf-8')
        while True:
            i_0x = json_str.find('"0x')
            if i_0x < 0:
                i_0x = json_str.find('0x')
                if i_0x < 0:
                    break
            if i_0x >= 0:
                i_comma = json_str[i_0x:].find(',')
                if i_comma < 0:
                    i_comma = json_str[i_0x:].find(']')
                i_comma += i_0x
                val_str = json_str[i_0x:i_comma]
                if val_str[0] == '"':
                    val_str = val_str[1:-1]
                value = int(val_str, 16)
                json_str = json_str[:i_0x] + str(value) + json_str[i_comma:]

        loaded = json.loads(json_str)
        can_frame = CANFrame(int(loaded['id']), bytearray(int(i) for i in loaded['data']))
        can_frame.set_timestamp(loaded['timestamp'])
        return can_frame

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
