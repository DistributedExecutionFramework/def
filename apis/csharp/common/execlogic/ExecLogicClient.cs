using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Thrift.Protocol;
using Thrift.Transport;
using common.client;
using common.thrift;
using common.util;

namespace common.execlogic
{
    public class ExecLogicClient : AbstractClient
    {
        private ExecLogicService.Iface execLogicRequestClient;
        private ExecLogicResponseService.Iface execLogicResponseClient;

        private static String THRIFT_EXECLOGIC_SERVICE_NAME_REQUEST = "/at.enfilo.def.execlogic.api.thrift.ExecLogicService";
        private static String THRIFT_EXECLOGIC_SERVICE_NAME_RESPONSE = "/at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService";

        private static string DEFAULT_KEY = "DEFAULT";

        public ExecLogicClient(ServiceEndpointDTO endpoint) : this(endpoint.Host, endpoint.Port, endpoint.Protocol) { }

        public ExecLogicClient(String host, int port, Protocol protocol) : base(host, port, protocol, THRIFT_EXECLOGIC_SERVICE_NAME_REQUEST, THRIFT_EXECLOGIC_SERVICE_NAME_RESPONSE)
        {
            this.execLogicRequestClient = new ExecLogicService.Client(requestProtocol);
            this.execLogicResponseClient = new ExecLogicResponseService.Client(responseProtocol);
        }

        /**
         * ---------------------------------------------------------------------
         * HELPER METHODS
         * ---------------------------------------------------------------------
         */

        private T ExtractValueFromResource<T>(ResourceDTO resource) where T : TBase
        {
            return ThriftSerializer.Deserialize<T>(resource.Data);
        }

        /**
        * ---------------------------------------------------------------------
        * PROGRAM METHODS
        * ---------------------------------------------------------------------
        */

        public Task<List<String>> GetAllPrograms(String userId)
        {
            String ticketId = null;
            lock (clientLock) 
            {
                ticketId = this.execLogicRequestClient.getAllPrograms(userId);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllPrograms);
        }

        public Task<String> CreateProgram(String cId, String userId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.createProgram(cId, userId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createProgram);
        }
       
        public Task<ProgramDTO> GetProgram(String pId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getProgram(pId);
            }
            return FetchResult<ProgramDTO>(ticketId, this.execLogicResponseClient.getProgram);
        }

        public Task<TicketStatusDTO> DeleteProgram(String pId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.deleteProgram(pId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> AbortProgram(String pId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.abortProgram(pId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> UpdateProgramName(String pId, String name)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.updateProgramName(pId, name);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> UpdateProgramDescription(String pId, String description)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.updateProgramDescription(pId, description);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> StartClientRoutine(String pId, String crId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.startClientRoutine(pId, crId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> MarkProgramAsFinished(String pId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.markProgramAsFinished(pId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<List<String>> GetAllSharedResources(String pId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getAllSharedResources(pId);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllSharedResources);
        }

        public Task<String> CreateSharedResource<T>(String pId, String dataTypeId, T value) where T : TBase
        { 
            byte[] data = ThriftSerializer.Serialize<T>(value);
            return CreateSharedResource(pId, dataTypeId, data);
        }

        private Task<String> CreateSharedResource(String pId, String dataTypeId, byte[] data)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.createSharedResource(pId, dataTypeId, data);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createSharedResource);
        }

        public Task<ResourceDTO> GetSharedResource(String pId, String rId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getSharedResource(pId, rId);
            }
            return FetchResult<ResourceDTO>(ticketId, this.execLogicResponseClient.getSharedResource);
        }

        public Task<TicketStatusDTO> DeleteSharedResource(String pId, String rId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.deleteSharedResource(pId, rId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public ProgramDTO WaitForProgram(String pId)
        {
            while (true)
            {
                Thread.Sleep(POLL_DELAY);
                Task<ProgramDTO> t = GetProgram(pId);
                ProgramDTO program = t.Result;
                if (program.State == ExecutionState.FAILED || program.State == ExecutionState.SUCCESS)
                {
                    return program;
                }
            }
        }

        public T ExtractResult<T>(ProgramDTO program, String key) where T : TBase
        {
            foreach (KeyValuePair<string, ResourceDTO> entry in program.Results)
            {
                if (entry.Key.Equals(key, StringComparison.InvariantCultureIgnoreCase))
                {
                    return ExtractValueFromResource<T>(entry.Value);
                }
            }
            throw new Exception(String.Format("Result with key {0} not found", key));
        }

        public Dictionary<String, T> ExtractResults<T>(ProgramDTO program) where T : TBase
        {
            Dictionary<String, T> results = new Dictionary<String, T>();
            foreach (KeyValuePair<string, ResourceDTO> entry in program.Results)
            {
                results.Add(entry.Key, ExtractValueFromResource<T>(entry.Value));
            }
            return results;
        }

        /**
        * ---------------------------------------------------------------------
        * JOB METHODS
        * ---------------------------------------------------------------------
        */

        public Task<List<String>> GetAllJobs(String pId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getAllJobs(pId);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllJobs);
        }

        public Task<String> CreateJob(String pId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.createJob(pId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createJob);
        }

        public Task<JobDTO> GetJob(String pId, String jId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getJob(pId, jId);
            }
            return FetchResult<JobDTO>(ticketId, this.execLogicResponseClient.getJob);
        }

        public Task<TicketStatusDTO> DeleteJob(String pId, String jId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.deleteJob(pId, jId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<String> GetAttachedMapRoutine(String pId, String jId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getAttachedMapRoutine(pId, jId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.getAttachedMapRoutine);
        }

        public Task<TicketStatusDTO> AttachMapRoutine(String pId, String jId, String mapRoutineId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.attachMapRoutine(pId, jId, mapRoutineId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<String> GetAttachedReduceRoutine(String pId, String jId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getAttachedReduceRoutine(pId, jId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.getAttachedReduceRoutine);
        }

        public Task<TicketStatusDTO> AttachReduceRoutine(String pId, String jId, String reduceRoutineId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.attachReduceRoutine(pId, jId, reduceRoutineId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> MarkJobAsComplete(String pId, String jId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.markJobAsComplete(pId, jId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> AbortJob(String pId, String jId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.abortJob(pId, jId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public JobDTO WaitForJob(String pId, String jId)
        {
            while (true)
            {
                Thread.Sleep(POLL_DELAY);
                Task<JobDTO> t = GetJob(pId, jId);
                JobDTO job = t.Result;
                if (job.State == ExecutionState.FAILED || job.State == ExecutionState.SUCCESS)
                {
                    return job;
                }
            }
        }

        public T ExtractReducedResult<T>(JobDTO job) where T : TBase
        {
            return ExtractReducedResult<T>(job, DEFAULT_KEY);
        }

        public T ExtractReducedResult<T>(JobDTO job, String key) where T : TBase
        {
            foreach (ResourceDTO resource in job.ReducedResults)
            {
                if (key.Equals(resource.Key, StringComparison.InvariantCultureIgnoreCase))
                {
                    return ExtractValueFromResource<T>(resource);
                }
            }
            throw new Exception(String.Format("Result with key {0} not found", key));
        }


        /**
        * ---------------------------------------------------------------------
        * TASK METHODS
        * ---------------------------------------------------------------------
        */

        public Task<List<String>> GetAllTasks(String pId, String jId, SortingCriterion sortingCriterion)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getAllTasks(pId, jId, sortingCriterion);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllTasks);
        }

        public Task<List<String>> GetAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getAllTasksWithState(pId, jId, state, sortingCriterion);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllTasksWithState);
        }

        public Task<String> CreateTask(String pId, String jId, RoutineInstanceDTO routineInstance)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.createTask(pId, jId, routineInstance);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createTask);
        }

        public Task<TaskDTO> GetTask(String pId, String jId, String tId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getTask(pId, jId, tId);
            }
            return FetchResult<TaskDTO>(ticketId, this.execLogicResponseClient.getTask);
        }

        public Task<TaskDTO> GetTask(String pId, String jId, String tId, bool includeInParameters, bool includeOutParameters)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.getTaskPartial(pId, jId, tId, includeInParameters, includeOutParameters);
            }
            return FetchResult<TaskDTO>(ticketId, this.execLogicResponseClient.getTaskPartial);
        }

        public Task<TicketStatusDTO> AbortTask(String pId, String jId, String tId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.abortTask(pId, jId, tId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> ReRunTask(String pId, String jId, String tId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.execLogicRequestClient.reRunTask(pId, jId, tId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public T ExtractOutParameter<T>(TaskDTO task) where T : TBase
        {
            return ExtractOutParameter<T>(task, DEFAULT_KEY);
        }

        public T ExtractOutParameter<T>(TaskDTO task, String key) where T : TBase
        {
            foreach (ResourceDTO resouce in task.OutParameters)
            {
                if (key.Equals(resouce.Key, StringComparison.InvariantCultureIgnoreCase))
                {
                    return ExtractValueFromResource<T>(resouce);
                }
            }
            throw new Exception(String.Format("Parameter with key {0} not found.", key));
        }
    }
}
