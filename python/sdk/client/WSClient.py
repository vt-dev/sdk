from queue import Queue

from ws4py.client.threadedclient import WebSocketClient


class WSClient(WebSocketClient):
    dead = 'dead'

    def __init__(self, url, headers, reading_queue, cert_path):
        """
        :param str url: 
        :param list of tuple headers: 
        :param Queue reading_queue: 
        """
        self.__reading_queue = reading_queue
        ssl_options = {'cert_reqs': 2}
        if cert_path:
            ssl_options['ca_certs'] = cert_path
        WebSocketClient.__init__(self, url=url, headers=headers, heartbeat_freq=15, ssl_options=ssl_options)

    def opened(self):
        pass

    def closed(self, code, reason=None):
        self.__reading_queue.put_nowait(WSClient.dead)

    def received_message(self, m):
        if m is not None:
            self.__reading_queue.put_nowait(m.data)
