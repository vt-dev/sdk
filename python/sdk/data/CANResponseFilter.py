from sdk.data.ByteArrayFilter import ByteArrayFilter
from sdk.data.Mappable import Mappable


class CANResponseFilter(Mappable):
    """CAN response filter data class"""

    @staticmethod
    def filter_id(ids):
        """
        Static constructor for CANResponseFilter with only id filtering
        :param set of can ids:
        :return:
        :rtype: CANResponseFilter
        """
        return CANResponseFilter(ids, CANResponseFilter.__empty_set)

    @staticmethod
    def filter_bytes(byte_filters):
        """
        Static constructor for CANResponseFilter with only byte filtering
        :param set of ByteArrayFilter  byte_filters:
        :return :
        :rtype: CANResponseFilter
        """
        return CANResponseFilter(CANResponseFilter.__empty_set, byte_filters)

    @staticmethod
    def NONE():
        """
        Dummy constructor for CANResponseFilter without filtering
        :return:
        :rtype: CANResponseFilter
        """
        return CANResponseFilter(CANResponseFilter.__empty_set, CANResponseFilter.__empty_set)

    __wrong_id = -1
    __empty_set = set()

    __slots__ = ('ids', 'byte_filters')

    def __init__(self, ids, byte_filters):
        """
        :param set ids:
        :param set of ByteArrayFilter byte_filters:
        """
        self.ids = ids
        self.byte_filters = byte_filters

    def __repr__(self):
        return 'CANResponseFilter(ids=%s, byte_array_filter=%s)' % (self.ids, self.byte_filters)

    def __eq__(self, other):
        if isinstance(other, CANResponseFilter):
            return self.ids == other.ids and self.byte_filters == other.byte_filters
        else:
            return False

    def to_map(self):
        result = {}
        if self.ids != self.__empty_set:
            result['ids'] = list(self.ids)
        if self.byte_filters != self.__empty_set:
            filters = []
            for byteFilter in self.byte_filters:
                filters.append(byteFilter.to_map())
            result['byteFilters'] = filters
        return result
