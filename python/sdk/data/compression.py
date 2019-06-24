import base64
from io import BytesIO
from zipfile import ZipFile


def decompress(compressed):
    try:
        data = BytesIO(base64.b64decode(compressed))
        archive = ZipFile(data)
        return [archive.read(name) for name in archive.namelist()]
    except Exception:
        return []


def compress(data):
    b = BytesIO()
    with ZipFile(b, mode='w') as zf:
        zf.writestr('can.traffic', data)
    return base64.b64encode(b.getvalue()).decode()
