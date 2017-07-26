#!/usr/bin/env python

import datetime
import getopt
import sys

from sdk.API import API
from sdk.data.CANResponseFilter import CANResponseFilter


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


def sniff(api_key, secret, device_id, sniff_time, cert_path):
    cloud = setup(api_key, secret, device_id, cert_path)
    file_name = str(datetime.datetime.now()).replace(':', '-') + '.traffic'
    f = open(file_name, 'w')
    try:
        print("Send sniff with interval: " + str(sniff_time) + ' second(s)')
        for frame in cloud.sniff(sniff_time * 1000, CANResponseFilter.NONE()):
            log_frame(frame)
            f.write(frame.to_json() + '\n')
    finally:
        f.close()
        cloud.close()
        print("Log saved to: " + file_name)


def log_frame(frame):
    if frame.response:
        print('<===' + frame.to_json())
    else:
        print('===>' + frame.to_json())


def usage():
    print("Usage: sniff.py -k <key> -s <secret> [-d <\"device id\">] [-t <\"sniff time (in secs)\">]")
    sys.exit(2)


def main(argv):
    # Parsing CLI arguments
    api_key = ''
    secret = ''
    device_id = ''
    cert_path = ''
    time = 1
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
            sniff(api_key, secret, device_id, time, cert_path)
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
