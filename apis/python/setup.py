from setuptools import setup, find_packages

setup(name='def_api',
      version='1.4.6',
      description='DEF API including Client and Routine',
      packages=find_packages(),
      zip_safe=False, install_requires=['thrift', 'numpy', 'pandas', 'requests', 'nest_asyncio'])
