using System;
using System.Threading;
using System.Threading.Tasks;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using Thrift.Protocol;
using Thrift.Transport;
using common.thrift;
namespace client_api
{
    public class DEFClient
    {
        // exec logic service
        private ExecLogicService.Iface execLogicRequestClient;
        private ExecLogicResponseService.Iface execLogicResponseClient;
        private ManagerService.Iface managerRequestClient;                 
        private ManagerResponseService.Iface managerResponseClient;       
        private TicketService.Iface ticketService;

        private static String THRIFT_TICKET_SERVICE_NAME = "/at.enfilo.def.communication.api.ticket.thrift.TicketService";
        private static String THRIFT_EXECLOGIC_SERVICE_NAME_REQUEST = "/at.enfilo.def.execlogic.api.thrift.ExecLogicService";
        private static String THRIFT_EXECLOGIC_SERVICE_NAME_RESPONSE = "/at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService";
        private static String THRIFT_MANAGER_SERVICE_NAME_REQUEST = "/at.enfilo.def.manager.api.thrift.ManagerService";
        private static String THRIFT_MANAGER_SERVER_NAME_RESPONSE = "/at.enfilo.def.manager.api.thrift.ManagerResponseService"; 

        private static int POLL_DELAY = 200;
        private static String DEFAULT_KEY = "DEFAULT";

        private readonly object ticketLock = new object();
        private readonly object execLogicLock = new object();
        private readonly object managerLock = new object();

        public DEFClient(String host, int port, Protocol protocol)
        {
            switch (protocol)
            {
                case Protocol.THRIFT_TCP:
                    // ticket service
                    TcpClient ticketClient = new TcpClient(host, port);
                    TTransport ticketTransport = new TSocket(ticketClient); 
                    TBufferedTransport ticketBufferedTransport = new TBufferedTransport(ticketTransport);
                    TProtocol ticketProtocol = new TBinaryProtocol(ticketBufferedTransport);
                    TProtocol ticketTcpProtocol = new TMultiplexedProtocol(ticketProtocol, THRIFT_TICKET_SERVICE_NAME);
                    this.ticketService = new TicketService.Client(ticketTcpProtocol);
                    //ticketTransport.Open();

                    // exec logic service
                    TcpClient execLogicClient = new TcpClient(host, port);
                    TSocket execLogicTransport = new TSocket(execLogicClient);
                    TBufferedTransport execLogicBufferedTransport = new TBufferedTransport(execLogicTransport);
                    TProtocol execLocigProtocol = new TBinaryProtocol(execLogicBufferedTransport);
                    TProtocol execLogicTcpProtocolRequest = new TMultiplexedProtocol(execLocigProtocol, THRIFT_EXECLOGIC_SERVICE_NAME_REQUEST);
                    TProtocol execLogicTcpProtocolResponse = new TMultiplexedProtocol(execLocigProtocol, THRIFT_EXECLOGIC_SERVICE_NAME_RESPONSE);
                    this.execLogicRequestClient = new ExecLogicService.Client(execLogicTcpProtocolRequest);
                    this.execLogicResponseClient = new ExecLogicResponseService.Client(execLogicTcpProtocolResponse);
                    //execLogicTransport.Open();

                    // manager logic service
                    TcpClient managerClient = new TcpClient(host, port);
                    TSocket managerTransport = new TSocket(managerClient);
                    TBufferedTransport managerBufferedTransport = new TBufferedTransport(managerTransport);
                    TProtocol managerProtocol = new TBinaryProtocol(managerBufferedTransport);
                    TProtocol managerTcpProtocolRequest = new TMultiplexedProtocol(managerProtocol, THRIFT_MANAGER_SERVICE_NAME_REQUEST);
                    TProtocol managerTcpProtocolResponse = new TMultiplexedProtocol(managerProtocol, THRIFT_MANAGER_SERVER_NAME_RESPONSE);
                    this.managerRequestClient = new ManagerService.Client(managerTcpProtocolRequest);
                    this.managerResponseClient = new ManagerResponseService.Client(managerTcpProtocolResponse);
                    //managerTransport.Open();

                    break;

                case Protocol.THRIFT_HTTP:
                    throw new NotImplementedException();
                    // TODO testen
                    //// ticket service
                    //THttpClient transportTicket = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api/", host, port, THRIFT_TICKET_SERVICE_NAME)));
                    //TProtocol httpProtocolTicket = new TBinaryProtocol(transportTicket);
                    //this.ticketService = new TicketService.Client(httpProtocolTicket);
                    //transportTicket.Open();

                    //// exec logic service
                    //THttpClient execLogicTransportRequest = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api/", host, port, THRIFT_EXECLOGIC_SERVICE_NAME_REQUEST)));
                    //THttpClient execLogicTransportResponse = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api/", host, port, THRIFT_EXECLOGIC_SERVICE_NAME_RESPONSE)));
                    //TProtocol execLogicHttpProtocolRequest = new TBinaryProtocol(execLogicTransportRequest);
                    //TProtocol execLogicHttpProtocolResponse = new TBinaryProtocol(execLogicTransportResponse);

                    //this.execLogicRequestClient = new ExecLogicService.Client(execLogicHttpProtocolRequest);
                    //this.execLogicResponseClient = new ExecLogicResponseService.Client(execLogicHttpProtocolResponse);

                    //execLogicTransportRequest.Open();
                    //execLogicTransportResponse.Open();

                    //// manager logic service
                    //THttpClient managerTransportRequest = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api/", host, port, THRIFT_MANAGER_SERVICE_NAME_REQUEST)));
                    //THttpClient managerTransportResponse = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api/", host, port, THRIFT_MANAGER_SERVER_NAME_RESPONSE)));
                    //TProtocol managerHttpProtocolRequest = new TBinaryProtocol(managerTransportRequest);
                    //TProtocol managerHttpProtocolResponse = new TBinaryProtocol(managerTransportResponse);

                    //this.managerRequestClient = new ManagerService.Client(managerHttpProtocolRequest);
                    //this.managerResponseClient = new ManagerResponseService.Client(managerHttpProtocolResponse);

                    //managerTransportRequest.Open();
                    //managerTransportResponse.Open();
                    //break;

                case Protocol.REST:
                    throw new NotImplementedException();
            }
        }

        internal Task<TicketStatusDTO> FetchTicketStatusTask(String ticketId)
        {
            var t = Task<TicketStatusDTO>.Run(() =>
            {
                return FetchTicketStatus(ticketId);
            });
            return t;
        }

        internal TicketStatusDTO FetchTicketStatus(String ticketId)
        {
            return this.ticketService.waitForTicket(ticketId);
        }

        internal Task<T> FetchResult<T>(String ticketId, Func<string, T> method, object lockObject)
        {
            //System.Console.WriteLine("Fetch result - ticket id: " + ticketId);
            var t = Task<T>.Run(() =>
            {
                TicketStatusDTO state = this.FetchTicketStatus(ticketId);

                if (state == TicketStatusDTO.DONE)
                {
                    //System.Console.WriteLine("Execute method");
                    lock (lockObject)
                    {
                        return method(ticketId);
                    }
                }
                else
                {
                    throw new Exception(String.Format("Error while fetching result from response client: {0}", this.ticketService.getFailedMessage(ticketId)));

                }
            });
            return t;
        }

        public Task<List<String>> GetAllPrograms(String userId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getAllPrograms(userId);
            } 
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllPrograms, execLogicLock);
        }

        public Task<String> CreateProgram(String cId, String userId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.createProgram(cId, userId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createProgram, execLogicLock);
        }

        public Task<ProgramDTO> GetProgram(String pId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getProgram(pId);
            }
            return FetchResult<ProgramDTO>(ticketId, this.execLogicResponseClient.getProgram, execLogicLock);
        }

        public Task<TicketStatusDTO> DeleteProgram(String pId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.deleteProgram(pId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> AbortProgram(String pId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.abortProgram(pId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> UpdateProgramName(String pId, String name)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.updateProgramName(pId, name);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> UpdateProgramDescription(String pId, String description)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.updateProgramDescription(pId, description);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> MarkProgramAsFinished(String pId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.markProgramAsFinished(pId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<List<String>> GetAllJobs(String pId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getAllJobs(pId);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllJobs, execLogicLock);
        }

        public Task<String> CreateJob(String pId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.createJob(pId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createJob, execLogicLock);
        }

        public Task<JobDTO> GetJob(String pId, String jId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getJob(pId, jId);
            }
            return FetchResult<JobDTO>(ticketId, this.execLogicResponseClient.getJob, execLogicLock);
        }

        public Task<TicketStatusDTO> DeleteJob(String pId, String jId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.deleteJob(pId, jId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<String> GetAttachedMapRoutine(String pId, String jId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getAttachedMapRoutine(pId, jId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.getAttachedMapRoutine, execLogicLock);
        }

        public Task<TicketStatusDTO> AttachMapRoutine(String pId, String jId, String mapRoutineId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.attachMapRoutine(pId, jId, mapRoutineId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<String> GetAttachedReduceRoutine(String pId, String jId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getAttachedReduceRoutine(pId, jId);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.getAttachedReduceRoutine, execLogicLock);
        }

        public Task<TicketStatusDTO> AttachReduceRoutine(String pId, String jId, String reduceRoutineId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.attachReduceRoutine(pId, jId, reduceRoutineId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<List<String>> GetAllTasks(String pId, String jId, SortingCriterion sortingCriterion)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getAllTasks(pId, jId, sortingCriterion);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllTasks, execLogicLock);
        }

        public Task<List<String>> GetAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getAllTasksWithState(pId, jId, state, sortingCriterion);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllTasksWithState, execLogicLock);
        }

        public Task<String> CreateTask(String pId, String jId, RoutineInstanceDTO routineInstance)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.createTask(pId, jId, routineInstance);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createTask, execLogicLock);
        }

        public Task<TaskDTO> GetTask(String pId, String jId, String tId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getTask(pId, jId, tId);
            }
            return FetchResult<TaskDTO>(ticketId, this.execLogicResponseClient.getTask, execLogicLock);
        }

        public Task<TaskDTO> GetTask(String pId, String jId, String tId, bool includeInParameters, bool includeOutParameters)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getTaskPartial(pId, jId, tId, includeInParameters, includeOutParameters);
            }
            return FetchResult<TaskDTO>(ticketId, this.execLogicResponseClient.getTaskPartial, execLogicLock);
        }

        public T ExtractOutParameter<T>(TaskDTO task) where T:TBase
        {
            return ExtractOutParameter<T>(task, DEFAULT_KEY);
        }

        public T ExtractOutParameter<T>(TaskDTO task, String key) where T:TBase
        {
            foreach (ResourceDTO resouce in task.OutParameters) {
                if (key.Equals(resouce.Key, StringComparison.InvariantCultureIgnoreCase))
                {
                    return ExtractValueFromResource<T>(resouce);
                }
            }
            throw new Exception(String.Format("Parameter with key {0} not found.", key));
        }

        public T ExtractReducedResult<T>(JobDTO job) where T:TBase
        {
            return ExtractReducedResult<T>(job, DEFAULT_KEY);
        }

        public T ExtractReducedResult<T>(JobDTO job, String key) where T:TBase
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

        public T ExtractValueFromResource<T>(ResourceDTO resource) where T:TBase
        {
            return ThriftSerializer.Deserialize<T>(resource.Data);
        }

        public Task<TicketStatusDTO> MarkJobAsComplete(String pId, String jId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.markJobAsComplete(pId, jId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> AbortJob(String pId, String jId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.abortJob(pId, jId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> AbortTask(String pId, String jId, String tId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.abortTask(pId, jId, tId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> ReRunTask(String pId, String jId, String tId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.reRunTask(pId, jId, tId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<List<String>> GetAllSharedResources(String pId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getAllSharedResources(pId);
            }
            return FetchResult<List<String>>(ticketId, this.execLogicResponseClient.getAllSharedResources, execLogicLock);
        }

        public Task<String> CreateSharedResource<T>(String pId, String dataTypeId, T value) where T:TBase
        {
            byte[] data = ThriftSerializer.Serialize<T>(value);
            return CreateSharedResource(pId, dataTypeId, data);
        }

        private Task<String> CreateSharedResource(String pId, String dataTypeId, byte[] data)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.createSharedResource(pId, dataTypeId, data);
            }
            return FetchResult<String>(ticketId, this.execLogicResponseClient.createSharedResource, execLogicLock);
        }

        public Task<ResourceDTO> GetSharedResource(String pId, String rId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.getSharedResource(pId, rId);
            }
            return FetchResult<ResourceDTO>(ticketId, this.execLogicResponseClient.getSharedResource, execLogicLock);
        }

        public Task<TicketStatusDTO> DeleteSharedResource(String pId, String rId)
        {
            String ticketId = null;
            lock (execLogicLock)
            {
                ticketId = this.execLogicRequestClient.deleteSharedResource(pId, rId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<List<String>> GetClusterIds()
        {
            String ticketId = null;
            lock (managerLock)
            {
                ticketId = this.managerRequestClient.getClusterIds();
            }
            return FetchResult<List<String>>(ticketId, this.managerResponseClient.getClusterIds, managerLock);
        }

        public Task<ClusterInfoDTO> GetClusterInfo(String cId)
        {
            String ticketId = null;
            lock (managerLock)
            {
                ticketId = this.managerRequestClient.getClusterInfo(cId);
            }
            return FetchResult<ClusterInfoDTO>(ticketId, this.managerResponseClient.getClusterInfo, managerLock);
        }

        public Task<ServiceEndpointDTO> GetClusterEndpoint(String cId)
        {
            String ticketId = null;
            lock (managerLock)
            {
                ticketId = this.managerRequestClient.getClusterEndpoint(cId);
            }
            return FetchResult<ServiceEndpointDTO>(ticketId, this.managerResponseClient.getClusterEndpoint, managerLock);
        }

        public Task<String> CreateAWSCluster(int nrOfWorkers, int nrOfReducers, AWSSpecificationDTO specification)
        {
            String ticketId = null;
            lock (managerLock)
            {
                ticketId = this.managerRequestClient.createAWSCluster(nrOfWorkers, nrOfReducers, specification);
            }
            return FetchResult<String>(ticketId, this.managerResponseClient.createAWSCluster, managerLock);
        }

        public Task<TicketStatusDTO> AddCluster(ServiceEndpointDTO endpoint)
        {
            String ticketId = null;
            lock (managerLock)
            {
                ticketId = this.managerRequestClient.addCluster(endpoint);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> DeleteCluster(String cId)
        {
            String ticketId = null;
            lock (managerLock)
            {
                ticketId = this.managerRequestClient.destroyCluster(cId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> AdjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType)
        {
            String ticketId = null;
            lock (managerLock)
            {
                ticketId = this.managerRequestClient.adjustNodePoolSize(cId, newNodePoolSize, nodeType);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public JobDTO WaitForJob(String pId, String jId)
        {
            while(true)
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

    }
}
