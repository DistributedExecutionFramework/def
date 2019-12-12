include "../transfer/DTOs.thrift"

namespace java at.enfilo.def.execlogic.api.thrift
namespace py def_api.thrift.execlogic

/**
* Execution Service: Handles all Programs, Jobs and Tasks.
*/
service ExecLogicService {

    /**
    * Returns all program ids which are accessable by given user id.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getAllPrograms(1: DTOs.Id userId);

    /**
    * Request the creation of a new Program on a specified Cluster (clusterId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createProgram(1: DTOs.Id clusterId, 2: DTOs.Id userId);

    /**
    * Returns info about Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getProgram(1: DTOs.Id pId);

    /**
    * Delete existing Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId deleteProgram(1: DTOs.Id pId);

    /**
    * Abort running Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId abortProgram(1: DTOs.Id pId);

    /**
    * Update name of Programm (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId updateProgramName(1: DTOs.Id pId, 2: string name);

    /**
    * Update description of Programm (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId updateProgramDescription(1: DTOs.Id pId, 2: string description);

    /**
    * Mark Program (pId) as finished.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId markProgramAsFinished(1: DTOs.Id pId);

    /**
    * Request the list of Jobs (jId) for a given Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getAllJobs(1: DTOs.Id pId);

    /**
    * Request the creation of a new Job on a specified Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createJob(1: DTOs.Id pId);

    /**
    * Requests info about specific Job given by Program Id (pId) and Job Id (jId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getJob(1: DTOs.Id pId, 2: DTOs.Id jId);

    /**
    * Requests deletion of a specific Job given by Program Id (pId) and Job Id (jId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId deleteJob(1: DTOs.Id pId, 2: DTOs.Id jId);

    /**
    * Request the MapRoutine which is attached to the specified Job by Program Id (pId) and Job Id (jId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getAttachedMapRoutine(1: DTOs.Id pId, 2: DTOs.Id jId);

    /**
    * Attaches a MapRoutine (mapRoutineId) to an Job (specified by pId and jId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId attachMapRoutine(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.Id mapRoutineId);

    /**
    * Returns attached ReduceRoutine (rlId) of the requested Program (pId) and Job (jId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getAttachedReduceRoutine(1: DTOs.Id pId, 2: DTOs.Id jId);

    /**
    * Attaches a ReduceRoutine (rlId) to an Job (specified by pId and jId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId attachReduceRoutine(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.Id reduceRoutineId);

    /**
    * Requests a list of all Tasks (tId) for a given Program (pId) and Job (jId) sorted by the given sorting criterion.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getAllTasks(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.SortingCriterion sortingCriterion);

    /**
    * Requests a list of Tasks (tId) matching with the given etate for a given Program (pId) and Job (jId) sorted by the given sorting criterion.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getAllTasksWithState(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.ExecutionState state, 4: DTOs.SortingCriterion sortingCriterion);

    /**
    * Request the creation of a new Task on a specified Program (pId) and Job (jId) using
    * given Routine Instance (= Routine Id with Parameters)
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createTask(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.RoutineInstanceDTO objectiveRoutine);

    /**
    * Returns info about Task by a combination of Program (pId), Job (jId) and Task (tId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getTask(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.Id tId);

    /**
    * Returns info about Task by a combination of Program (pId), Job (jId) and Task (tId).
    * Includes in- and/or out-resources (parameter) if set.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getTaskPartial(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.Id tId, 4: bool includeInParameters, 5: bool includeOutParameters);

    /**
    * Mark the given Job (jId) on Program (pId) as complete. This means all Tasks are created.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId markJobAsComplete(1: DTOs.Id pId, 2: DTOs.Id jId);

    /**
    * Abort Job (jId) on Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId abortJob(1: DTOs.Id pId, 2: DTOs.Id jId);

    /**
    * Abort a running Task (tId) on a specified Job (jId) and Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId abortTask(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.Id tId);

    /**
    * Re run a given Task (tId). This Task must be at state FAILED or SUCCESS.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId reRunTask(1: DTOs.Id pId, 2: DTOs.Id jId, 3: DTOs.Id tId);

    /**
    * Request a list of all SharedResources of a Program (pId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getAllSharedResources(1: DTOs.Id pId);

    /**
    * Request the creation of a new SharedResource on a specified Program (pId) of a given DatyType (dataType) and data.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createSharedResource(1: DTOs.Id pId, 2: DTOs.Id dataTypeId, 3: binary data);

    /**
    * Request the SharedResource by a combination of Program (pId) and SharedResource (rId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getSharedResource(1: DTOs.Id pId, 2: DTOs.Id rId);

    /**
    * Request the deletion of a SharedResource by a combination of Program (pId) and SharedResource (rId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId deleteSharedResource(1: DTOs.Id pId, 2: DTOs.Id rId);
}


service ExecLogicResponseService {

    /**
    * Returns a list of program ids.
    */
    list<DTOs.Id> getAllPrograms(1: DTOs.TicketId ticketId);

    /**
    * Returns the id (pId) of the created Program by the given ticket.
    */
    DTOs.Id createProgram(1: DTOs.TicketId ticketId);

    /**
    * Returns info about Program (pId) requested by the given ticket.
    */
    DTOs.ProgramDTO getProgram(1: DTOs.TicketId ticketId);

    /**
    * Returns list of Jobs (jId) requested by the given ticket.
    */
    list<DTOs.Id> getAllJobs(1: DTOs.TicketId ticketId);

    /**
    * Returns the id (jId) of the created Job by this ticket.
    */
    DTOs.Id createJob(1: DTOs.TicketId ticketId);

    /**
    * Returns info about Job requested by given ticket.
    */
    DTOs.JobDTO getJob(1: DTOs.TicketId ticketId);

    /**
     * Returns attached MapRoutine Id requested by the given ticket.
     */
    DTOs.Id getAttachedMapRoutine(1: DTOs.TicketId ticketId);

    /**
    * Returns attached ReduceLibrary Id requested by the given ticket.
    */
    DTOs.Id getAttachedReduceRoutine(1: DTOs.TicketId ticketId);

    /**
    * Returns a list of Tasks (tId) requested by the given ticket.
    **/
    list<DTOs.Id> getAllTasks(1: DTOs.TicketId ticketId);

    /**
    * Returns list of Tasks (tId) requested by the given ticket.
    */
    list<DTOs.Id> getAllTasksWithState(1: DTOs.TicketId ticketId);

    /**
    * Returns the id (jId) of the created Task by this ticket.
    */
    DTOs.Id createTask(1: DTOs.TicketId ticketId);

    /**
    * Returns info about Task requested by the given ticket.
    */
    DTOs.TaskDTO getTask(1: DTOs.TicketId ticketId);

    /**
    * Returns info about Task requested by the given ticket.
    */
    DTOs.TaskDTO getTaskPartial(1: DTOs.TicketId ticketId);

    /**
    * Returns a list of all SharedResources requested by the given ticket.
    */
    list<DTOs.Id> getAllSharedResources(1: DTOs.TicketId ticketId);

    /**
    * Returns the id of created SharedResource by the given ticket.
    */
    DTOs.Id createSharedResource(1: DTOs.TicketId ticketId);

    /**
     * Returns SharedResource requested by the given ticket.
     */
    DTOs.ResourceDTO getSharedResource(1: DTOs.TicketId ticketId);
}
