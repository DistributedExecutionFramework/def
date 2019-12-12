using System;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;
using common.util;
using Thrift.Protocol;
using Thrift.Transport;

namespace common.client
{
    public abstract class AbstractClient
    {
        protected TProtocol requestProtocol;
        protected TProtocol responseProtocol;

        protected TicketService.Iface ticketService;

        private static String THRIFT_TICKET_SERVICE_NAME = "/at.enfilo.def.communication.api.ticket.thrift.TicketService";
        protected static int POLL_DELAY = 200;

        protected readonly object clientLock = new object();
        private readonly object ticketLock = new object();
        
        protected AbstractClient(String host, int port, Protocol protocol, String requestServiceName, String responseServiceName)
        {
            switch (protocol)
            {
                case Protocol.THRIFT_TCP:
                    // client service
                    TcpClient client = new TcpClient(host, port);
                    TSocket transport = new TSocket(client);
                    TBufferedTransport bufferedTransport = new TBufferedTransport(transport);
                    TProtocol transportProtocol = new TBinaryProtocol(bufferedTransport);
                    requestProtocol = new TMultiplexedProtocol(transportProtocol, requestServiceName);
                    responseProtocol = new TMultiplexedProtocol(transportProtocol, responseServiceName);

                    // ticket service
                    TcpClient ticketClient = new TcpClient(host, port);
                    TTransport ticketTransport = new TSocket(ticketClient);
                    TBufferedTransport ticketBufferedTransport = new TBufferedTransport(ticketTransport);
                    TProtocol ticketProtocol = new TBinaryProtocol(ticketBufferedTransport);
                    TProtocol ticketTcpProtocol = new TMultiplexedProtocol(ticketProtocol, THRIFT_TICKET_SERVICE_NAME);
                    this.ticketService = new TicketService.Client(ticketTcpProtocol);

                    break;

                case Protocol.THRIFT_HTTP:
                    // client service
                    THttpClient clientTransportRequest = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api/", host, port, requestServiceName)));
                    THttpClient clientTransportResponse = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api", host, port, responseServiceName)));
                    requestProtocol = new TBinaryProtocol(clientTransportRequest);
                    responseProtocol = new TBinaryProtocol(clientTransportResponse);
                    clientTransportRequest.Open();
                    clientTransportResponse.Open();

                    // ticket service
                    THttpClient transportTicket = new THttpClient(new Uri(String.Format("http://{0}:{1}/{2}/api", host, port, THRIFT_TICKET_SERVICE_NAME)));
                    TProtocol httpProtocolTicket = new TBinaryProtocol(transportTicket);
                    this.ticketService = new TicketService.Client(httpProtocolTicket);
                    transportTicket.Open();

                    break;

                case Protocol.REST:
                    throw new NotImplementedException();
            }
        }

        protected Task<TicketStatusDTO> FetchTicketStatusTask(String ticketId)
        {
            var t = Task<TicketStatusDTO>.Run(() =>
            {
                return FetchTicketStatus(ticketId);
            });
            return t;
        }

        protected TicketStatusDTO FetchTicketStatus(String ticketId)
        {
            TicketStatusDTO state = TicketStatusDTO.UNKNOWN;
            while (state != TicketStatusDTO.DONE
                && state != TicketStatusDTO.CANCELED
                && state != TicketStatusDTO.FAILED)
            {
                Thread.Sleep(POLL_DELAY);
                lock (ticketLock)
                {
                    state = this.ticketService.getTicketStatus(ticketId);
                }
            }
            return state;
        }

        protected Task<T> FetchResult<T>(String ticketId, Func<string, T> method)
        {
            var t = Task<T>.Run(() =>
            {
                TicketStatusDTO state = FetchTicketStatus(ticketId);

                if (state == TicketStatusDTO.DONE)
                {
                    lock (clientLock)
                    {
                        return method(ticketId);
                    }
                }
                else
                {
                    throw new Exception(String.Format("Error while fetching result from response client. Finished with state: {0}", state));
                }
            });
            return t;
        }
    }
}
