import sdk.util as util

if util.P3:
    from queue import Queue
else:
    from Queue import Queue


class CANResponse(object):
    __POISON_PILL = 'poison-pill'

    __slots__ = ('request', 'response_queue', 'finished')

    def __init__(self, request):
        """
        :param CANFrame request:
        """
        self.request = request
        self.response_queue = Queue()
        self.finished = False

    def __repr__(self):
        return 'CANResponseIterator(request=%s, total_responses=%d)' % (self.request, self.response_queue.qsize())

    def __eq__(self, other):
        if isinstance(other, CANResponse):
            return self.request == other.request and self.response_queue == other.response_queue
        else:
            return False

    def responses(self):
        value = self.response_queue.get(True)
        if value != self.__POISON_PILL:
            yield value

    def add(self, can_frame):
        self.response_queue.put_nowait(can_frame)

    def stop(self):
        self.finished = True
        self.response_queue.put_nowait(self.__POISON_PILL)

    def iterator(self):
        while True:
            value = self.response_queue.get(True)
            if value != self.__POISON_PILL:
                yield value
            else:
                break

    def empty(self):
        return self.response_queue.empty()
