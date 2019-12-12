using System;
using System.IO;
using routine_api.exception;
using common.thrift;
using Thrift;
using Thrift.Protocol;

namespace routine_api
{
    /// <inheritdoc />
    /// <summary>
    /// Defines the base functionality of an Objective Routine.
    /// </summary>
    /// <typeparam name="TR">Defines the type of the result value, which needs to be a Thrift generated type</typeparam>
    public abstract class ObjectiveRoutine<TR> : AbstractRoutine where TR : TBase
    {

        /// <summary>
        /// Stores the result of the routine implementation to the output pipe.
        /// </summary>
        /// <param name="result">The result to store</param>
        private void SetResult(TR result)
        {
            Log(LogLevel.Debug, $"Storing Result {result.ToString()}");
            byte[] data = ThriftSerializer.Serialize(result);
            OutPipe.Store(data.Length);
            OutPipe.Store(data);
        }

        /// <summary>
        /// Fetch an input parameter from the input pipe by its name. A command will be sent to the control pipe to
        /// feed the parameter to the input pipe, which will be read and mapped to the specific type T.
        /// </summary>
        /// <param name="name">The name of the input parameter to search for</param>
        /// <typeparam name="T">The type of the parameter to map to, which needs to be a Thrift generated type</typeparam>
        /// <returns>Returns an instance of type T created from the parameter</returns>
        /// <exception cref="AccessParameterException">If there is an error reading the parameter</exception>
        protected T GetParameter<T>(string name) where T : TBase
        {
            Log(LogLevel.Debug, $"Requesting Parameter {name}");

            Order getParam = new Order
            {
                Command = Command.GET_PARAMATER,
                Value = name
            };
            try
            {
                CtrlPipe.Store(getParam);
                T param = InPipe.Read(Activator.CreateInstance<T>());
                Log(LogLevel.Debug, $"Received: {param}");
                return param;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        /// <inheritdoc />
        /// <summary>
        /// Runs the routine implementation and stores the result.
        /// </summary>
        /// <exception cref="T:routine_api.exception.RoutineException">If there is an error running the routine or storing the result</exception>
        protected override void RunRoutine()
        {
            try
            {
                TR result = Routine();
                SetResult(result);
            }
            catch (Exception e) when (e is TException || e is IOException)
            {
                throw new RoutineException(e);
            }
        }

        /// <summary>
        /// Implementation of the routine. This method will be implemented by the Algorithm developer and contains the routine logic.
        /// </summary>
        /// <returns>Returns an instance of type TR, which is the result of the routine</returns>
        protected abstract TR Routine();
    }
}