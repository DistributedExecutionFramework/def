using System;

namespace routine_api.exception
{
    /// <summary>
    /// Exception thrown when there is an error creating a pipe
    /// </summary>
    public class PipeCreationException : Exception
    {   
        public PipeCreationException(string message) : base(message)
        {
        }
        
        public PipeCreationException(Exception cause) : base(cause.Message, cause)
        {
        }

        public PipeCreationException(string message, Exception cause) : base(message, cause)
        {
        }
    }
}