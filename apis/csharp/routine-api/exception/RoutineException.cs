using System;

namespace routine_api.exception
{
    /// <summary>
    /// Exception thrown when there is an error executing a routine.
    /// </summary>
    public class RoutineException : Exception
    {
        public RoutineException(string message) : base(message)
        {
        }
        
        public RoutineException(Exception cause) : base(cause.Message, cause)
        {
        }

        public RoutineException(string message, Exception cause) : base(message, cause)
        {
        }
    }
}