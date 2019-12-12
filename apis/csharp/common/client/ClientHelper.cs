using System;
using System.Threading;
using System.Threading.Tasks;
using common.thrift;

namespace common.client
{
    public class ClientHelper
    {
        private TicketService.Iface ticketService;
        private int pollDelay;
        private readonly object ticketLock = new object();

        public ClientHelper(TicketService.Iface ticketService, int pollDelay)
        {
            this.ticketService = ticketService;
            this.pollDelay = pollDelay;
        }

        public Task<TicketStatusDTO> FetchTicketStatusTask(String ticketId)
        {
            var t = Task<TicketStatusDTO>.Run(() =>
            {
                return FetchTicketStatus(ticketId);
            });
            return t;
        }

        public TicketStatusDTO FetchTicketStatus(String ticketId)
        {
            TicketStatusDTO state = TicketStatusDTO.UNKNOWN;
            while (state != TicketStatusDTO.DONE
                && state != TicketStatusDTO.CANCELED
                && state != TicketStatusDTO.FAILED)
            {
                Thread.Sleep(pollDelay);
                lock (ticketLock)
                {
                    state = this.ticketService.getTicketStatus(ticketId);
                }
            }
            return state;
        }

        public Task<T> FetchResult<T>(String ticketId, Func<string, T> method, object lockObject)
        {
            var t = Task<T>.Run(() =>
            {
                TicketStatusDTO state = FetchTicketStatus(ticketId);

                if (state == TicketStatusDTO.DONE)
                {
                    lock (lockObject)
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
