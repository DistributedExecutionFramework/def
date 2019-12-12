using System;
using System.IO;
using System.Reflection;
using routine_api.exception;
using routine_api.util;
using Thrift;

namespace routine_api
{
    /// <summary>
    /// Defines the base functionality for all different types of routines.
    /// </summary>
    public abstract class AbstractRoutine
    {
        protected string RoutineName { get; set; }
        protected string InPipeName { get; set; }
        protected string OutPipeName { get; set; }
        protected string CtrlPipeName { get; set; }

        protected DataReader InPipe;
        protected DataWriter OutPipe;
        protected DataWriter CtrlPipe;

        /// <summary>
        /// Tries to parse the input arguments and tries to create and initialize a routine by loading a specified class from an
        /// external assembly.
        /// </summary>
        /// <param name="args">The command line arguments to be parsed</param>
        /// <returns>Returns a created routine</returns>
        /// <exception cref="ArgumentException">If the external assembly or class cannot be loaded</exception>
        private static AbstractRoutine CreateRoutine(string[] args)
        {
            string assemblyName = args[0];
            string routineName = args[1];

            if (!File.Exists(assemblyName))
            {
                throw new ArgumentException("Assembly " + assemblyName + " cannot be found");
            }

            Assembly assembly = Assembly.LoadFrom(assemblyName);
            Type type = assembly.GetType(routineName);

            if (type == null)
            {
                throw new ArgumentException("Class " + routineName + " cannot be found");
            }

            // Instantiate routine
            AbstractRoutine routine = (AbstractRoutine) Activator.CreateInstance(type);
            routine.Setup(args);
            routine.RoutineName = routineName;

            return routine;
        }

        /// <summary>
        /// Start the execution of a routine by setting up the IO, running the routine logic implemented by the external
        /// assembly and finally closing the IO and sending a "Done" Order to the control pipe.
        /// </summary>
        /// <exception cref="RoutineException">If there is an error running the routine</exception>
        private void Run()
        {
            try
            {
                // Setup routine IO
                SetupIo(InPipeName, OutPipeName, CtrlPipeName);
                Log(LogLevel.Debug, $"Routine {RoutineName}: IO setup done, run");

                RunRoutine();

                // Send 'done' to RoutinesCommunicator (Worker)
                Order done = new Order
                {
                    Command = Command.ROUTINE_DONE,
                    Value = ""
                };
                Log(LogLevel.Debug, "Send ROUTINE_DONE RoutinesCommunicator (Worker)");
                CtrlPipe.Store(done);

                // Shutdown
                Log(LogLevel.Debug, $"Routine {RoutineName} done, shutting down IO");
                ShutdownIo();
            }
            catch (Exception e)
            {
                Log(LogLevel.Error, $"Error while running routine {RoutineName}: {e.Message}");
                throw new RoutineException(e);
            }
        }

        /// <summary>
        /// Perform an initial setup of the routine type. Each routine type can override this method to perform a different
        /// argument parsing and routine setup. This method checks the number of arguments and initializes the pipe names.
        /// </summary>
        /// <param name="args">The command line arguments to parse</param>
        /// <exception cref="RoutineException">If there is a wrong number of arguments</exception>
        protected virtual void Setup(string[] args)
        {
            if (args.Length != 5)
            {
                throw new RoutineException("Wrong Number of Arguments for Routine. Usage: <routineName> <inPipe> <outPipe> <ctrlPipe>");
            }

            string inPipeName = args[2];
            string outPipeName = args[3];
            string ctrlPipeName = args[4];

            InPipeName = "null".Equals(inPipeName, StringComparison.InvariantCultureIgnoreCase)
                ? null
                : inPipeName;
            OutPipeName = "null".Equals(outPipeName, StringComparison.InvariantCultureIgnoreCase)
                ? null
                : outPipeName;
            CtrlPipeName = "null".Equals(ctrlPipeName, StringComparison.InvariantCultureIgnoreCase)
                ? null
                : ctrlPipeName;
        }

        /// <summary>
        /// Each routine type can override this method to perform a final teardown of e.g. open resources that need to be closed.
        /// The default implementation does nothing.
        /// </summary>
        protected virtual void End()
        {
        }

        /// <summary>
        /// This method contains routine type specific code and has to be implemented by each routine type.
        /// This is the "routine body" and should contain any routine specific pipe communication.
        /// </summary>
        protected abstract void RunRoutine();

        /// <summary>
        /// Closes any open pipes.
        /// </summary>
        private void ShutdownIo()
        {
            // Close all pipes
            InPipe?.Dispose();

            OutPipe?.Dispose();

            CtrlPipe?.Dispose();
        }

        /// <summary>
        /// This method sets up the communication pipes. If a pipe name is null, no pipe will be opened.
        /// </summary>
        /// <param name="inPipePath">The name of the data input pipe</param>
        /// <param name="outPipePath">The name of the data output pipe</param>
        /// <param name="ctrlPipePath">The name of the control pipe</param>
        private void SetupIo(string inPipePath, string outPipePath, string ctrlPipePath)
        {
            if (InPipe == null)
            {
                InPipe = inPipePath != null ? new DataReader(inPipePath) : null;
            }

            if (OutPipe == null)
            {
                OutPipe = outPipePath != null ? new DataWriter(outPipePath) : null;
            }

            if (CtrlPipe == null)
            {
                CtrlPipe = ctrlPipePath != null ? new DataWriter(ctrlPipePath) : null;
            }

            Log(LogLevel.Debug, $"inPipe={inPipePath}, outPipe={outPipePath}, ctrlPipe={ctrlPipePath}");
        }

        /// <summary>
        /// Log a message by sending it to the control pipe.
        /// </summary>
        /// <param name="level">The Log Level to log the message with</param>
        /// <param name="msg">The message to log</param>
        protected void Log(LogLevel level, string msg)
        {
            Command cmd;
            switch (level)
            {
                case LogLevel.Error:
                    cmd = Command.LOG_ERROR;
                    break;
                case LogLevel.Debug:
                    cmd = Command.LOG_DEBUG;
                    break;
                default:
                    cmd = Command.LOG_INFO;
                    break;
            }

            try
            {
                CtrlPipe.Store(new Order
                {
                    Command = cmd,
                    Value = msg
                });
            }
            catch (Exception e) when (e is NullReferenceException || e is TException || e is IOException)
            {
                string outMsg = $"{level} {msg} (Could not send log through ctrl-pipe: {e.Message})";
                switch (level)
                {
                    case LogLevel.Error:
                        Console.Error.WriteLine(outMsg);
                        break;
                    default:
                        Console.WriteLine(outMsg);
                        break;
                }
            }
        }

        /// <summary>
        /// Main entry point for the API Framework.
        /// </summary>
        /// <param name="args">The command line arguments to pass to the routine.</param>
        /// <exception cref="RoutineException">If there is an error in the routine process</exception>
        public static void Main(string[] args)
        {
            try
            {
                AbstractRoutine routine = CreateRoutine(args);
                routine.Setup(args);
                routine.Run();
                routine.End();
            }
            catch (Exception e)
            {
                Console.Error.WriteLine($"{LogLevel.Error} - Error while running Routine: {e.Message}");
                throw new RoutineException(e);
            }
        }
    }
}