using System;
using System.Collections.Generic;
using routine_api.exception;
using common.thrift;
using Thrift.Protocol;

namespace routine_api
{
    /// <summary>
    /// Defines the base functionality of a MapRoutine.
    /// </summary>
    /// <typeparam name="T">The type to map from. This needs to be a Thrift generated type</typeparam>
    /// <typeparam name="TV">The type to map to. This needs to be a Thrift generates type</typeparam>
    public abstract class MapRoutine<T, TV> : AbstractRoutine where T : TBase where TV : TBase
    {
        /// <inheritdoc />
        protected override void RunRoutine()
        {
            try
            {
                Log(LogLevel.Debug, "Try to receive result from ObjectiveRoutine");
                // Receive object from inPipe
                InPipe.ReadInt();
                T t = InPipe.Read(Activator.CreateInstance<T>());
                Log(LogLevel.Debug, $"Received from ObjectiveRoutine: {t.ToString()}");

                // Map function
                List<Tuple<string, TV>> tuples = Map(t);
                Log(LogLevel.Debug, $"Mapping done, created {tuples.Count} tuples");

                // Tell Receiver the number of tuples
                Log(LogLevel.Debug, "Storing number of tuples");
                OutPipe.Store(tuples.Count);

                // Store tuples to outPipe
                int i = 1;
                foreach (Tuple<string, TV> tuple in tuples)
                {
                    // Write key
                    Log(LogLevel.Debug, $"Write key {i}: {tuple.Item1}");
                    OutPipe.Store(tuple.Item1);
                    // Write value (size & data)
                    Log(LogLevel.Debug, $"Write value {i}: {tuple.Item2.ToString()}");
                    byte[] data = ThriftSerializer.Serialize(tuple.Item2);
                    OutPipe.Store(data.Length); // size
                    OutPipe.Store(data);
                    i++;
                }
            }
            catch (Exception e)
            {
                Log(LogLevel.Error, $"Error while running map routine: {e.Message}");
                throw new RoutineException(e);
            }
        }

        /// <summary>
        /// Map routine implementation. Maps an instance of type T to a Key/Value pair list.
        /// </summary>
        /// <param name="toMap">The instance to map</param>
        /// <returns>Returns a list of Key/Value pairs</returns>
        protected abstract List<Tuple<string, TV>> Map(T toMap);
    }
}