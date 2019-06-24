class APIException(Exception):
    """Base class for API exceptions"""

    def __init__(self, text):
        """
        :param str text:
        """
        self.text = text

    def __str__(self):
        return 'APIException(' + self.text + ')'
