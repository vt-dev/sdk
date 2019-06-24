#!/usr/bin/env python

import getopt
import sys

from sdk.API import API
from sdk.data.CANFrame import CANFrame
from sdk.data.CANResponseFilter import CANResponseFilter
from sdk.data.Request import Request

ecu_data = bytearray([0x02, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00])
found = bytearray([0x50, 0x7f])


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


def ecu_ids(api_key, secret, device_id, wait_time, cert_path):
    cloud = setup(api_key, secret, device_id, cert_path)
    found_ids = set()
    try:
        ids = {i + 0x700 for i in range(0x800 - 0x700)}
        frames = [CANFrame(i, ecu_data) for i in ids]
        requests = [Request(f, wait_time) for f in frames]
        responses = cloud.send_can_frames(requests, CANResponseFilter.filter_min_max_ids(0x700, 0x7FF))
        for response in responses:
            log_frame(response.request)
            for frame in response.iterator():
                log_frame(frame)
                if frame.response and frame.data[1] in found:
                    found_ids.add((hex(response.request.frame_id), hex(frame.frame_id)))

    finally:
        cloud.close()
        if found_ids:
            print("Found " + str(len(found_ids)) + " ECU IDs pairs:")
        for req_id, res_id in found_ids:
            print("\t" + str(req_id) + " -> " + str(res_id))


def log_frame(frame):
    if frame:
        if frame.response:
            print('<===' + frame.to_json())
        else:
            print('===>' + frame.to_json())


def usage():
    print("Usage: ecu_ids.py -k <key> -s <secret> [-d <\"device id\">] [-t <\"wait time (in ms)\">]")
    sys.exit(2)


def main(argv):
    # Parsing CLI arguments
    api_key = ''
    secret = ''
    device_id = ''
    cert_path = ''
    time = 300
    try:
        opts, args = getopt.getopt(argv, "k:s:d:t:a:")
        for opt, arg in opts:
            if opt == '-k':
                api_key = arg
            elif opt == '-s':
                secret = arg
            elif opt == '-d':
                device_id = arg
            elif opt == '-a':
                cert_path = arg
            elif opt == '-t':
                time = int(arg)
        if api_key and secret:
            # starting real work
            ecu_ids(api_key, secret, device_id, time, cert_path)
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
