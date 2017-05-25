from datetime import datetime


class Token(object):
    """Auth token container"""

    def __init__(self, private_token, refresh_token):
        """
        :param str private_token:
        :param str refresh_token:
        """
        self.private_token = private_token
        self.refresh_token = refresh_token

    def get_bearer(self):
        return 'Bearer ' + self.private_token

    def __repr__(self):
        return 'Token(private_token=%s, refresh_token=%s)' % (self.private_token, self.refresh_token)


class Device(object):
    """Data class for describing device and device time slots"""

    @staticmethod
    def map_from_json(json):
        """
        Extract devices from json response
        :param json:
        :return:
        :rtype: list of Device
        """
        devices = []
        for dev in json:
            try:
                ts = []
                try:
                    ts = TimeSlot.map_from_json(dev['timeSlots'])
                except:
                    pass
                devices.append(Device(dev['deviceId'], dev['name'], ts))
            except:
                pass
        return devices

    def __init__(self, device_id, name, time_slots):
        """
        :param str name:
        :param str device_id:
        :param list of TimeSlot time_slots:
        """
        self.name = name
        self.device_id = device_id
        self.time_slots = time_slots

    def __eq__(self, other):
        if isinstance(other, Device):
            return self.name == other.name and self.device_id == other.device_id and self.time_slots == other.time_slots
        else:
            return False

    def is_available_now(self):
        """
        Is device available now
        :rtype: bool
        """
        is_available = self.device_id == 'test-device'
        now = datetime.now()
        for slot in self.time_slots:
            if slot.is_applicable(now):
                is_available = True
        return is_available

    def __str__(self):
        return 'Device(device_id=%s, name=%s)' % (self.device_id, self.name)


class TimeSlot(object):
    """Representation of time interval"""

    @staticmethod
    def time_to_long(time):
        day = 24 * 60 * 60  # POSIX day in seconds (exact value)
        return (time - datetime(1970, 1, 1)).days * day

    @staticmethod
    def map_from_json(json_slots):
        slots = []
        for json_slot in json_slots:
            from_long = json_slot['start'] / 1e3
            if from_long < TimeSlot.time_to_long(datetime.min):
                from_long = TimeSlot.time_to_long(datetime.min)

            duration_long = json_slot['duration'] / 1e3
            to_long = from_long + duration_long
            if to_long > TimeSlot.time_to_long(datetime.max):
                to_long = TimeSlot.time_to_long(datetime.max)

            fr = datetime.fromtimestamp(from_long)
            to = datetime.fromtimestamp(to_long)
            slots.append(TimeSlot(fr, to))
        return slots

    def __init__(self, start, stop):
        """
        :param datetime start:
        :param datetime stop:
        """
        self.start = start
        self.stop = stop

    def __eq__(self, other):
        if isinstance(other, TimeSlot):
            return self.start == other.start and self.stop == other.stop
        else:
            return False

    def is_applicable(self, date_time):
        """
        Is current time in between start and stop
        :param datetime date_time:
        :rtype: bool
        """
        return self.start <= date_time < self.stop

    def __repr__(self):
        return 'TimeSlot(start=%s, stop=%s)' % (self.start, self.stop)
