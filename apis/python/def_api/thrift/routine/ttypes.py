#
# Autogenerated by Thrift Compiler (0.11.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#
#  options string: py
#

from thrift.Thrift import TType, TMessageType, TFrozenDict, TException, TApplicationException
from thrift.protocol.TProtocol import TProtocolException
from thrift.TRecursive import fix_spec

import sys

from thrift.transport import TTransport
all_structs = []


class Command(object):
    GET_PARAMETER = 0
    GET_PARAMETER_KEY = 1
    EXEC_ROUTINE = 2
    SEND_RESULT = 3
    ROUTINE_DONE = 4
    LOG_DEBUG = 5
    LOG_INFO = 6
    LOG_ERROR = 7
    CANCEL = 8

    _VALUES_TO_NAMES = {
        0: "GET_PARAMETER",
        1: "GET_PARAMETER_KEY",
        2: "EXEC_ROUTINE",
        3: "SEND_RESULT",
        4: "ROUTINE_DONE",
        5: "LOG_DEBUG",
        6: "LOG_INFO",
        7: "LOG_ERROR",
        8: "CANCEL",
    }

    _NAMES_TO_VALUES = {
        "GET_PARAMETER": 0,
        "GET_PARAMETER_KEY": 1,
        "EXEC_ROUTINE": 2,
        "SEND_RESULT": 3,
        "ROUTINE_DONE": 4,
        "LOG_DEBUG": 5,
        "LOG_INFO": 6,
        "LOG_ERROR": 7,
        "CANCEL": 8,
    }


class Order(object):
    """
    Attributes:
     - command
     - value
    """


    def __init__(self, command=None, value=None,):
        self.command = command
        self.value = value

    def read(self, iprot):
        if iprot._fast_decode is not None and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None:
            iprot._fast_decode(self, iprot, [self.__class__, self.thrift_spec])
            return
        iprot.readStructBegin()
        while True:
            (fname, ftype, fid) = iprot.readFieldBegin()
            if ftype == TType.STOP:
                break
            if fid == 1:
                if ftype == TType.I32:
                    self.command = iprot.readI32()
                else:
                    iprot.skip(ftype)
            elif fid == 2:
                if ftype == TType.STRING:
                    self.value = iprot.readString().decode('utf-8') if sys.version_info[0] == 2 else iprot.readString()
                else:
                    iprot.skip(ftype)
            else:
                iprot.skip(ftype)
            iprot.readFieldEnd()
        iprot.readStructEnd()

    def write(self, oprot):
        if oprot._fast_encode is not None and self.thrift_spec is not None:
            oprot.trans.write(oprot._fast_encode(self, [self.__class__, self.thrift_spec]))
            return
        oprot.writeStructBegin('Order')
        if self.command is not None:
            oprot.writeFieldBegin('command', TType.I32, 1)
            oprot.writeI32(self.command)
            oprot.writeFieldEnd()
        if self.value is not None:
            oprot.writeFieldBegin('value', TType.STRING, 2)
            oprot.writeString(self.value.encode('utf-8') if sys.version_info[0] == 2 else self.value)
            oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()

    def validate(self):
        return

    def __repr__(self):
        L = ['%s=%r' % (key, value)
             for key, value in self.__dict__.items()]
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

    def __eq__(self, other):
        return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

    def __ne__(self, other):
        return not (self == other)


class Result(object):
    """
    Attributes:
     - seq
     - key
     - url
     - data
    """


    def __init__(self, seq=None, key=None, url=None, data=None,):
        self.seq = seq
        self.key = key
        self.url = url
        self.data = data

    def read(self, iprot):
        if iprot._fast_decode is not None and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None:
            iprot._fast_decode(self, iprot, [self.__class__, self.thrift_spec])
            return
        iprot.readStructBegin()
        while True:
            (fname, ftype, fid) = iprot.readFieldBegin()
            if ftype == TType.STOP:
                break
            if fid == 1:
                if ftype == TType.I32:
                    self.seq = iprot.readI32()
                else:
                    iprot.skip(ftype)
            elif fid == 2:
                if ftype == TType.STRING:
                    self.key = iprot.readString().decode('utf-8') if sys.version_info[0] == 2 else iprot.readString()
                else:
                    iprot.skip(ftype)
            elif fid == 3:
                if ftype == TType.STRING:
                    self.url = iprot.readString().decode('utf-8') if sys.version_info[0] == 2 else iprot.readString()
                else:
                    iprot.skip(ftype)
            elif fid == 4:
                if ftype == TType.STRING:
                    self.data = iprot.readBinary()
                else:
                    iprot.skip(ftype)
            else:
                iprot.skip(ftype)
            iprot.readFieldEnd()
        iprot.readStructEnd()

    def write(self, oprot):
        if oprot._fast_encode is not None and self.thrift_spec is not None:
            oprot.trans.write(oprot._fast_encode(self, [self.__class__, self.thrift_spec]))
            return
        oprot.writeStructBegin('Result')
        if self.seq is not None:
            oprot.writeFieldBegin('seq', TType.I32, 1)
            oprot.writeI32(self.seq)
            oprot.writeFieldEnd()
        if self.key is not None:
            oprot.writeFieldBegin('key', TType.STRING, 2)
            oprot.writeString(self.key.encode('utf-8') if sys.version_info[0] == 2 else self.key)
            oprot.writeFieldEnd()
        if self.url is not None:
            oprot.writeFieldBegin('url', TType.STRING, 3)
            oprot.writeString(self.url.encode('utf-8') if sys.version_info[0] == 2 else self.url)
            oprot.writeFieldEnd()
        if self.data is not None:
            oprot.writeFieldBegin('data', TType.STRING, 4)
            oprot.writeBinary(self.data)
            oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()

    def validate(self):
        return

    def __repr__(self):
        L = ['%s=%r' % (key, value)
             for key, value in self.__dict__.items()]
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

    def __eq__(self, other):
        return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

    def __ne__(self, other):
        return not (self == other)
all_structs.append(Order)
Order.thrift_spec = (
    None,  # 0
    (1, TType.I32, 'command', None, None, ),  # 1
    (2, TType.STRING, 'value', 'UTF8', None, ),  # 2
)
all_structs.append(Result)
Result.thrift_spec = (
    None,  # 0
    (1, TType.I32, 'seq', None, None, ),  # 1
    (2, TType.STRING, 'key', 'UTF8', None, ),  # 2
    (3, TType.STRING, 'url', 'UTF8', None, ),  # 3
    (4, TType.STRING, 'data', 'BINARY', None, ),  # 4
)
fix_spec(all_structs)
del all_structs
