import pytest

from sdk.data.ByteArrayFilter import ByteArrayFilter


def refernce_serialization(byte_num, byte_val):
    return {"byte" + str(byte_num): byte_val}


def test_serializing_empty_ByteArrayFilter():
    byte_array_filter = ByteArrayFilter()
    assert byte_array_filter.to_json() == "{}"


@pytest.mark.parametrize("byte_num,byte_val", [
    (0, 234),
    (1, 234),
    (2, 12),
    (3, 99),
    (5, 12),
    (6, 0),
    (7, 255),
])
def test_serializng_ByteArrayFilter(byte_num, byte_val):
    reference_serialization = {"byte" + str(byte_num): byte_val}
    byte_array_filter = ByteArrayFilter()
    byte_array_filter.set_byte(byte_num, byte_val)
    assert byte_array_filter.to_map() == refernce_serialization(byte_num, byte_val)


@pytest.mark.parametrize("byte_num,byte_val", [
    (8, 12),
    (-1, 12),
    (2, 399),
    (3, -12)
])
def test_wrong_serializng_ByteArrayFilter(byte_num, byte_val):
    byte_array_filter = ByteArrayFilter()
    byte_array_filter.set_byte(byte_num, byte_val)
    assert byte_array_filter.to_map() != refernce_serialization(byte_num, byte_val)


def test_custom_equality():
    byte_array_filter = ByteArrayFilter()
    byte_array_filter.set_byte(2, 20)
    byte_array_filter.set_byte(1, 21)
    byte_array_filter.set_byte(0, 22)

    byte_array_filter_another = ByteArrayFilter()
    byte_array_filter_another.set_byte(2, 20)
    byte_array_filter_another.set_byte(1, 21)
    byte_array_filter_another.set_byte(0, 22)

    assert byte_array_filter == byte_array_filter_another


def test_custom_equality_fail():
    byte_array_filter = ByteArrayFilter()
    byte_array_filter.set_byte(2, 20)
    byte_array_filter.set_byte(1, 21)
    byte_array_filter.set_byte(0, 22)
    assert not byte_array_filter == {}
