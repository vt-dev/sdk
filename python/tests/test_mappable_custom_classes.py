import pytest

from sdk.data.Mappable import NoneAsMappable, StringAsMappable


def test_none_as_mappable():
    none = NoneAsMappable()
    assert none.to_json() == ''
    assert none == NoneAsMappable()


@pytest.mark.parametrize('string', [
    'asdfg',
    'test-test',
    'dunnoLol'
])
def test_string_as_mappable(string):
    repr = StringAsMappable(string)
    assert repr.to_json() == string
    assert StringAsMappable.from_str(string) == repr


def test_string_as_mappable_equality_fail():
    repr = StringAsMappable('lol')
    assert not repr == {}

