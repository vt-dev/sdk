from sdk.data.Mappable import Mappable


class ByteArrayFilter(Mappable):
    """Byte array filter data class"""

    __slots__ = ('byte0', 'byte1', 'byte2', 'byte3', 'byte4', 'byte5', 'byte6', 'byte7')

    def __repr__(self):
        return 'ByteArrayFilter(' + str(self.to_map()) + ')'

    def __eq__(self, other):
        if isinstance(other, ByteArrayFilter):
            return self.to_map() == other.to_map()
        else:
            return False

    def __hash__(self):
        return hash(self.__repr__())

    def set_byte(self, position, byte):
        """
        :param int position:
        :param int byte:
        :return:
        """
        if 0 <= byte <= 255 and 0 <= position <= 7:
            setattr(self, 'byte' + str(position), byte)

    def to_map(self):
        result = {}
        for key in self.__slots__:
            if hasattr(self, key):
                result[key] = getattr(self, key)
        return result
