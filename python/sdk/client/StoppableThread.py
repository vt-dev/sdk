import threading
from time import sleep

from queue import Queue, Empty

import sdk.util as util
from sdk.client.WSClient import WSClient
from sdk.data.Messages import Message
from sdk.data.Messages import CANFinal


class StoppableThread(threading.Thread):
    def __init__(self, name):
        """
        :param str name:
        """
        threading.Thread.__init__(self, name=name)
        self.__stop = threading.Event()

    def stop(self):
        self.__stop.set()

    def stopped(self):
        return self.__stop.isSet()


class SendingThread(StoppableThread):
    terminate = 'quit'

    def __init__(self, sending_queue, ws):
        """
        :param Queue sending_queue:
        :param WSClient ws:
        """
        StoppableThread.__init__(self, name='SendingThread')
        self.__ws = ws
        self.__queue = sending_queue

    def run(self):
        while not self.stopped():
            try:
                msg = self.__queue.get_nowait()
                if isinstance(msg, str):
                    if msg == self.terminate:
                        break
                    else:
                        self.__ws.send(msg)
                self.__queue.task_done()
            except Empty:
                sleep(0.1)


class ReadingThread(StoppableThread):
    def __init__(self, reading_queue, callback_map, dead_callback):
        """
        :param Queue reading_queue:
        :param dict of (str, (Response) -> None) callback_map:
        :param () -> None dead_callback:
        """
        StoppableThread.__init__(self, name='ReadingThread')
        self.__reading_queue = reading_queue
        self.__callback_map = callback_map
        self.__dead_callback = dead_callback
        self.__stop = threading.Event()

    def run(self):
        while not self.stopped():
            try:
                response_str = self.__reading_queue.get(timeout=3)
                self.__reading_queue.task_done()
                if util.P3:
                    response_str = response_str.decode('UTF-8')
                if isinstance(response_str, str):
                    if response_str == WSClient.dead:
                        self.__dead_callback()
                    else:
                        response = self.__decode_str(response_str)
                        if response is not None and isinstance(response, Message):
                            callback = self.__callback_map.get(response.get_id(), None)
                            if callback is not None:
                                callback(response)
                            if isinstance(response, CANFinal):
                                del self.__callback_map[response.get_id()]
            except Exception as exc:
                if not isinstance(exc, Empty):
                    print(str(exc))

    @staticmethod
    def __decode_str(_str):
        import json
        return json.loads(_str, object_hook=Message.object_decoder)
