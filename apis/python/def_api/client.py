import asyncio

import requests
from time import sleep
from thrift import TSerialization
from thrift.protocol import TBinaryProtocol
from thrift.protocol import TMultiplexedProtocol
from thrift.transport import TSocket
from thrift.transport.THttpClient import THttpClient

from def_api.mapper import init_mapper
from def_api.thrift.execlogic import ExecLogicService
from def_api.thrift.ticket import TicketService
from def_api.thrift.communication.ttypes import *
from def_api.thrift.execlogic import ExecLogicResponseService
from .json import json2thrift, thrift2json
from def_api.thrift.transfer.ttypes import *


class RESTExecLogicService(ExecLogicService.Iface):
    def __init__(self, host, port):
        self._base_url = 'http://{}:{}/api/exec-logic'.format(host, port)
        self._post_headers = {'Content-type': 'application/json'}

    def getAllJobs(self, pId):
        url = self._base_url + '/programs'
        r = requests.get(url)
        return r.text

    def createSharedResource(self, pId, dataTypeId, data):
        url = self._base_url + '/programs/' + pId + '/resources'
        params = {'dataTypeId': dataTypeId}
        r = requests.post(url, params=params, data=data, headers=self._post_headers)
        return r.text

    def getAllSharedResources(self, pId):
        url = self._base_url + '/programs/' + pId + '/resources'
        r = requests.get(url)
        return r.text

    def markProgramAsFinished(self, pId):
        url = self._base_url + '/programs/' + pId + '/finished'
        r = requests.post(url)
        return r.text

    def deleteProgram(self, pId):
        url = self._base_url + '/programs/' + pId
        r = requests.delete(url)
        return r.text

    def abortJob(self, pId, jId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/abort'
        r = requests.post(url)
        return r.text

    def markJobAsComplete(self, pId, jId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/tasks/complete'
        r = requests.post(url)
        return r.text

    def getAttachedMapRoutine(self, pId, jId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/map'
        r = requests.get(url)
        return r.text

    def getJob(self, pId, jId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId
        r = requests.get(url)
        return r.text

    def attachMapRoutine(self, pId, jId, mapRoutineId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/map'
        r = requests.put(url, data=mapRoutineId, headers=self._post_headers)
        return r.text

    def getTask(self, pId, jId, tId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/tasks/' + tId
        r = requests.get(url)
        return r.text

    def getAllTasksWithState(self, pId, jId, state, sortingCriterion):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/tasks'
        params = {'state': state, 'sortingCriterion': sortingCriterion}
        r = requests.get(url, params=params)
        return r.text

    def getAllTasks(self, pId, jId, sortingCriterion):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/allTasks'
        params = {'sortingCriterion': sortingCriterion}
        r = requests.get(url, params=params)
        return r.text

    def createProgram(self, cId, uId):
        url = self._base_url + '/programs'
        params = {'clusterId': cId, 'userId': uId}
        r = requests.post(url, params=params)
        return r.text

    def createJob(self, pId):
        url = self._base_url + '/programs/' + pId + '/jobs'
        r = requests.post(url)
        return r.text

    def attachReduceRoutine(self, pId, jId, reduceRoutineId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/map'
        r = requests.put(url, data=reduceRoutineId, headers=self._post_headers)
        return r.text

    def getSharedResource(self, pId, rId):
        url = self._base_url + '/programs/' + pId + '/resources/' + rId
        r = requests.get(url)
        return r.text

    def deleteJob(self, pId, jId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId
        r = requests.delete(url)
        return r.text

    def getAttachedReduceRoutine(self, pId, jId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/reduce'
        r = requests.get(url)
        return r.text

    def getProgram(self, pId):
        url = self._base_url + '/programs/' + pId
        r = requests.get(url)
        return r.text

    def deleteSharedResource(self, pId, rId):
        url = self._base_url + '/programs/' + pId + '/resources/' + rId
        r = requests.delete(url)
        return r.text

    def createTask(self, pId, jId, objectiveRoutine):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/tasks'
        r = requests.post(url, data=thrift2json(objectiveRoutine), headers=self._post_headers)
        return r.text

    def getAllPrograms(self, userId):
        url = self._base_url + '/programs'
        params = {'userId': userId}
        r = requests.get(url, params=params)
        return r.text

    def abortTask(self, pId, jId, tId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/tasks/' + tId
        r = requests.delete(url)
        return r.text

    def reRunTask(self, pId, jId, tId):
        url = self._base_url + '/programs/' + pId + '/jobs/' + jId + '/tasks/' + tId
        r = requests.post(url)
        return r.text


class RESTExecLogicResponseService(ExecLogicResponseService.Iface):
    def __init__(self, host, port):
        self._base_url = 'http://{}:{}/api/response/exec-logic'.format(host, port)

    def getAttachedMapRoutine(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/map'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return json2thrift(r.text, RoutineDTO)

    def createProgram(self, ticketId):
        url = self._base_url + '/programs'
        params = {'ticketId': ticketId}
        r = requests.post(url, params=params)
        return r.text

    def getSharedResource(self, ticketId):
        url = self._base_url + '/programs/pId/resources/rId'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return json2thrift(r.text, ResourceDTO)

    def getAllJobs(self, ticketId):
        url = self._base_url + '/programs/pId/jobs'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return r.json()

    def getAllPrograms(self, ticketId):
        url = self._base_url + '/programs'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return r.json()

    def getAllTasks(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/allTasks'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return r.json()

    def getAllTasksWithState(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/tasks'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return r.json()

    def getJob(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return json2thrift(r.text, JobDTO)

    def getProgram(self, ticketId):
        url = self._base_url + '/programs/pId'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return json2thrift(r.text, ProgramDTO)

    def getAttachedReduceRoutine(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/reduce'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return r.text

    def createSharedResource(self, ticketId):
        url = self._base_url + '/programs/pId/resources'
        params = {'ticketId': ticketId}
        r = requests.post(url, params=params)
        return r.text

    def createTask(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/tasks'
        params = {'ticketId': ticketId}
        r = requests.post(url, params=params)
        return r.text

    def createJob(self, ticketId):
        url = self._base_url + '/programs/pId/jobs'
        params = {'ticketId': ticketId}
        r = requests.post(url, params=params)
        return r.text

    def getAllSharedResources(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/reduce'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return r.json()

    def getTask(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/tasks/tId'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return json2thrift(r.text, TaskDTO)

    def getTaskPartial(self, ticketId):
        url = self._base_url + '/programs/pId/jobs/jId/tasks/tId/partial'
        params = {'ticketId': ticketId}
        r = requests.get(url, params=params)
        return json2thrift(r.text, TaskDTO)


class RESTTicketService(TicketService.Iface):
    def __init__(self, host, port):
        self._base_url = 'http://{}:{}/api/tickets'.format(host, port)

    def cancelTicketExecution(self, ticketId, mayInterruptIfRunning):
        url = '{}/{}'.format(self._base_url, ticketId)
        params = {'mayInterraupt': mayInterruptIfRunning}
        requests.delete(url, params)

    def getTicketStatus(self, ticketId):
        url = '{}/{}'.format(self._base_url, ticketId)
        r = requests.get(url)
        return TicketStatusDTO._NAMES_TO_VALUES[r.text.replace('"', '')]


class DEFClient(object):
    POLL_DELAY = 200 / 1000
    THRIFT_SERVICE_NAME_REQUEST = "/at.enfilo.def.execlogic.api.thrift.ExecLogicService"
    THRIFT_SERVICE_NAME_TICKET = "/at.enfilo.def.communication.api.ticket.thrift.TicketService"
    THRIFT_SERVICE_NAME_RESPONSE = "/at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService"

    def __init__(self, host='manager', port=40002, protocol=Protocol.THRIFT_TCP, request_client=None, response_client=None,
                 ticket_client=None, loop=asyncio.get_event_loop()):
        init_mapper()
        self._loop = loop
        self._request_client = None
        self._ticket_client = None
        self._response_client = None
        if request_client is not None and response_client is not None and ticket_client is not None:
            self._request_client = request_client
            self._response_client = response_client
            self._ticket_client = ticket_client
        else:
            if protocol == Protocol.THRIFT_TCP:
                transport = TSocket.TSocket(host, port)
                protocol = TBinaryProtocol.TBinaryProtocol(transport)
                protocol_request = TMultiplexedProtocol.TMultiplexedProtocol(protocol, self.THRIFT_SERVICE_NAME_REQUEST)
                protocol_ticket = TMultiplexedProtocol.TMultiplexedProtocol(protocol, self.THRIFT_SERVICE_NAME_TICKET)
                protocol_response = TMultiplexedProtocol.TMultiplexedProtocol(protocol,
                                                                              self.THRIFT_SERVICE_NAME_RESPONSE)
                self._request_client = ExecLogicService.Client(protocol_request)
                self._ticket_client = TicketService.Client(protocol_ticket)
                self._response_client = ExecLogicResponseService.Client(protocol_response)
                transport.open()
            elif protocol == Protocol.REST:
                self._request_client = RESTExecLogicService(host, port)
                self._response_client = RESTExecLogicResponseService(host, port)
                self._ticket_client = RESTTicketService(host, port)
            elif protocol == Protocol.THRIFT_HTTP:
                transport_request = THttpClient('http://{}:{}/{}'.format(host, port, self.THRIFT_SERVICE_NAME_REQUEST))
                transport_response = THttpClient('http://{}:{}/{}'.format(host, port, self.THRIFT_SERVICE_NAME_RESPONSE))
                transport_ticket = THttpClient('http://{}:{}/{}'.format(host, port, self.THRIFT_SERVICE_NAME_TICKET))
                protocol_request = TBinaryProtocol.TBinaryProtocol(transport_request)
                protocol_response = TBinaryProtocol.TBinaryProtocol(transport_response)
                protocol_ticket = TBinaryProtocol.TBinaryProtocol(transport_ticket)
                self._request_client = ExecLogicService.Client(protocol_request)
                self._response_client = ExecLogicResponseService.Client(protocol_response)
                self._ticket_client = TicketService.Client(protocol_ticket)
                transport_request.open()
                transport_response.open()
                transport_ticket.open()

    async def __wait_for_ticket(self, ticket_id, future, method = None):
        state = self._ticket_client.waitForTicket(ticket_id)
        if state == TicketStatusDTO.DONE:
            if method:
                result = method(ticket_id)
            else:
                result = state
            future.set_result(result)
        elif state == TicketStatusDTO.CANCELED:
            future.cancel()
        elif state == TicketStatusDTO.FAILED:
            failed_msg = self._ticket_client.getFailedMessage(ticket_id)
            raise ('Error while execute ticket: {}'.format(failed_msg))
        else:
            raise ('Unknown state: {}'.format(state))

    def get_all_programs(self, user_id):
        """
        Returns a list of Programs for a given user
        :param user_id: User id
        :return: List of Program ids
        """
        ticket_id = self._request_client.getAllPrograms(user_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.getAllPrograms),
            loop=self._loop
        )
        return future

    def create_program(self, cluster_id, user_id):
        """
        Creates a new Program for the given user on a defined cluster
        :param cluster_id: Cluster Id to create and run the Program on
        :param user_id: User to assign
        :return: Program id
        """
        ticket_id = self._request_client.createProgram(cluster_id, user_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.createProgram),
            loop=self._loop
        )
        return future

    def get_program(self, p_id):
        """
        Returns all meta information for the given Program id
        :param p_id: Program Id
        :return: ProgramDTO
        """
        ticket_id = self._request_client.getProgram(p_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future, self._response_client.getProgram), loop=self._loop)
        return future

    def delete_program(self, p_id):
        """
        Delete the specified Program and all attached resources (Jobs, Tasks, SharedResource)
        :param p_id: Program id to delete
        :return:
        """
        ticket_id = self._request_client.deleteProgram(p_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def mark_program_as_finished(self, p_id):
        """
        Mark a Program as finished, this means all Jobs and Tasks were submitted.
        :param p_id: Program Id
        :return:
        """
        ticket_id = self._request_client.markProgramAsFinished(p_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def get_all_jobs(self, p_id):
        """
        Returns all Job ids from a given Program.
        :param p_id: Program id
        :return: List of Job ids
        """
        ticket_id = self._request_client.getAllJobs(p_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future, self._response_client.getAllJobs), loop=self._loop)
        return future

    def create_job(self, p_id):
        """
        Creates a new Job in a given Program.
        :param p_id: Program id
        :return: Job id
        """
        ticket_id = self._request_client.createJob(p_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future, self._response_client.createJob), loop=self._loop)
        return future

    def get_job(self, p_id, j_id):
        """
        Returns meta information from a specified Job.
        :param p_id: Program id
        :param j_id: Job id
        :return: JobDTO
        """
        ticket_id = self._request_client.getJob(p_id, j_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future, self._response_client.getJob), loop=self._loop)
        return future

    def delete_job(self, p_id, j_id):
        """
        Delete a specified Job with all attached Tasks.
        :param p_id: Program id
        :param j_id: Job id
        :return:
        """
        ticket_id = self._request_client.deleteJob(p_id, j_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def get_attached_map_routine(self, p_id, j_id):
        """
        Returns the assigned MapRoutine id for the given Job.
        :param p_id: Program id
        :param j_id: Job id
        :return: MapRoutine id
        """
        ticket_id = self._request_client.getAttachedMapRoutine(p_id, j_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.getAttachedMapRoutine),
            loop=self._loop
        )
        return future

    def attach_map_routine(self, p_id, j_id, map_routine_id):
        """
        Attach/Assign a MapRoutine to the given Job.
        :param p_id: Program id
        :param j_id: Job id
        :param map_routine_id: MapRoutine id
        :return:
        """
        ticket_id = self._request_client.attachMapRoutine(p_id, j_id, map_routine_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def get_attached_reduce_routine(self, p_id, j_id):
        """
        Returns the assigned ReduceRoutine id for the given Job.
        :param p_id: Program id
        :param j_id: Job id
        :return: ReduceRoutine id
        """
        ticket_id = self._request_client.getAttachedReduceRoutine(p_id, j_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.getAttachedReduceRoutine),
            loop=self._loop
        )
        return future

    def attach_reduce_routine(self, p_id, j_id, reduce_routine_id):
        """
        Attach/Assign a ReduceRoutine to the given Job. All Task result will be reduced with the given Routine.
        :param p_id: Program id
        :param j_id: Job id
        :param reduce_routine_id: MapRoutine id
        :return:
        """
        ticket_id = self._request_client.attachReduceRoutine(p_id, j_id, reduce_routine_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def mark_job_as_complete(self, p_id, j_id):
        """
        Mark a specified Job as complete, this means all Tasks were submitted.
        :param p_id: Program id
        :param j_id: Job id
        :return:
        """
        ticket_id = self._request_client.markJobAsComplete(p_id, j_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def abort_job(self, p_id, j_id):
        """
        Abort a given Job and all attached Tasks.
        :param p_id: Program id
        :param j_id: Job id
        :return:
        """
        ticket_id = self._request_client.abortJob(p_id, j_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def get_all_tasks(self, p_id, j_id, sorting = SortingCriterion.NO_SORTING):
        """
        Returns a list of all attached Task ids.
        :param p_id: Program id
        :param j_id: Job id
        :param sorting: Sorting of the returned list
        :return: List of Task ids
        """
        ticket_id = self._request_client.getAllTasks(p_id, j_id, sorting)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.getAllTasks),
            loop=self._loop
        )
        return future

    def get_all_tasks_with_state(self, p_id, j_id, state, sorting = SortingCriterion.NO_SORTING):
        """
        Returns a list of all attached Task ids which match with the given state.
        :param p_id: Program id
        :param j_id: Job id
        :param state: Task state
        :param sorting: Sorting of the returned list
        :return: List of Task ids
        """
        ticket_id = self._request_client.getAllTasksWithState(p_id, j_id, state, sorting)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.getAllTasksWithState),
            loop=self._loop
        )
        return future

    def create_task(self, p_id, j_id, routine_instance):
        """
        Create a new Task in given Job.
        :param p_id: Program id
        :param j_id: Job id
        :param routine_instance: Routine of Task
        :return: Task id
        """
        ticket_id = self._request_client.createTask(p_id, j_id, routine_instance)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future, self._response_client.createTask), loop=self._loop)
        return future

    def get_task(self, p_id, j_id, t_id):
        """
        Returns the meta data and all attached Resources (in- and out parameters) of the specified Task.
        :param p_id: Program id
        :param j_id: Job id
        :param t_id: Task id
        :return: TaskDTO
        """
        ticket_id = self._request_client.getTask(p_id, j_id, t_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future, self._response_client.getTask), loop=self._loop)
        return future

    def get_task_partial(self, p_id, j_id, t_id, include_in_parameters = False, include_out_parameters = True):
        """
        Returns the meta data and a part of attached Resources (in- and out parameters) of the specified Task.
        :param p_id: Program id
        :param j_id: Job id
        :param t_id: Task id
        :param include_in_parameters: TaskDTO includes in-parameters or not
        :param include_out_parameters: TaskDTO includes out-parameters or not
        :return: TaskDTO
        """
        ticket_id = self._request_client.getTaskPartial(p_id, j_id, t_id, include_in_parameters, include_out_parameters)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future, self._response_client.getTask), loop=self._loop)
        return future

    def re_run_task(self, p_id, j_id, t_id):
        """
        Restart/Rerun the specified Task.
        :param p_id: Program id
        :param j_id: Job id
        :param t_id: Task id
        :return:
        """
        ticket_id = self._request_client.reRunTask(p_id, j_id, t_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future),
            loop=self._loop
        )
        return future

    def abort_task(self, p_id, j_id, t_id):
        """
        Abort the given Task. The state of the given Task will be set to failed.
        :param p_id: Program id.
        :param j_id: Job id.
        :param t_id: Task id.
        :return:
        """
        ticket_id = self._request_client.abortTask(p_id, j_id, t_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def get_all_shared_resources(self, p_id):
        """
        Returns a list of SharedResource ids attached to the given Program.
        :param p_id: Program id
        :return: List of SharedResource ids
        """
        ticket_id = self._request_client.getAllSharedResources(p_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.getAllSharedResources),
            loop=self._loop
        )
        return future

    def create_shared_resource(self, p_id, value):
        """
        Create a SharedResource in a given Program.
        :param p_id: Program id
        :param value: Shared resource value, DataType value
        :return: SharedResource id.
        """
        data_type_id = getattr(value, '_id')
        data = TSerialization.serialize(value)
        ticket_id = self._request_client.createSharedResource(p_id, data_type_id, data)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.createSharedResource),
            loop=self._loop
        )
        return future

    def get_shared_resource(self, p_id, r_id):
        """
        Returns the specified SharedResource including meta information and data.
        :param p_id: Program id
        :param r_id: SharedResource id
        :return: ResourceDTO
        """
        ticket_id = self._request_client.getSharedResource(p_id, r_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(
            self.__wait_for_ticket(ticket_id, future, self._response_client.getSharedResource),
            loop=self._loop
        )
        return future

    def delete_shared_resource(self, p_id, r_id):
        """
        Delete a specified SharedResource.
        :param p_id: Program id.
        :param r_id: SharedResource id.
        :return:
        """
        ticket_id = self._request_client.deleteSharedResource(p_id, r_id)
        future = asyncio.Future(loop=self._loop)
        asyncio.ensure_future(self.__wait_for_ticket(ticket_id, future), loop=self._loop)
        return future

    def wait_for_job_finished(self, p_id, j_id, poll_delay = 1):
        while 1:
            future = self.get_job(p_id, j_id)
            self._loop.run_until_complete(future)
            job = future.result()
            if job.state == ExecutionState.FAILED or job.state == ExecutionState.SUCCESS:
                return job.state
            else:
                sleep(poll_delay)
