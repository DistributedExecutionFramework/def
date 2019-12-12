namespace java at.enfilo.def.communication.dto
namespace py def_api.thrift.communication

enum TicketStatusDTO {
    UNKNOWN = 0,
    IN_PROGRESS = 1,
    CANCELED = 2,
    DONE = 3,
    FAILED = 4;
}

enum Protocol {
    DIRECT = 0,
    REST = 1,
    THRIFT_TCP = 2,
    THRIFT_HTTP = 3;
}

struct ServiceEndpointDTO {
    1: string host;
    2: i32 port;
    3: Protocol protocol;
    4: optional string pathPrefix = "/*";
}

