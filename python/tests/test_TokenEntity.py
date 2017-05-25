from sdk.data.Entities import Token


def test_get_bearer():
    token = Token('private', 'refresh')
    assert token.get_bearer() == "Bearer private"
    assert token.get_bearer() != "Bearer refresh"
