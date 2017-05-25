from datetime import datetime, timedelta

from sdk.data.Entities import TimeSlot

reference_time_slot_json_1 = {'start': 1491491259000, 'duration': 3600000}
reference_time_slot_json_2 = {'start': 1491494859000, 'duration': 3600000}
reference_time_slot_1 = TimeSlot(datetime.fromtimestamp(1491491259), datetime.fromtimestamp(1491494859))
reference_time_slot_2 = TimeSlot(datetime.fromtimestamp(1491494859), datetime.fromtimestamp(1491498459))


def test_check_map_from_dummy_json():
    extracted = TimeSlot.map_from_json([])
    assert extracted == []


def test_check_map_from_json():
    extracted = TimeSlot.map_from_json([reference_time_slot_json_1])
    assert extracted == [reference_time_slot_1]


def test_check_map_from_multiple_json():
    extracted = TimeSlot.map_from_json([reference_time_slot_json_1, reference_time_slot_json_2])
    assert extracted == [reference_time_slot_1, reference_time_slot_2]


def test_check_availability_hit():
    now = datetime.now()
    start = now - timedelta(minutes=1)
    stop = now + timedelta(minutes=1)
    time_slot = TimeSlot(start, stop)
    assert time_slot.is_applicable(now)


def test_check_availability_miss():
    now = datetime.now()
    start = now - timedelta(minutes=2)
    stop = now - timedelta(minutes=1)
    time_slot = TimeSlot(start, stop)
    assert not time_slot.is_applicable(now)


def test_custom_equality_fail():
    now = datetime.now()
    start = now - timedelta(minutes=1)
    stop = now + timedelta(minutes=1)
    time_slot = TimeSlot(start, stop)
    assert not time_slot == {}
