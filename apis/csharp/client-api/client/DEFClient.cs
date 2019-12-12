using System;
using System.IO;
using System.Threading.Tasks;
using System.Collections.Generic;
using common.execlogic;
using common.manager;
using Thrift.Protocol;

namespace client_api.client
{
    public class DEFClient
    {
        private ExecLogicClient execLogicClient;
        private ManagerClient managerClient;

        public DEFClient(String host, int port, Protocol protocol)
        {
            this.execLogicClient = new ExecLogicClient(host, port, protocol);
            this.managerClient = new ManagerClient(host, port, protocol);
        }

        public DEFClient(ServiceEndpointDTO endpoint) : this(endpoint.Host, endpoint.Port, endpoint.Protocol) { }

        /**
        * ---------------------------------------------------------------------
        * PROGRAM METHODS
        * ---------------------------------------------------------------------
        */

        public Task<List<String>> GetAllPrograms(String userId)
        {
            return execLogicClient.GetAllPrograms(userId);
        }

        public Task<String> CreateProgram(String cId, String userId)
        {
            return execLogicClient.CreateProgram(cId, userId);
        }

        public Task<ProgramDTO> GetProgram(String pId)
        {
            return execLogicClient.GetProgram(pId);
        }

        public Task<TicketStatusDTO> DeleteProgram(String pId)
        {
            ProgramDTO program = GetProgram(pId).Result;
            if (program.ClientRoutineId != null && !program.ClientRoutineId.Equals(""))
            {
                TicketStatusDTO state = managerClient.RemoveClientRoutine(program.ClientRoutineId).Result;
            }
            return execLogicClient.DeleteProgram(pId);
        }

        public Task<TicketStatusDTO> AbortProgram(String pId)
        {
            return execLogicClient.AbortProgram(pId);
        }

        public Task<TicketStatusDTO> UpdateProgramName(String pId, String name)
        {
            return execLogicClient.UpdateProgramName(pId, name);
        }

        public Task<TicketStatusDTO> UpdateProgramDescription(String pId, String description)
        {
            return execLogicClient.UpdateProgramDescription(pId, description);
        }

        public Task<TicketStatusDTO> MarkProgramAsFinished(String pId)
        {
            return execLogicClient.MarkProgramAsFinished(pId);
        }

        public Task<TicketStatusDTO> StartClientRoutine(String pId, String crId)
        {
            return execLogicClient.StartClientRoutine(pId, crId);
        }

        public Task<TicketStatusDTO> AttachAndStartClientRoutine(String pId, List<string> fileBinaries, List<FeatureDTO> requiredFeatures, List<String> arguments)
        {
            // Create new client routine
            String name = String.Format("ClientRoutine for Program {0}", pId);
            RoutineDTO routine = new RoutineDTO();
            routine.Name = name;
            routine.RequiredFeatures = requiredFeatures;
            routine.Arguments = arguments;
            String rId = managerClient.CreateClientRoutine(routine).Result;

            // Upload binaries
            bool primary = true;
            foreach (string binaryFilePath in fileBinaries)
            {
                byte[] bytes = File.ReadAllBytes(binaryFilePath);
                string fileName = Path.GetFileName(binaryFilePath);
                String rbId = managerClient.UploadClientRoutineBinary(rId, fileName, primary, bytes).Result;
                primary = false;
            }

            // Attach and start client routine
            return execLogicClient.StartClientRoutine(pId, rId);
        }

        public Task<List<String>> GetAllSharedResources(String pId)
        {
            return execLogicClient.GetAllSharedResources(pId);
        }

        public Task<String> CreateSharedResource<T>(String pId, String dataTypeId, T value) where T : TBase
        {
            return execLogicClient.CreateSharedResource(pId, dataTypeId, value);
        }

        public Task<ResourceDTO> GetSharedResource(String pId, String rId)
        {
            return execLogicClient.GetSharedResource(pId, rId);
        }

        public Task<TicketStatusDTO> DeleteSharedResource(String pId, String rId)
        {
            return execLogicClient.DeleteSharedResource(pId, rId);
        }

        public ProgramDTO WaitForProgram(String pId)
        {
            return execLogicClient.WaitForProgram(pId);
        }

        public T ExtractResult<T>(ProgramDTO program, String key) where T : TBase
        {
            return execLogicClient.ExtractResult<T>(program, key);
        }

        public Dictionary<String, T> ExtractResults<T>(ProgramDTO program) where T : TBase
        {
            return execLogicClient.ExtractResults<T>(program);
        }


        /**
        * ---------------------------------------------------------------------
        * JOB METHODS
        * ---------------------------------------------------------------------
        */

        public Task<List<String>> GetAllJobs(String pId)
        {
            return execLogicClient.GetAllJobs(pId);
        }

        public Task<String> CreateJob(String pId)
        {
            return execLogicClient.CreateJob(pId);
        }

        public Task<JobDTO> GetJob(String pId, String jId)
        {
            return execLogicClient.GetJob(pId, jId);
        }

        public Task<TicketStatusDTO> DeleteJob(String pId, String jId)
        {
            return execLogicClient.DeleteJob(pId, jId);
        }

        public Task<String> GetAttachedMapRoutine(String pId, String jId)
        {
            return execLogicClient.GetAttachedMapRoutine(pId, jId);
        }

        public Task<TicketStatusDTO> AttachMapRoutine(String pId, String jId, String mapRoutineId)
        {
            return execLogicClient.AttachMapRoutine(pId, jId, mapRoutineId);
        }

        public Task<String> GetAttachedReduceRoutine(String pId, String jId)
        {
            return execLogicClient.GetAttachedReduceRoutine(pId, jId);
        }

        public Task<TicketStatusDTO> AttachReduceRoutine(String pId, String jId, String reduceRoutineId)
        {
            return execLogicClient.AttachReduceRoutine(pId, jId, reduceRoutineId);
        }

        public Task<TicketStatusDTO> MarkJobAsComplete(String pId, String jId)
        {
            return execLogicClient.MarkJobAsComplete(pId, jId);
        }

        public Task<TicketStatusDTO> AbortJob(String pId, String jId)
        {
            return execLogicClient.AbortJob(pId, jId);
        }

        public JobDTO WaitForJob(String pId, String jId)
        {
            return execLogicClient.WaitForJob(pId, jId);
        }

        public T ExtractReducedResult<T>(JobDTO job) where T : TBase
        {
            return execLogicClient.ExtractReducedResult<T>(job);
        }

        public T ExtractReducedResult<T>(JobDTO job, String key) where T : TBase
        {
            return execLogicClient.ExtractReducedResult<T>(job, key);
        }


        /**
        * ---------------------------------------------------------------------
        * TASK METHODS
        * ---------------------------------------------------------------------
        */

        public Task<List<String>> GetAllTasks(String pId, String jId, SortingCriterion sortingCriterion)
        {
            return execLogicClient.GetAllTasks(pId, jId, sortingCriterion);
        }

        public Task<List<String>> GetAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion)
        {
            return execLogicClient.GetAllTasksWithState(pId, jId, state, sortingCriterion);
        }

        public Task<String> CreateTask(String pId, String jId, RoutineInstanceDTO routineInstance)
        {
            return execLogicClient.CreateTask(pId, jId, routineInstance);
        }

        public Task<TaskDTO> GetTask(String pId, String jId, String tId)
        {
            return execLogicClient.GetTask(pId, jId, tId);
        }

        public Task<TaskDTO> GetTask(String pId, String jId, String tId, bool includeInParameters, bool includeOutParameters)
        {
            return execLogicClient.GetTask(pId, jId, tId, includeInParameters, includeOutParameters);
        }

        public T ExtractOutParameter<T>(TaskDTO task) where T : TBase
        {
            return execLogicClient.ExtractOutParameter<T>(task);
        }

        public T ExtractOutParameter<T>(TaskDTO task, String key) where T : TBase
        {
            return execLogicClient.ExtractOutParameter<T>(task, key);
        }

        public Task<TicketStatusDTO> AbortTask(String pId, String jId, String tId)
        {
            return execLogicClient.AbortTask(pId, jId, tId);
        }

        public Task<TicketStatusDTO> ReRunTask(String pId, String jId, String tId)
        {
            return execLogicClient.ReRunTask(pId, jId, tId);
        }

        /**
        * ---------------------------------------------------------------------
        * MANAGER METHODS
        * ---------------------------------------------------------------------
        */

        public Task<List<String>> GetClusterIds()
        {
            return managerClient.GetClusterIds();
        }

        public Task<ClusterInfoDTO> GetClusterInfo(String cId)
        {
            return managerClient.GetClusterInfo(cId);
        }

        public Task<ServiceEndpointDTO> GetClusterEndpoint(String cId)
        {
            return managerClient.GetClusterEndpoint(cId);
        }

        public Task<String> CreateAWSCluster(int nrOfWorkers, int nrOfReducers, AWSSpecificationDTO specification)
        {
            return managerClient.CreateAWSCluster(nrOfWorkers, nrOfReducers, specification);
        }

        public Task<TicketStatusDTO> AddCluster(ServiceEndpointDTO endpoint)
        {
            return managerClient.AddCluster(endpoint);
        }

        public Task<TicketStatusDTO> DeleteCluster(String cId)
        {
            return managerClient.DeleteCluster(cId);
        }

        public Task<TicketStatusDTO> AdjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType)
        {
            return managerClient.AdjustNodePoolSize(cId, newNodePoolSize, nodeType);
        }

        public Task<String> CreateClientRoutine(RoutineDTO routine)
        {
            return managerClient.CreateClientRoutine(routine);
        }

        public Task<String> UploadClientRoutineBinary(String rId, string binaryName, bool isPrimary, byte[] data)
        {
            return managerClient.UploadClientRoutineBinary(rId, binaryName, isPrimary, data);
        }

        public Task<TicketStatusDTO> RemoveClientRoutine(String rId)
        {
            return managerClient.RemoveClientRoutine(rId);
        }

        public Task<FeatureDTO> GetFeatureByNameAndVersion(String name, String version)
        {
            return managerClient.GetFeatureByNameAndVersion(name, version);
        }
    }
}
