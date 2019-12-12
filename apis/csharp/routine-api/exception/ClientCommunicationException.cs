using System;
namespace routine_api.exception
{
    public class ClientCommunicationException : Exception
    {
        public ClientCommunicationException(string message) : base(message)
        {
        }

        public ClientCommunicationException(Exception cause) : base(cause.Message, cause)
        {
        }

        public ClientCommunicationException(string message, Exception cause) : base(message, cause)
        {
        }
    }
}
