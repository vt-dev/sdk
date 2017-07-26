#!/usr/bin/env python

import collections
import getopt
import random
import sys

from sdk.API import API
from sdk.data.CANFrame import CANFrame
from sdk.data.CANResponseFilter import CANResponseFilter
from sdk.data.Request import Request

fuzz_count = 10  # type: int
fuzz_ids = range(0, 0x700)


def count_stats(responses):
    """
    :param iterator of CANResponseIterator responses:
    :return:
    """
    ids = {}
    for response in responses:
        log_frame(response.request)
        for frame in response.iterator():
            log_frame(frame)
            if frame.frame_id in ids:
                ids[frame.frame_id] += 1
            else:
                ids[frame.frame_id] = 1
    print("Amount of unique IDs: " + str(len(ids.keys())))
    oids = collections.OrderedDict(sorted(ids.items()))
    for key, val in oids.items():
        print("ID: " + hex(key) + " was encountered " + str(val) + " time(s)")


def usage():
    print("Usage: fuzzing.py -k <key> -s <secret> [-d <\"device id\">]")
    sys.exit(2)


def setup(api_key, secret, device_id, cert_path):
    # create api connection
    api = API(cert_path=cert_path)
    # get authentication token
    token = api.authenticate(api_key, secret)
    # get device list from cloud
    devices = api.get_connected_devices(token)
    # find available device
    for dev in devices:
        if dev.is_available_now() and ((not device_id) or dev.device_id == device_id):
            print('Found device: ' + str(dev))
            return api.connect_to_device(token, dev)
    else:
        print("Couldn't find device")
        sys.exit(3)


def generate_request():
    # Generate random request to device
    data_length = random.randint(1, 7)
    data = bytearray((random.getrandbits(8) for i in range(data_length)))
    frame_id = fuzz_ids[random.randint(0, len(fuzz_ids) - 1)]
    can_frame = CANFrame(frame_id, data)
    request = Request(can_frame, 100)
    return request


def fuzz(api_key, secret, device_id, cert_path):
    cloud = setup(api_key, secret, device_id, cert_path)
    try:
        requests = [generate_request() for _ in range(fuzz_count)]
        ids = [hex(r.frame.frame_id) for r in requests]
        print("Send request with ids: " + str(ids))
        count_stats(cloud.send_can_frames(requests, CANResponseFilter.filter_min_max_ids(0, 0x300)))
    finally:
        cloud.close()


def log_frame(frame):
    if frame.response:
        print('<===' + frame.to_json())
    else:
        print('===>' + frame.to_json())


def main(argv):
    # Parsing CLI arguments
    api_key = ''
    secret = ''
    device_id = ''
    cert_path = ''
    try:
        opts, args = getopt.getopt(argv, "th:p:k:s:d:a:")
        for opt, arg in opts:
            if opt == '-k':
                api_key = arg
            elif opt == '-s':
                secret = arg
            elif opt == '-d':
                device_id = arg
            elif opt == '-a':
                cert_path = arg
        if api_key and secret:
            # starting real work
            fuzz(api_key, secret, device_id, cert_path)
        else:
            usage()
    except getopt.GetoptError:
        usage()


if __name__ == "__main__":
    try:
        main(sys.argv[1:])
    except Exception as e:
        print('Error: ')
        print(str(e))
