from def_api.routine import run_routine
from def_api.routine.base import ObjectiveRoutine
from def_api.ttypes import DEFDouble


class SquareCalc(ObjectiveRoutine):
    def __run__(self):
        x = self.__get_parameter__('x', DEFDouble())
        rv = DEFDouble(x.value * x.value)
        return rv


if __name__ == '__main__':
    run_routine(SquareCalc)
