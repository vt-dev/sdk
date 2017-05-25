import sdk.util as util
from sdk.data.CANResponse import CANResponse

if util.P3:
    from queue import Queue
else:
    from Queue import Queue


class CANResponsesIterator(object):
    __slots__ = ('queue', '__last')

    __POISON_PILL = 'poison-pill'

    def __init__(self):
        self.queue = Queue()
        self.__last = CANResponse(None)

    def __repr__(self):
        return 'CANResponsesIterator(total=%d)' % (self.queue.qsize())

    def __eq__(self, other):
        if isinstance(other, CANResponsesIterator):
            return self.queue == other.queue
        else:
            return False

    def iterator(self):
        while True:
            value = self.queue.get(True)
            if value != self.__POISON_PILL:
                yield value
            else:
                break

    def flat_iterator(self):
        while True:
            value = self.queue.get(True)
            if value != self.__POISON_PILL:
                for frame in value.iterator():
                    yield frame
            else:
                break

    def stop(self):
        if not self.__last.empty():
            self.queue.put_nowait(self.__last)
        self.__last.stop()
        self.queue.put_nowait(self.__POISON_PILL)

    def add_frames(self, can_frames):
        for frame in can_frames:
            if frame.response:
                self.__last.add(frame)
            else:
                if not self.__last.empty():
                    self.queue.put_nowait(self.__last)
                    self.__last.stop()
                self.__last = CANResponse(frame)
