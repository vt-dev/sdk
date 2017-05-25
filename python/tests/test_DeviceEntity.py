from datetime import datetime, timedelta

from sdk.data.Entities import Device, TimeSlot
from tests.test_TimeSlotEntity import reference_time_slot_1, reference_time_slot_2, reference_time_slot_json_1, \
    reference_time_slot_json_2

single_time_slot = [reference_time_slot_1]
multiple_time_slots = [reference_time_slot_1, reference_time_slot_2]

reference_device_json_1 = {'deviceId': 'dev1', 'name': 'dev1', 'timeSlots': [reference_time_slot_json_1]}
reference_device_json_2 = {'deviceId': 'dev2', 'name': 'dev2', 'timeSlots': [reference_time_slot_json_2]}

reference_device_1 = Device('dev1', 'dev1', [reference_time_slot_1])
reference_device_2 = Device('dev2', 'dev2', [reference_time_slot_2])


def test_check_map_from_dummy_json():
    extracted = Device.map_from_json([])
    assert extracted == []


def test_check_map_from_json():
    extrcated = Device.map_from_json([reference_device_json_1])
    assert extrcated == [reference_device_1]


def test_check_map_from_multiple_json():
    extrcated = Device.map_from_json([reference_device_json_1, reference_device_json_2])
    assert extrcated == [reference_device_1, reference_device_2]


def test_check_availability_hit():
    now = datetime.now()
    start = now - timedelta(minutes=1)
    stop = now + timedelta(minutes=1)
    time_slot = TimeSlot(start, stop)
    device = Device('dev', 'dev', [time_slot])
    assert device.is_available_now()


def test_check_availability_miss():
    now = datetime.now()
    start = now - timedelta(minutes=2)
    stop = now - timedelta(minutes=1)
    time_slot = TimeSlot(start, stop)
    device = Device('dev', 'dev', [time_slot])
    assert not device.is_available_now()


def test_custo_equality_fail():
    device = Device('dev', 'dev', [])
    assert not device == {}
