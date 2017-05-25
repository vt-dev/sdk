import json


class Mappable(object):
    """Dummy iface for plain object extraction"""
    __slots__ = ()

    def to_map(self):
        """
        :return: 
        :rtype: dict of (str, str)
        """
        result = {}
        for key in self.__slots__:
            field = getattr(self, key)
            if isinstance(field, Mappable):
                result[key] = field.to_json()
            else:
                result[key] = field
        return result

    def to_json(self):
        return json.dumps(self.to_map(), sort_keys=True)

    @staticmethod
    def from_str(obj):
        return None


class ListAsMappable(Mappable):
    """Mappable representation for list of instances"""
    __slots__ = ('__inst', '__data')

    def __init__(self, inst):
        self.__inst = inst
        self.__data = []

    def __iter__(self):
        return self.__data.__iter__()

    def __eq__(self, other):
        if isinstance(other, ListAsMappable):
            return self.__data == other.__data
        else:
            return False

    def populate(self, collection):
        for elem in collection:
            self.__data.append(self.__inst(elem))

    def __repr__(self):
        return str(self.__data)

    def to_map(self):
        result = []
        for inst in self.__data:
            result.append(inst.to_map())
        return result


class NoneAsMappable(Mappable):
    """Mappable representation for None(nothing)"""
    __slots__ = ()

    def __eq__(self, other):
        return isinstance(other, NoneAsMappable)

    def __repr__(self):
        return None

    def to_json(self):
        return ''

    def __str__(self):
        return ''

    @staticmethod
    def from_str(obj):
        return NoneAsMappable()


class StringAsMappable(Mappable):
    """Mappable representation for string"""
    __slots__ = ('__data')

    def __init__(self, string):
        self.__data = string

    def __eq__(self, other):
        if isinstance(other, StringAsMappable):
            return self.__data == other.__data
        else:
            return False

    def __repr__(self):
        return self.__data

    def to_json(self):
        return self.__data

    @staticmethod
    def from_str(obj):
        return StringAsMappable(obj)
