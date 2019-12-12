using System;
using System.Net.Sockets;
using System.Threading.Tasks;
using Thrift.Protocol;
using Thrift.Transport;
using common.client;
using common.util;
using common.thrift;

namespace common.parameterserver
{
    public class ParameterServerClient : AbstractClient
    {
        private ParameterServerService.Iface parameterServerRequestClient;
        private ParameterServerResponseService.Iface parameterServerResponseClient;

        private static String THRIFT_PARAMETERSERVER_SERVICE_NAME_REQUEST = "/at.enfilo.def.parameterserver.api.thrift.ParameterServerService";
        private static String THRIFT_PARAMETERSERVER_SERVICE_NAME_RESPONSE = "/at.enfilo.def.parameterserver.api.thrift.ParameterServerResponseService";

        public ParameterServerClient(ServiceEndpointDTO endpoint) : this(endpoint.Host, endpoint.Port, endpoint.Protocol) { }

        public ParameterServerClient(String host, int port, Protocol protocol) : base(host, port, protocol, THRIFT_PARAMETERSERVER_SERVICE_NAME_REQUEST, THRIFT_PARAMETERSERVER_SERVICE_NAME_RESPONSE)
        {
            this.parameterServerRequestClient = new ParameterServerService.Client(requestProtocol);
            this.parameterServerResponseClient = new ParameterServerResponseService.Client(responseProtocol);
        }

        public Task<String> SetParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.parameterServerRequestClient.setParameter(programId, parameterId, parameter, protocol);
            }
            return FetchResult<String>(ticketId, this.parameterServerResponseClient.setParameter);
        }

        public Task<String> CreateParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol, ParameterType type)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.parameterServerRequestClient.createParameter(programId, parameterId, parameter, protocol, type);
            }
            return FetchResult<String>(ticketId, this.parameterServerResponseClient.createParameter);
        }

        public Task<ResourceDTO> GetParameter(String programId, String parameterId, ParameterProtocol protocol)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.parameterServerRequestClient.getParameter(programId, parameterId, protocol);
            }
            return FetchResult<ResourceDTO>(ticketId, this.parameterServerResponseClient.getParameter);
        }

        public Task<String> AddToParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.parameterServerRequestClient.addToParameter(programId, parameterId, parameter, protocol);
            }
            return FetchResult<String>(ticketId, this.parameterServerResponseClient.addToParameter);
        }

        public Task<String> DeleteParameter(String programId, String parameterId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.parameterServerRequestClient.deleteParameter(programId, parameterId);
            }
            return FetchResult<String>(ticketId, this.parameterServerResponseClient.deleteParameter);
        }

        public Task<String> DeleteAllParameters(String programId)
        {
            String ticketId = null;
            lock (clientLock)
            {
                ticketId = this.parameterServerRequestClient.deleteAllParameters(programId);
            }
            return FetchResult<String>(ticketId, this.parameterServerResponseClient.deleteAllParameters);
        }
    }
}
