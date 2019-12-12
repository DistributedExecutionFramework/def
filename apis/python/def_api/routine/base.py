from abc import ABCMeta, abstractmethod

from thrift import TSerialization
from thrift.protocol import TBinaryProtocol
from thrift.transport import TTransport

from def_api.mapper import init_mapper
from def_api.thrift.routine.ttypes import Order, Command


def send_object(protocol, transport, obj):
    obj.write(protocol)
    transport.flush()


def send_int(protocol, transport, i):
    protocol.writeI32(i)
    transport.flush()


def send_str(protocol, transport, str_val):
    protocol.writeString(str_val)
    transport.flush()


def send_binary(transport, buf):
    transport.write(buf)
    transport.flush()


class ObjectiveRoutine(metaclass=ABCMeta):
    def __init__(self, in_pipe, out_pipe, ctrl_pipe):
        init_mapper()
        self._in_pipe = in_pipe
        self._out_pipe = out_pipe
        self._ctrl_pipe = ctrl_pipe
        self._in_proto = None
        self._in_trans = None
        self._out_proto = None
        self._out_trans = None
        self._ctrl_proto = None
        self._ctrl_trans = None

    def __log_debug__(self, msg):
        log = Order(command=Command.LOG_DEBUG, value=msg)
        send_object(self._ctrl_proto, self._ctrl_trans, log)
        return

    def __log_info__(self, msg):
        log = Order(command=Command.LOG_INFO, value=msg)
        send_object(self._ctrl_proto, self._ctrl_trans, log)
        return

    def __log_error__(self, msg):
        log = Order(command=Command.LOG_ERROR, value=msg)
        send_object(self._ctrl_proto, self._ctrl_trans, log)
        return

    def run(self):
        # setup communication
        self._in_trans = TTransport.TFileObjectTransport(open(self._in_pipe, 'rb'))
        self._out_trans = TTransport.TFileObjectTransport(open(self._out_pipe, 'wb'))
        self._ctrl_trans = TTransport.TFileObjectTransport(open(self._ctrl_pipe, 'wb'))
        self._in_proto = TBinaryProtocol.TBinaryProtocol(self._in_trans)
        self._out_proto = TBinaryProtocol.TBinaryProtocol(self._out_trans)
        self._ctrl_proto = TBinaryProtocol.TBinaryProtocol(self._ctrl_trans)
        self.__log_debug__('Setup communication done.')
        self.__log_debug__('Start routine: {}'.format(self))

        # run real implementation
        self.__log_debug__('Run routine implementation.')
        result = self.__run__()

        # store result to map routine
        self.__log_debug__('Store result to map routine.')
        buffer = TSerialization.serialize(result)
        send_int(self._out_proto, self._out_trans, len(buffer))
        send_binary(self._out_trans, buffer)

        # Shutdown routine
        self.__log_debug__('Done. Shutdown routine')
        send_object(self._ctrl_proto, self._ctrl_trans, Order(command=Command.ROUTINE_DONE))
        self._in_trans.close()
        self._out_trans.close()
        self._ctrl_trans.close()
        return

    def __get_parameter__(self, name, instance):
        self.__log_debug__('Try to get Parameter {}'.format(name))
        send_object(self._ctrl_proto, self._ctrl_trans, Order(command=Command.GET_PARAMETER, value=name))
        instance.read(self._in_proto)
        self.__log_debug__('Received parameter: {}'.format(name))
        return instance

    @abstractmethod
    def __run__(self):
        pass
