namespace java at.enfilo.def.routine.api
namespace py def_api.thrift.routine
namespace cpp at.enfilo.def.routine.api

enum Command {
    GET_PARAMETER = 0,
    GET_PARAMETER_KEY = 1,
    EXEC_ROUTINE = 2,
    SEND_RESULT = 3,
    ROUTINE_DONE = 4,
    LOG_DEBUG = 5,
    LOG_INFO = 6,
    LOG_ERROR = 7,
    CANCEL = 8
}

struct Order {
    1: Command command,
    2: string value
}

struct Result {
    1: i32 seq,
    2: string key,
    3: string url,
    4: binary data
}
