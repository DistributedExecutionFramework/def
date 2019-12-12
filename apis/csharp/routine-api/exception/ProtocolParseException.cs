using System;
namespace routine_api.exception
{
    public class ProtocolParseException : Exception
    {
        public ProtocolParseException(Exception cause) : base(cause.Message, cause)
        {
        }
    }
}
