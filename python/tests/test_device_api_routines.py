import pytest

from sdk.data.Entities import Device
from sdk.VTCloud import _get_ws_url, remove_empty_entries


@pytest.mark.parametrize("inp,output", [
    ([1, 2, 3, 4, None], [1, 2, 3, 4]),
    ([1, None, 2, None, 3, 4, None], [1, 2, 3, 4]),
    (['a', 'b', 'c', ''], ['a', 'b', 'c'])
])
def test_remove_empty_entries(inp, output):
    assert remove_empty_entries(inp) == output


@pytest.mark.parametrize('host,port,path,device_id,result', [
    ('host', 443, 'path', '123', 'wss://host:443/path/123'),
    ('host', 443, '/path', '123', 'wss://host:443/path/123'),
    ('host', 443, '/path/', '123', 'wss://host:443/path/123'),
    ('host', 443, '/path/', '/123', 'wss://host:443/path/123'),
])
def test_get_ws_url(host, port, path, device_id, result):
    device = Device(device_id, '', [])
    assert _get_ws_url(host, port, path, device) == result
