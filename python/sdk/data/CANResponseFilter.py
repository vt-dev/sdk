from sdk.data.ByteArrayFilter import ByteArrayFilter
from sdk.data.Mappable import Mappable


class CANResponseFilter(Mappable):
    """CAN response filter data class"""

    @staticmethod
    def filter_ids(ids):
        """
        Static constructor for CANResponseFilter with only id filtering
        :param set of can ids:
        :return:
        :rtype: CANResponseFilter
        """
        return CANResponseFilter(ids, CANResponseFilter.__empty_set,
                                 CANResponseFilter.__wrong_id, CANResponseFilter.__wrong_id)

    @staticmethod
    def filter_min_max_ids(min_id, max_id):
        """
        Static constructor for CANResponseFilter with min and max values for ids (inclusive)
        :param min_id: int of min id
        :param max_id: int of max id
        :return: CANResponseFilter
        """
        return CANResponseFilter(CANResponseFilter.__empty_set, CANResponseFilter.__empty_set,
                                 min_id, max_id)

    @staticmethod
    def filter_bytes(byte_filters):
        """
        Static constructor for CANResponseFilter with only byte filtering
        :param set of ByteArrayFilter  byte_filters:
        :return :
        :rtype: CANResponseFilter
        """
        return CANResponseFilter(CANResponseFilter.__empty_set, byte_filters,
                                 CANResponseFilter.__wrong_id, CANResponseFilter.__wrong_id)

    @staticmethod
    def NONE():
        """
        Dummy constructor for CANResponseFilter without filtering
        :return:
        :rtype: CANResponseFilter
        """
        return CANResponseFilter(CANResponseFilter.__empty_set, CANResponseFilter.__empty_set,
                                 CANResponseFilter.__wrong_id, CANResponseFilter.__wrong_id)

    __wrong_id = -1
    __empty_set = set()

    __slots__ = ('ids', 'byte_filters', 'min_id', 'max_id')

    def __init__(self, ids, byte_filters, min_id, max_id):
        """
        :param set ids:
        :param set of ByteArrayFilter byte_filters:
        """
        self.ids = ids
        self.byte_filters = byte_filters
        self.min_id = min_id
        self.max_id = max_id

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
        if self.min_id != self.__wrong_id:
            result['minId'] = self.min_id
        if self.max_id != self.__wrong_id:
            result['maxId'] = self.max_id
        return result
