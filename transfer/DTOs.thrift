namespace java at.enfilo.def.transfer.dto
namespace py def_api.thrift.transfer

typedef string TicketId
typedef string Id

//
// Generall DTOs and Enumerations
//

/**
* UUID representation
*/
//struct Id {
//    1: required i64 msb;
//    2: required i64 lsb;
//}

/**
* PeriodUnit (based on Java TimeUnit enum)
**/
enum PeriodUnit {
    SECONDS,
    MINUTES,
    HOURS
}

struct AuthDTO {
    1: string userId;
    2: string token;
}


//
// Library specific DTOs
//

enum LibraryType {
    SLAVE,
    MASTER
}

struct LibraryInfoDTO {
    1: Id id,
    2: LibraryType libraryType,
    3: string storeDriver,
    4: set<Id> storedRoutines
}

//
// Execlogic specific DTOs
//

enum ExecutionState {
    SCHEDULED,
    RUN,
    SUCCESS,
    FAILED
}

struct TaskDTO {
    1: Id id,
    2: Id jobId,
    3: Id programId,
    4: ExecutionState state,
    5: i64 startTime,
    6: i64 createTime,
    7: i64 finishTime,
    8: Id objectiveRoutineId,
    9: Id mapRoutineId,
   10: map<string, ResourceDTO> inParameters,
   11: list<ResourceDTO> outParameters,
   12: list<string> messages,
   13: i64 runtime
}

struct TagDTO {
    1: Id id,
    2: string description
}

struct JobDTO {
    1: Id id,
    2: Id programId,
    3: ExecutionState state,
    4: i64 createTime,
    5: i64 startTime,
    6: i64 finishTime,
    7: i32 scheduledTasks,
    8: i32 runningTasks,
    9: i32 successfulTasks,
   10: i32 failedTasks,
   11: Id mapRoutineId,
   12: optional Id reduceRoutineId,
   13: optional list<ResourceDTO> reducedResults
}

struct ProgramDTO {
    1: Id id,
    2: ExecutionState state,
    3: i64 createTime,
    4: i64 finishTime,
    5: optional bool masterLibraryRoutine = false,
    6: Id userId,
    7: string name,
    8: string description
    9: i32 nrOfJobs
}

struct DataTypeDTO {
    1: Id id,
    2: string name,
    3: string schema
}

struct ResourceDTO {
    1: Id id,
    3: Id dataTypeId,
    4: optional binary data,
    5: optional string url,
    6: optional string key = "DEFAULT"
}

enum RoutineType {
    OBJECTIVE = 0;
    MAP = 1,
    STORE = 2,
    REDUCE = 3,
    MASTER = 4
}

struct RoutineDTO {
    1: Id id,
    2: bool privateRoutine,
    3: string name,
    4: string description,
    5: i16 revision,
    6: RoutineType type,
    8: list<FormalParameterDTO> inParameters,
    9: FormalParameterDTO outParameter,
    10: set<RoutineBinaryDTO> routineBinaries,
    11: list<string> arguments
    12: list<FeatureDTO> requiredFeatures
}

struct FormalParameterDTO {
    1: Id id,
    2: string name,
    3: string description,
    4: DataTypeDTO dataType
}

struct RoutineBinaryDTO {
    1: Id id,
    2: string md5,
    3: i64 sizeInBytes,
    4: bool primary,
    5: optional binary data,
    6: optional string url
}

struct RoutineInstanceDTO {
    1: Id routineId,
    2: map<string, ResourceDTO> inParameters,
    3: list<string> missingParameters
}

struct FeatureDTO {
    1: Id id,
    2: Id baseId,
    3: string name,
    4: string group,
    5: string version,
    6: list<FeatureDTO> extensions
}
//
// Cloud-Communication specific DTOs
//

enum CloudType {
    PUBLIC = 0,
    COMMUNITY = 1,
    PRIVATE = 2,
    HYBRID = 3
}

//
// Node specific DTOs
//

enum NodeType {
    WORKER = 0,
    REDUCER = 1
}

/**
* NodeInfo Data Transfer Object.
* Contains all relevant node information.
*/
struct NodeInfoDTO {
    1: Id id,
    2: Id clusterId,
    3: NodeType type,
    4: i32 numberOfCores,
    5: double load,
    6: i64 timeStamp,
    7: map<string, string> parameters,
    8: string host
}

/**
* NodeEnvironment Data Transfer Object.
* Contains all node features.
*/
struct NodeEnvironmentDTO {
    1: Id id,
    2: list<string> environment
}

/**
* QueueInfo Data Transfer Object.
* Contains all relevant queuePriorityWrapper information.
*/
struct QueueInfoDTO {
    1: Id id,
    2: bool released,
    3: i32 numberOfTasks
}


//
// Cluster specific DTOs
//

struct ClusterInfoDTO {
    1: Id id,
    2: string name,
    3: Id managerId,
    4: CloudType cloudType,
    5: i64 startTime,
    6: list<string> activePrograms,
    7: i32 numberOfWorkers,
    8: i32 numberOfReducers,
    9: Id defaultMapRoutineId,
   10: Id storeRoutineId,
   11: string host
}

//
// Manager Webservice specific DTOs
//

enum SortingCriterion {
    CREATION_DATE_FROM_NEWEST,
    CREATION_DATE_FROM_OLDEST,
    START_DATE_FROM_NEWEST,
    START_DATE_FROM_OLDEST,
    FINISH_DATE_FROM_NEWEST,
    FINISH_DATE_FROM_OLDEST,
    RUNTIME_FROM_LONGEST,
    RUNTIME_FROM_SHORTEST,
    NO_SORTING
}