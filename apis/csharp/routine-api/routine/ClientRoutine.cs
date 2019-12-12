using System;
using System.IO;
using System.Collections.Generic;
using common.execlogic;
using common.parameterserver;
using common.thrift;
using routine_api.exception;
using routine_api.util;
using Thrift;

namespace routine_api.routine
{
    public abstract class ClientRoutine : AbstractRoutine
    {
        private ProgramDTO program;
        private ExecLogicClient client;
        private ParameterServerCommunicator parameterServerCommunicator;
        private Dictionary<String, ResourceDTO> results;

        private void SetResult()
        {
            Log(LogLevel.Debug, "Storing results.");
            program.Results = results;
            byte[] data = ThriftSerializer.Serialize(program);
            Result result = new Result
            {
                Seq = 0,
                Key = "PROGRAM",
                Url = null,
                Data = data
            };
            Order order = new Order
            {
                Command = Command.SEND_RESULT,
                Value = "1".ToString()
            };
            CtrlPipe.Store(order);
            CtrlPipe.Store(result);
            Log(LogLevel.Debug, "Client routine results stored.");
        }

        protected override void RunRoutine()
        {
            try
            {
                this.results = new Dictionary<String, ResourceDTO>();
                this.program = GetProgram();
                this.client = GetServiceClient();
                ParameterServerClient parameterServerClient = GetParameterServerClient();
                this.parameterServerCommunicator = new ParameterServerCommunicator(parameterServerClient, this.program.Id);

                Routine(this.program.Id, this.client);
                SetResult();
            }
            catch (Exception e) when (e is TException || e is IOException)
            {
                throw new RoutineException(e);
            }
        }

        protected abstract void Routine(string pId, ExecLogicClient client);

        private ProgramDTO GetProgram()
        {
            Log(LogLevel.Debug, "Requesting program.");
            Order getProgram = new Order
            {
                Command = Command.GET_PARAMETER,
                Value = "program"
            };
            try
            {
                CtrlPipe.Store(getProgram);
                ProgramDTO fetchedProgram = InPipe.Read(Activator.CreateInstance<ProgramDTO>());
                Log(LogLevel.Debug, $"Received: {fetchedProgram}");
                return fetchedProgram;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        private ExecLogicClient GetServiceClient()
        {
            Log(LogLevel.Debug, "Requesting service client.");
            Order getClient = new Order
            {
                Command = Command.GET_PARAMETER,
                Value = "serviceEndpoint"
            };
            try
            {
                CtrlPipe.Store(getClient);
                ServiceEndpointDTO fetchedEndpoint = InPipe.Read(Activator.CreateInstance<ServiceEndpointDTO>());
                Log(LogLevel.Debug, $"Received: {fetchedEndpoint}");
                ExecLogicClient createdClient = new ExecLogicClient(fetchedEndpoint);
                return createdClient;
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

        protected void AddToResults(String key, ResourceDTO resource)
        {
            if (key != null)
            {
                results.Add(key, resource);
            }
        }

        /// <inheritdoc />
        /// <summary>
        /// Performs a StoreRoutine specific parsing of parameters and setup.
        /// </summary>
        protected override void Setup(string[] args)
        {
            if (args.Length != 4)
            {
                throw new RoutineException("Wrong Number of Arguments for Client Routine. Usage: <routineName> <inPipe> <ctrlPipe>");
            }

            string inPipeName = args[2];
            string ctrlPipeName = args[3];

            InPipeName = "null".Equals(inPipeName, StringComparison.InvariantCultureIgnoreCase)
                ? null
                : inPipeName;
            OutPipeName = null;
            CtrlPipeName = "null".Equals(ctrlPipeName, StringComparison.InvariantCultureIgnoreCase)
                ? null
                : ctrlPipeName;
        }


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
