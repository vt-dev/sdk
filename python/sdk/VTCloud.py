import itertools

from sdk.client.Client import Client
from sdk.data.CANResponseFilter import CANResponseFilter
from sdk.data.CANResponsesIterator import CANResponsesIterator
from sdk.data.Entities import Device
from sdk.data.Messages import CANFinal
from sdk.data.Messages import CANFrames
from sdk.data.Messages import CANRequest, Sniff
from sdk.data.Query import Query
from sdk.data.SniffQuery import SniffQuery
from sdk.exceptions.APIException import APIException


def remove_empty_entries(elements):
    return list([el for el in elements if el])


def _get_ws_url(host, port, path, device):
    host += ':' + str(port)
    url = '/'.join(remove_empty_entries(host.split('/')) + remove_empty_entries(path.split('/')) +
                   remove_empty_entries(device.device_id.split('/')))
    url = 'wss://' + url
    return url


def _batches(l, n):
    """Yield successive n-sized chunks from l."""
    for i in range(0, len(l), n):
        yield l[i:i + n]


class VTCloud(object):
    """Device API class"""
    __id = 0

    __ws_path = '/public/notify/device'

    __queues = {}

    __MAX_REQUESTS = 100

    __closed = False

    def __init__(self, device, token, api_host, api_port, cert_path):
        """
        :param Device device:
        :param Token token:
        :param str api_host:
        :param int api_port:
        :param str cert_path:
        """
        self.device = device
        self.token = token
        self.is_open = False
        url = _get_ws_url(api_host, api_port, self.__ws_path, device)
        headers = [('Authorization', token.get_bearer())]
        self.__client = Client(url, headers, None)

    def send_can_frames(self, requests, can_response_filter):
        """
        Send list of request to device
        :param list of Request requests:
        :param CANResponseFilter can_response_filter: filter for response CAN frames
        :return iterator for CANFrame request-responses tuples, 'responses' is also an iterator
        """
        if self.__closed:
            raise APIException('Can\'t send frames to closed Cloud connection')
        batches = [Query(reqs, can_response_filter) for reqs in _batches(requests, self.__MAX_REQUESTS)]
        return itertools.chain.from_iterable((self.send_can_query(q) for q in batches))

    def send_can_query(self, query):
        """
        Send list of request to device
        :param Query query: Query with requests and filters
        :return iterator for CANFrame request-responses tuples, 'responses' is also an iterator
        """
        if self.__closed:
            raise APIException('Can\'t send CAN Query to closed Cloud connection')
        self.__id += 1
        request = CANRequest(self.__id, query)
        q = CANResponsesIterator()
        self.__queues[request.id] = q
        self.__client.send(request, self.__response_handler)

        return q.iterator()

    def sniff(self, interval, can_response_filter):
        """
        Get data from CAN bus
        :param int interval: time limit
        :param CANResponseFilter can_response_filter: filter for response CAN frames
        """
        if self.__closed:
            raise APIException('Can\'t sniff frames from closed Cloud connection')
        return self.sniff_by_query(SniffQuery(interval, can_response_filter))

    def sniff_by_query(self, query):
        """
        Get data from CAN bus
        :param SniffQuery query: filter for response CAN frames
        """
        if self.__closed:
            raise APIException('Can\'t sniff frames from closed Cloud connection')
        self.__id += 1
        request = Sniff(self.__id, query)
        q = CANResponsesIterator()
        self.__queues[request.id] = q
        self.__client.send(request, self.__response_handler)
        return q.flat_iterator()

    def close(self):
        """
        Close connection to cloud
        :return: nothing
        """
        self.__closed = True
        self.__client.close()

    def __response_handler(self, response):
        if isinstance(response, CANFrames):
            q = self.__queues[response.id]
            if q is not None and response.message:
                q.add_frames(response.message)

        if isinstance(response, CANFinal):
            q = self.__queues[response.id]
            if q is not None:
                q.stop()
