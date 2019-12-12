import abc

from pandas import DataFrame
from pickle import dumps, loads

from def_api.ttypes import DEFDataFrame, DEFDataFrameList, DEFInteger, DEFDouble, DEFString, DEFBoolean, DEFLong

__mapper__ = []


class Mapper(metaclass = abc.ABCMeta):
    @abc.abstractmethod
    def can_map(self, value):
        """Returns true if given value can be mapped"""

    def map_value(self, value):
        """Map given value from Python to DEF value and vice-versa"""


def register_mapper(mapper):
    if isinstance(mapper, Mapper):
        __mapper__.append(mapper)
    else:
        raise Exception('Given mapper object is not a Mapper instance.')
    return


def init_mapper():
    register_mapper(BoolMapper())
    register_mapper(IntMapper())
    register_mapper(FloatMapper())
    register_mapper(StrMapper())
    register_mapper(DataFrameMapper())
    register_mapper(DataFrameListMapper())


def map_value(value):
    if isinstance(value, list) and len(value) == 0:
        raise Exception('Can not map an empty list. Type of elements are not given.')
    for mapper in __mapper__:
        if mapper.can_map(value):
            return mapper.map_value(value)
    return None


class IntMapper(Mapper):
    def map_value(self, value):
        if isinstance(value, int):
            return DEFInteger(value=value)
        elif isinstance(value, DEFInteger) or isinstance(value, DEFLong):
            return value.value
        return None

    def can_map(self, value):
        if isinstance(value, int) or isinstance(value, DEFInteger):
            return True
        return False


class FloatMapper(Mapper):
    def map_value(self, value):
        if isinstance(value, float):
            return DEFDouble(value=value)
        elif isinstance(value, DEFDouble):
            return value.value
        return None

    def can_map(self, value):
        if isinstance(value, float) or isinstance(value, DEFDouble):
            return True
        return False


class StrMapper(Mapper):
    def map_value(self, value):
        if isinstance(value, str):
            return DEFString(value=value)
        elif isinstance(value, DEFString):
            return value.value
        return None

    def can_map(self, value):
        if isinstance(value, str) or isinstance(value, DEFString):
            return True
        return False


class BoolMapper(Mapper):
    def map_value(self, value):
        if isinstance(value, bool):
            return DEFString(value=value)
        elif isinstance(value, DEFBoolean):
            return value.value
        return None

    def can_map(self, value):
        if isinstance(value, bool) or isinstance(value, DEFBoolean):
            return True
        return False


class DataFrameMapper(Mapper):
    def can_map(self, value):
        if isinstance(value, DataFrame) or isinstance(value, DEFDataFrame):
            return True
        return False

    def map_value(self, value):
        if isinstance(value, DataFrame):
            buf = dumps(value)
            return DEFDataFrame(pickledDF=buf)
        elif isinstance(value, DEFDataFrame):
            return loads(value.pickledDF)
        return None


class DataFrameListMapper(Mapper):
    def can_map(self, value):
        if (isinstance(value, list) and isinstance(value[0], DataFrame)) or isinstance(value, DEFDataFrameList):
            return True
        return False

    def map_value(self, value):
        if isinstance(value, list) and isinstance(value[0], DataFrame):
            buf = []
            for element in value:
                buf.append(dumps(element))
            return DEFDataFrameList(pickledDFs=buf)
        elif isinstance(value, DEFDataFrameList):
            buf = []
            for pdf in value.pickledDFs:
                buf.append(loads(pdf))
            return buf
        return None
