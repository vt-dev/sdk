#!/usr/bin/env python

import getopt
import sys
import traceback
from time import time

from sdk.API import API
from sdk.data.CANFrame import CANFrame
from sdk.data.CANResponseFilter import CANResponseFilter
from sdk.data.Request import Request

__BATCH_SIZE = 400


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


def replay(api_key, secret, device_id, log_name, wait_time, cert_path):
    with open(log_name, 'r') as f:
        frames = []
        for line in f.readlines():
            try:
                frame = CANFrame.from_json(line.strip('\n'))
                if frame:
                    frames.append(frame)
            except Exception as e:
                pass

        requests = []
        l = len(frames)
        for i in range(l):
            delta = wait_time
            if wait_time < 0:
                delta = 30
                if i < l - 1:
                    delta = max(1, frames[i + 1].timestamp - frames[i].timestamp)

            requests.append(Request(frames[i], delta))

        if len(requests) > 0:
            cloud = setup(api_key, secret, device_id, cert_path)
            start_time = time()
            try:
                l = len(requests)
                batches = [requests[i:i + __BATCH_SIZE] for i in range(0, len(requests), __BATCH_SIZE)]
                print("Replaying " + str(l) + " request(s) by " + str(len(batches)) + " batch(es)")
                replayed = 0
                for batch in batches:
                    responses = cloud.send_can_frames(batch, CANResponseFilter.NONE())
                    for response in responses:
                        replayed += 1
                        if replayed % 100 == 0:
                            print("Replayed " + str(replayed) + " requests from " + str(l) + ", time: " +
                                  str(time() - start_time))
                        log_frame(response.request)
                        for frame in response.iterator():
                            log_frame(frame)
                print("Traffic is replayed, time spent: " + str(time() - start_time) + "sec")
            finally:
                cloud.close()
        else:
            print("Nothing to replay")


def log_frame(frame):
    if frame:
        if frame.response:
            print('<===' + frame.to_json())
        else:
            print('===>' + frame.to_json())


def usage():
    print("Usage: replay.py -k <key> -s <secret> -l <\"log file name\"> [-d <\"device id\">]")
    sys.exit(2)


def main(argv):
    # Parsing CLI arguments
    api_key = ''
    secret = ''
    device_id = ''
    cert_path = ''
    log_name = ''
    wait_time = -1
    try:
        opts, args = getopt.getopt(argv, "k:s:d:l:t:a:")
        for opt, arg in opts:
            if opt == '-k':
                api_key = arg
            elif opt == '-s':
                secret = arg
            elif opt == '-d':
                device_id = arg
            elif opt == '-a':
                cert_path = arg
            elif opt == '-l':
                log_name = arg
            elif opt == '-t':
                wait_time = int(arg)
        if api_key and secret and log_name:
            # starting real work
            replay(api_key, secret, device_id, log_name, wait_time, cert_path)
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
        traceback.print_exc()
