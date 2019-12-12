using System;

namespace routine_api.exception
{
    /// <summary>
    /// Exception thrown when there is an error accessing a parameter.
    /// </summary>
    public class AccessParameterException : Exception
    {   
        public AccessParameterException(string message) : base(message)
        {
        }
        
        public AccessParameterException(Exception cause) : base(cause.Message, cause)
        {
        }

        public AccessParameterException(string message, Exception cause) : base(message, cause)
        {
        }
    }
}