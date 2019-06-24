import errno
import socket
from _ssl import CERT_NONE
from _ssl import CERT_REQUIRED
from threading import Thread
from ws4py.client.threadedclient import WebSocketClient

import sdk.util as util

if util.P3:
    pass
else:
    pass

try:
    from OpenSSL.SSL import Error as pyOpenSSLError
except ImportError:
    class pyOpenSSLError(Exception):
        pass


class WSClient(WebSocketClient):
    dead = 'dead'

    def __init__(self, url, headers, reading_queue, cert_path):
        """
        :param str url:
        :param list of tuple headers:
        :param Queue reading_queue:
        """
        self.__reading_queue = reading_queue
        ssl_options = {'cert_reqs': CERT_NONE}
        if cert_path:
            ssl_options['ca_certs'] = cert_path
            ssl_options = {'cert_reqs': CERT_REQUIRED}
        WebSocketClient.__init__(self, url=url, headers=headers, heartbeat_freq=15, ssl_options=ssl_options)
        self.__thread = WSThread(self)

    def opened(self):
        pass

    def connect(self):
        WebSocketClient.connect(self)
        self.__thread.start()

    def closed(self, code, reason=None):
        print("Connection closed: " + str(code) + " | " + str(reason))
        self.__reading_queue.put_nowait(WSClient.dead)

    def received_message(self, m):
        if m is not None:
            self.__reading_queue.put_nowait(m.data)

    def once(self):
        """
        Performs the operation of reading from the underlying
        connection in order to feed the stream of bytes.
        Because this needs to support SSL sockets, we must always
        read as much as might be in the socket at any given time,
        however process expects to have itself called with only a certain
        number of bytes at a time. That number is found in
        self.reading_buffer_size, so we read everything into our own buffer,
        and then from there feed self.process.
        Then the stream indicates
        whatever size must be read from the connection since
        it knows the frame payload length.
        It returns `False` if an error occurred at the
        socket level or during the bytes processing. Otherwise,
        it returns `True`.
        """
        if self.terminated:
            return False
        try:
            if not self.buf:
                b = self.sock.recv(self.reading_buffer_size)
                if self._is_secure:
                    b += self._get_from_pending()
                if not b:
                    return False
                self.buf += b
        except (socket.error, OSError, pyOpenSSLError) as e:
            if hasattr(e, "errno") and e.errno == errno.EINTR:
                pass
            else:
                self.unhandled_error(e)
                return False
        else:
            # handle spillover to prevent overfilling a frame
            buf = self.buf[:self.reading_buffer_size]
            remaining = self.buf[self.reading_buffer_size:]

            assert len(buf) + len(remaining) == len(self.buf)

            if not self.process(buf):
                return False
            self.buf = remaining

        return True


class WSThread(Thread):
    def __init__(self, ws):
        Thread.__init__(self, name="WSThread")
        self.__ws = ws
        self.daemon = True

    def run(self):
        try:
            self.__ws.run_forever()
        except KeyboardInterrupt:
            self.__ws.close()
