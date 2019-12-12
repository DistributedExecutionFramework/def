from def_api.routine import run_routine
from def_api.routine.base import ObjectiveRoutine
from def_api.ttypes import DEFDouble


class Demo(ObjectiveRoutine):
    def __run__(self):
        print('yeah')
        rv = DEFDouble(100)
        return rv


if __name__ == '__main__':
    run_routine(Demo)
