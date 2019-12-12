include "../transfer/DTOs.thrift"

namespace java at.enfilo.def.node.observer.api.thrift

/**
* Node Observer Service interface.
* A node notifies all registered observers, which must implement this interface.
**/
service NodeObserverService {

    /**
    * Notification that a list of tasks changed a new execution state.
    */
    DTOs.TicketId notifyTasksNewState(1: DTOs.Id nId, 2: list<DTOs.Id> taskIds, 3: DTOs.ExecutionState newState);

    /**
    * Notification that a node received a list of tasks.
    */
    DTOs.TicketId notifyTasksReceived(1: DTOs.Id nId, 2: list<DTOs.Id> taskIds);

    /**
    * Notification over Node state.
    */
    DTOs.TicketId notifyNodeInfo(1: DTOs.Id nId, 2: DTOs.NodeInfoDTO nodeInfo);
}