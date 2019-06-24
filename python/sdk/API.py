import requests

from sdk.VTCloud import VTCloud
from sdk.data.Entities import Token, Device
from sdk.exceptions.APIAuthException import APIAuthException


def remove_empty_entries(elements):
    return list([el for el in elements if el])


def _get_request_url(host, port, path):
    """
    concat url and add schema
    :param str host:
    :param int port:
    :param str path:
    :return:
    :rtype str
    """
    url = '/'.join(remove_empty_entries(host.split('/')) + remove_empty_entries(path.split('/')))
    url = 'https://' + url
    return url


class API(object):
    """API which connects to cloud and do some routines"""

    __public_auth_path = '/auth/realms/services/protocol/openid-connect/token'
    __private_auth_path = '/auth/public/token?otp=123123'

    __devices_path = '/service/autox/api/remote/devices/reserved'

    __endpoints = {
        "01": "visualthreat.net",
        "02": "us.visualthreat.net",
        "03": "apollo.visualthreat.net",
        # remove endpoints below
        "04": "pilot.visualthreat.net",
        "05": "edu.visualthreat.net",
        "e0": "vt.dev",
        "e1": "vt.devlprs.com",
        "f0": "staging.visualthreat.net"
    }

    def __init__(self, api_host='visualthreat.net', api_port=9001, cert_path=''):
        """
        :param str api_host:
        :param int api_port:
        :param str cert_path:
        """
        self.__api_host = api_host
        self.__api_port = api_port
        self.__cert_path = cert_path

    def __init_host(self, api_key):
        if len(api_key) == 38:
            try:
                self.__api_host = self.__endpoints[api_key[0:2]]
            except KeyError:
                pass
        else:
            self.__api_host = self.__endpoints["01"]

    def authenticate(self, api_key, secret):
        """
        Authenticate to cloud with defined api_key and secret.
        api_key and secret could be occurred in AutoX/shared
        :param str api_key: key for access to api
        :param str secret: secret for access to api
        :return: Authentication token
        :rtype: Token
        """
        self.__init_host(api_key)
        data = {'username': api_key, 'password': secret, 'grant_type': 'key'}

        try:
            public_url = _get_request_url(self.__api_host, self.__api_port, self.__public_auth_path)

            if self.__cert_path:
                public_token = requests.post(public_url, data=data, verify=self.__cert_path).json()['access_token']
            else:
                public_token = requests.post(public_url, data=data).json()['access_token']

            return Token(public_token, public_token)
        except ValueError:
            raise APIAuthException('Could not get token from cloud.')

    def get_connected_devices(self, token):
        """
        Get list of shared devices
        :param Token token: Authentication token
        :return: Shared devices
        :rtype: list of Device
        """
        headers = {'Authorization': token.get_bearer()}
        devices_url = _get_request_url(self.__api_host, self.__api_port, self.__devices_path)

        if self.__cert_path:
            devices_json = requests.get(devices_url, headers=headers, verify=self.__cert_path).json()
        else:
            devices_json = requests.get(devices_url, headers=headers).json()

        return Device.map_from_json(devices_json)

    def connect_to_device(self, token, device):
        """
        Connect to selected device with token
        :param Token token: Authentication token
        :param device: Shared device
        :return: Instance of class for interacting with device
        :rtype: VTCloud
        """
        return VTCloud(device, token, self.__api_host, self.__api_port, self.__cert_path)
