from queue import Queue

from sdk.client.StoppableThread import SendingThread, ReadingThread
from sdk.client.WSClient import WSClient
from sdk.data.Messages import Message


class Client(object):
    """
    Client routine class
    """

    def __init__(self, url, headers, cert_path):
        self.__callbacks = {}
        self.__sending_queue = Queue()
        self.__reading_queue = Queue()
        self.__ws = WSClient(url, headers, self.__reading_queue, cert_path)
        self.__ws.connect()
        self.__sending_thread = SendingThread(self.__sending_queue, self.__ws)
        self.__sending_thread.start()
        self.__reading_thread = ReadingThread(self.__reading_queue, self.__callbacks, self.close)
        self.__reading_thread.start()

    def __register_cb(self, request_id, callback):
        self.__callbacks[request_id] = callback

    def send(self, request, callback):
        """
        Send message through WS and register response handler
        :param Message request:
        :param function callback:
        :return:
        """
        request_id = request.id
        if request_id != 0:
            self.__register_cb(request_id, callback)
        request_as_str = request.to_json()
        self.__sending_queue.put_nowait(request_as_str)

    def close(self):
        """
        Simply stop threads and wait for termination
        """
        self.__sending_queue.join()
        self.__sending_thread.stop()
        self.__ws.close(code=1000, reason='Goodbye')
        self.__reading_thread.stop()
