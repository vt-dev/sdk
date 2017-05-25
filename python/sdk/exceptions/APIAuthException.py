from sdk.exceptions.APIException import APIException


class APIAuthException(APIException):
    """ Some error with authentication in cloud """

    def __init__(self, text):
        """
        :param str text:
        """
        self.text = text

    def __str__(self):
        return 'APIAuthException(' + self.text + ')'
