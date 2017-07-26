import json

import sdk.util as util
from sdk.data import compression
from sdk.data.CANFrame import CANFrame
from sdk.data.Mappable import Mappable, NoneAsMappable, ListAsMappable, StringAsMappable


class Message(Mappable):
    """ Request base class"""

    __slots__ = ('message', 'type', 'id')

    def __init__(self, message_type, message):
        """
        :param str message_type:
        :param Mappable message:
        """
        self.id = 0
        self.message = message
        self.type = message_type

    def set_id(self, message_id):
        self.id = message_id

    def get_id(self):
        return self.id

    def to_map(self):
        """
        :return:
        :rtype: dict of (str, str)
        """
        result = {}
        for key in self.__slots__:
            field = getattr(self, key)
            if isinstance(field, Mappable):
                if isinstance(field, NoneAsMappable):
                    pass
                else:
                    result[key] = field.to_json()
            else:
                result[key] = field
        return result

    @staticmethod
    def object_decoder(obj):
        if 'type' in obj and obj['type'] in types:
            try:
                const = types[obj['type']]
                instance = const.from_str(obj['message'])
                instance.set_id(obj['id'])
                return instance
            except Exception as exc:
                print(str(exc))
                return None
        else:
            return None

    def __repr__(self):
        return self.type + '(id:' + str(self.id) + ', message:' + str(self.message) + ');'


class Ping(Message):
    """ Ping message"""

    _type = 'PING'

    def __init__(self, message):
        """
        :param str message:
        """
        Message.__init__(self, self._type, StringAsMappable(message))

    @staticmethod
    def from_str(_str):
        return Ping(_str)


class Sniff(Message):
    """Sniff message"""
    _type = 'ZIP_CAN_SNIFF'

    def __init__(self, _id, message):
        """
        :param integer _id:
        :param  Mappable message:
        """
        Message.__init__(self, self._type, compression.compress(message.to_json()))
        self.set_id(_id)


class CANRequest(Message):
    """Sniff message"""
    _type = 'ZIP_CAN_REQUEST'

    def __init__(self, _id, message):
        """
        :param integer _id:
        :param  Mappable message:
        """
        Message.__init__(self, self._type, compression.compress(message.to_json()))
        self.set_id(_id)


class CANCancel(Message):
    """Cancel message"""
    _type = 'CAN_CANCEL'

    def __init__(self, message):
        """
        :param  Mappable message:
        """
        Message.__init__(self, self._type, message)


class CANFinal(Message):
    """Stop message"""
    _type = 'CAN_FINAL'

    def __init__(self):
        Message.__init__(self, self._type, NoneAsMappable())

    @staticmethod
    def from_str(obj):
        return CANFinal()


class Disconnect(Message):
    _type = 'DISCONNECTED'

    def __init__(self):
        Message.__init__(self, self._type, NoneAsMappable())

    def __eq__(self, other):
        return isinstance(other, Disconnect)

    @staticmethod
    def from_str(_str):
        return Disconnect()


class CANFrames(Message):
    _type = 'CAN_FRAMES'

    def __init__(self, can_frames):
        """
        :param ListAsMappable can_frames:
        """
        Message.__init__(self, self._type, can_frames)

    def __eq__(self, other):
        if isinstance(other, CANFrames):
            return self._type == other._type and self.id == other.id and self.message == other.message
        else:
            return False

    @staticmethod
    def from_str(obj):
        if util.P3 and "decode" in dir(obj):
            obj = obj.decode('UTF-8')
        collection = json.loads(obj)
        mappable_list = ListAsMappable(CANFrame.from_obj)
        mappable_list.populate(collection)
        return CANFrames(mappable_list)


class ZIPCANFrames(CANFrames):
    _type = 'ZIP_CAN_FRAMES'

    def __init__(self, can_frames):
        CANFrames.__init__(self, can_frames)

    @staticmethod
    def from_str(obj):
        return CANFrames.from_str(compression.decompress(obj)[0])


types = {Ping._type: Ping,
         Disconnect._type: Disconnect,
         Sniff._type: Sniff,
         CANRequest._type: CANRequest,
         CANCancel._type: CANCancel,
         CANFrames._type: CANFrames,
         ZIPCANFrames._type: ZIPCANFrames,
         CANFinal._type: CANFinal}
