from setuptools import setup

setup(name='sdk',
      version='1.0.0',
      description='Client API for AutoX',
      author='Alexandr Mihailov',
      author_email='mixeyy@gmail.com',
      license='MIT',
      setup_requires=['pytest-runner'],
      tests_require=['pytest', 'pytest-cov'],
      packages=['sdk', 'sdk.client', 'sdk.data', 'sdk.exceptions', 'tests', 'examples'],
      install_requires=[
          'Requests',
          'ws4py',
          'future'
      ],
      zip_safe=False)
