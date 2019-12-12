using System;
using System.Threading;
using System.Threading.Tasks;
using System.Collections.Generic;
using System.Net.Sockets;
using Thrift.Protocol;
using Thrift.Transport;
using common.client;
using common.util;

namespace common.manager
{
    public class ManagerClient : AbstractClient
    {
        private ManagerService.Iface managerRequestClient;
        private ManagerResponseService.Iface managerResponseClient;

        private static String THRIFT_MANAGER_SERVICE_NAME_REQUEST = "/at.enfilo.def.manager.api.thrift.ManagerService";
        private static String THRIFT_MANAGER_SERVICE_NAME_RESPONSE = "/at.enfilo.def.manager.api.thrift.ManagerResponseService";
     
        public ManagerClient(String host, int port, Protocol protocol) : base(host, port, protocol, THRIFT_MANAGER_SERVICE_NAME_REQUEST, THRIFT_MANAGER_SERVICE_NAME_RESPONSE)
        {
            this.managerRequestClient = new ManagerService.Client(requestProtocol);
            this.managerResponseClient = new ManagerResponseService.Client(responseProtocol);
        }

        public Task<List<String>> GetClusterIds()
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.getClusterIds();
            }
            return FetchResult<List<String>>(ticketId, this.managerResponseClient.getClusterIds);
        }

        public Task<ClusterInfoDTO> GetClusterInfo(String cId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.getClusterInfo(cId);
            }
            return FetchResult<ClusterInfoDTO>(ticketId, this.managerResponseClient.getClusterInfo);
        }

        public Task<ServiceEndpointDTO> GetClusterEndpoint(String cId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.getClusterEndpoint(cId);
            }
            return FetchResult<ServiceEndpointDTO>(ticketId, this.managerResponseClient.getClusterEndpoint);
        }

        public Task<String> CreateAWSCluster(int nrOfWorkers, int nrOfReducers, AWSSpecificationDTO specification)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.createAWSCluster(nrOfWorkers, nrOfReducers, specification);
            }
            return FetchResult<String>(ticketId, this.managerResponseClient.createAWSCluster);
        }

        public Task<TicketStatusDTO> AddCluster(ServiceEndpointDTO endpoint)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.addCluster(endpoint);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> DeleteCluster(String cId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.destroyCluster(cId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<TicketStatusDTO> AdjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.adjustNodePoolSize(cId, newNodePoolSize, nodeType);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<String> CreateClientRoutine(RoutineDTO routine)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.createClientRoutine(routine);
            }
            return FetchResult<String>(ticketId, this.managerResponseClient.createClientRoutine);
        }

        public Task<String> UploadClientRoutineBinary(String rId, string binaryName, bool isPrimary, byte[] data)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.uploadClientRoutineBinary(rId, binaryName, isPrimary, data);
            }
            return FetchResult<String>(ticketId, this.managerResponseClient.uploadClientRoutineBinary);
        }

        public Task<TicketStatusDTO> RemoveClientRoutine(String rId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.removeClientRoutine(rId);
            }
            return FetchTicketStatusTask(ticketId);
        }

        public Task<FeatureDTO> GetFeatureByNameAndVersion(String name, String version)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.managerRequestClient.getFeatureByNameAndVersion(name, version);
            }
            return FetchResult<FeatureDTO>(ticketId, this.managerResponseClient.getFeatureByNameAndVersion);
        }
    }
}
