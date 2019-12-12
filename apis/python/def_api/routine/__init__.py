import sys

__all__ = ['run_routine']


def run_routine(routine_cls):
    """
    Start a DEF Routine
    :param routine_cls: name of class (extends ObjectiveRoutine)
    :return:
    """
    if len(sys.argv) < 4:
        print("Routine needs at least 5 arguments.")
        exit(1)
    in_pipe = sys.argv[1]
    out_pipe = sys.argv[2]
    ctrl_pipe = sys.argv[3]
    routine = routine_cls(in_pipe=in_pipe, out_pipe=out_pipe, ctrl_pipe=ctrl_pipe)
    routine.run()
