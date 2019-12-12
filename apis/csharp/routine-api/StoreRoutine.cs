using System;
using System.Collections.Generic;
using System.IO;
using routine_api.exception;
using Thrift;

namespace routine_api
{
    /// <inheritdoc />
    /// <summary>
    /// Defines the base functionality of a Store Routine.
    /// </summary>
    public abstract class StoreRoutine : AbstractRoutine
    {
        private string ProgramId { get; set; }
        private string JobId { get; set; }
        private string TaskId { get; set; }

        /// <summary>
        /// Stores a key/value pair and returns a ResultInfo object. This method contains the StoreRoutine implementation.
        /// </summary>
        /// <param name="partition">The partition id to store to</param>
        /// <param name="key">The key to store</param>
        /// <param name="data">The value to store</param>
        /// <param name="tupleSeq">The current tuple seq #</param>
        /// <returns>Returns a Result object</returns>
        protected abstract Result Store(string partition, string key, byte[] data, int tupleSeq);

        /// <inheritdoc />
        protected override void RunRoutine()
        {
            try
            {
                LinkedList<Result> results = new LinkedList<Result>();

                Log(LogLevel.Debug, "Try to receive from PartitionRoutine");
                // fetch nr of tuples
                int tuples = InPipe.ReadInt();
                Log(LogLevel.Debug, $"{tuples} Tuples to process.");

                for (int i = 0; i < tuples; i++)
                {
                    // receive partition-id, key and value from in stream
                    string partition = InPipe.ReadString();
                    string key = InPipe.ReadString();
                    int size = InPipe.ReadInt();
                    byte[] data = InPipe.ReadBytes(size);

                    // store key value pair
                    Result result = Store(partition, key, data, i);
                    results.AddLast(result);
                    Log(LogLevel.Debug,
                        $"Stored {i + 1}/{tuples}: key {key} and {size} data bytes to partition {partition}");
                }

                // send ResultInfos to RoutineCommunicator (Worker)
                Log(LogLevel.Debug, $"Send {tuples} StoreResult object to RoutinesCommunicator (Worker)");
                Order order = new Order
                {
                    Command = Command.SEND_RESULT,
                    Value = tuples.ToString()
                };
                CtrlPipe.Store(order);
                foreach (Result result in results)
                {
                    CtrlPipe.Store(result);
                }
            }
            catch (Exception e) when (e is TException || e is IOException)
            {
                Log(LogLevel.Error, $"Error while running StoreRoutine: {e.Message}");
                throw new RoutineException(e);
            }
        }

        /// <inheritdoc />
        /// <summary>
        /// Performs a StoreRoutine specific parsing of parameters and setup.
        /// </summary>
        protected override void Setup(string[] args)
        {
            if (args.Length < 7 || args.Length > 8)
            {
                throw new RoutineException("Wrong Number of Arguments for Storage Routine. Usage: <routineName> <inPipe> <ctrlPipe> <pId> <jId> <tId> [<configFile>]");
            }

            string inPipeName = args[2];
            string outPipeName = null;
            string ctrlPipeName = args[3];
            string pId = args[4];
            string jId = args[5];
            string tId = args[6];
            string configFile = null;
            if (args.Length == 8)
            {
                configFile = args[7];
            }

            InPipeName = "null".Equals(inPipeName, StringComparison.InvariantCultureIgnoreCase)
                ? null
                : inPipeName;
            OutPipeName = null;
            CtrlPipeName = "null".Equals(ctrlPipeName, StringComparison.InvariantCultureIgnoreCase)
                ? null
                : ctrlPipeName;

            ProgramId = pId;
            JobId = jId;
            TaskId = tId;

            if (configFile != null)
            {
                Log(LogLevel.Debug, $"Configure StoreRoutine {RoutineName} with {configFile}");
                Configure(configFile);
            }

            SetupStorage();
        }

        /// <inheritdoc />
        /// <summary>
        /// Call the ShutdownStorage method.
        /// </summary>
        protected override void End()
        {
            ShutdownStorage();
        }

        /// <summary>
        /// Configures this routine with a given config file. Method to be implemented by the routine implementation. 
        /// </summary>
        /// <param name="configFile">The path to the config file</param>
        protected abstract void Configure(string configFile);

        /// <summary>
        /// Shutdown any resources opened previously (e.g. storage streams, database connections, ...).
        /// Method to be implemented by the routine implementation. 
        /// </summary>
        protected abstract void ShutdownStorage();

        /// <summary>
        /// Setup any resources needed for the storage routine to work (e.g. storage streams, database connections, ...).
        /// Method to be implemented by the routine implementation. 
        /// </summary>
        protected abstract void SetupStorage();
    }
}