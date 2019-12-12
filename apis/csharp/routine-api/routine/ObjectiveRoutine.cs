using System;
using System.IO;
using routine_api.exception;
using routine_api.util;
using common.parameterserver;
using common.thrift;
using Thrift;
using Thrift.Protocol;

namespace routine_api.routine
{
    /// <inheritdoc />
    /// <summary>
    /// Defines the base functionality of an Objective Routine.
    /// </summary>
    /// <typeparam name="TR">Defines the type of the result value, which needs to be a Thrift generated type</typeparam>
    public abstract class ObjectiveRoutine<TR> : AbstractRoutine where TR : TBase
    {
        private ParameterServerCommunicator parameterServerCommunicator;

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
                Command = Command.GET_PARAMETER,
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

        private ParameterServerClient GetParameterServerClient()
        {
            Log(LogLevel.Debug, "Requesting parameter server client.");
            Order getClient = new Order
            {
                Command = Command.GET_PARAMETER,
                Value = "parameterServerEndpoint"
            };
            try
            {
                CtrlPipe.Store(getClient);
                ServiceEndpointDTO fetchedEndpoint = InPipe.Read(Activator.CreateInstance<ServiceEndpointDTO>());
                Log(LogLevel.Debug, $"Received: {fetchedEndpoint}");
                ParameterServerClient createdClient = new ParameterServerClient(fetchedEndpoint);
                return createdClient;
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
                String programId = GetParameter<DEFString>("program").Value;
                ParameterServerClient parameterServerClient = GetParameterServerClient();
                this.parameterServerCommunicator = new ParameterServerCommunicator(parameterServerClient, programId);

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

        protected String DeleteParameter(String name)
        {
            return this.parameterServerCommunicator.DeleteParameter(name);
        }

        protected String DeleteAllParameters()
        {
            return this.parameterServerCommunicator.DeleteAllParameters();
        }

        // BYTE
        protected byte[] GetByteParameter(String name)
        {
            return this.parameterServerCommunicator.GetByteParameter(name);
        }

        protected String SetParameter(String name, byte[] data)
        {
            return this.parameterServerCommunicator.SetParameter(name, data);
        }

        protected String AddToParameter(String name, byte[] data)
        {
            return this.parameterServerCommunicator.AddToParameter(name, data);
        }

        // SHORT
        protected short[] GetShortParameter(String name)
        {
            return this.parameterServerCommunicator.GetShortParameter(name);
        }

        protected String SetParameter(String name, short[] data)
        {
            return this.parameterServerCommunicator.SetParameter(name, data);
        }

        protected String AddToParameter(String name, short[] data)
        {
            return this.parameterServerCommunicator.AddToParameter(name, data);
        }

        // INT
        protected int[] GetIntParameter(String name)
        {
            return this.parameterServerCommunicator.GetIntParameter(name);
        }

        protected String SetParameter(String name, int[] data)
        {
            return this.parameterServerCommunicator.SetParameter(name, data);
        }

        protected String AddToParameter(String name, int[] data)
        {
            return this.parameterServerCommunicator.AddToParameter(name, data);
        }

        // LONG
        protected long[] GetLongParameter(String name)
        {
            return this.parameterServerCommunicator.GetLongParameter(name);
        }

        protected String SetParameter(String name, long[] data)
        {
            return this.parameterServerCommunicator.SetParameter(name, data);
        }

        protected String AddToParameter(String name, long[] data)
        {
            return this.parameterServerCommunicator.AddToParameter(name, data);
        }

        // FLOAT
        protected float[] GetFloatParameter(String name)
        {
            return this.parameterServerCommunicator.GetFloatParameter(name);
        }

        protected String SetParameter(String name, float[] data)
        {
            return this.parameterServerCommunicator.SetParameter(name, data);
        }

        protected String AddToParameter(String name, float[] data)
        {
            return this.parameterServerCommunicator.AddToParameter(name, data);
        }

        // DOUBLE
        protected double[] GetDoubleParameter(String name)
        {
            return this.parameterServerCommunicator.GetDoubleParameter(name);
        }

        protected String SetParameter(String name, double[] data)
        {
            return this.parameterServerCommunicator.SetParameter(name, data);
        }

        protected String AddToParameter(String name, double[] data)
        {
            return this.parameterServerCommunicator.AddToParameter(name, data);
        }

        // CHAR
        protected char[] GetCharParameter(String name)
        {
            return this.parameterServerCommunicator.GetCharParameter(name);
        }

        protected String SetParameter(String name, char[] data)
        {
            return this.SetParameter(name, data);
        }

        protected String AddToParameter(String name, char[] data)
        {
            return this.AddToParameter(name, data);
        }

        // BOOL
        protected bool[] GetBoolParameter(String name)
        {
            return this.GetBoolParameter(name);
        }

        protected String SetParameter(String name, bool[] data)
        {
            return this.SetParameter(name, data);
        }

        protected String AddToParameter(String name, bool[] data)
        {
            return this.AddToParameter(name, data);
        }
    }
}
