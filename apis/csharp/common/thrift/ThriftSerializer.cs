using System;
using System.IO;
using Thrift.Protocol;
using Thrift.Transport;

namespace common.thrift
{
    /// <summary>
    /// Helper class to serialze an instance into a byte array in the TBinaryProtocol.
    /// This class is used to simulate the TSerializer available in Java.
    /// </summary>
    public static class ThriftSerializer
    {
        /// <summary>
        /// Serializes an instance of type T into a byte array in the TBinaryProtocol.
        /// This is done by writing the data to a MemoryStream and reading its contents.
        /// The stream is openend and closed automatically.
        /// </summary>
        /// <param name="instance">The instance to serialize</param>
        /// <typeparam name="T">The type of the instance to serialize. This has to be a Thrift generated type</typeparam>
        /// <returns>Returns a byte array</returns>
        public static byte[] Serialize<T>(T instance) where T : TBase
        {
            MemoryStream memoryStream = new MemoryStream();
            using (TBinaryProtocol protocol = new TBinaryProtocol(new TStreamTransport(memoryStream, memoryStream)))
            {
                instance.Write(protocol);
                return memoryStream.ToArray();
            }
        }

        public static T Deserialize<T>(byte[] data) where T: TBase
        {
            T instance = Activator.CreateInstance<T>();
            var buffer = new TMemoryBuffer(data);
            using (TProtocol tProtocol = new TBinaryProtocol(buffer))
            {
                instance.Read(tProtocol);
                return instance;
            }

        }
    }
}