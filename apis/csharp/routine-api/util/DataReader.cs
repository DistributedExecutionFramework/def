using System;
using System.IO;
using routine_api.api;
using Thrift.Protocol;
using Thrift.Transport;

namespace routine_api.util
{
    /// <summary>
    /// Reads data from a Stream in the Thrift Binary Protocol.
    /// </summary>
    public class DataReader : IDisposable, IPipeReader
    {
        private bool _disposed;

        private readonly TTransport _inPipe;
        private readonly TBinaryProtocol _inProto;

        /// <inheritdoc />
        /// <summary>
        /// Builds a DataReader that accesses data in a File. A Filestream will be opened to access a named pipe.
        /// </summary>
        /// <param name="inPipePath">The path to the file to open the stream</param>
        public DataReader(string inPipePath) : this(File.OpenRead(inPipePath))
        {
        }

        /// <summary>
        /// Builds a DataReader that accesses data from a stream. This uses the provided stream directly.
        /// </summary>
        /// <param name="inPipeStream">The stream to use</param>
        public DataReader(Stream inPipeStream)
        {
            _inPipe = new TStreamTransport(inPipeStream, null);
            _inProto = new TBinaryProtocol(_inPipe);
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (_disposed) return;
            if (disposing)
            {
                _inPipe?.Dispose();
            }

            _disposed = true;
        }

        /// <summary>
        /// Read/Fetch data of a specific type from the stream.
        /// </summary>
        /// <param name="instance">The type to read</param>
        /// <typeparam name="T">The type needs to be a Thrift generated type</typeparam>
        /// <exception cref="IOException">If there is an error reading</exception>  
        /// <returns>An instance of type T read from the stream</returns>
        public T Read<T>(T instance) where T : TBase
        {
            instance.Read(_inProto);
            return instance;
        }

        /// <summary>
        /// Read a specific number of bytes from the stream.
        /// </summary>
        /// <param name="length">The number of bytes to read</param>
        /// <exception cref="TTransportException">If there is an error reading</exception>  
        /// <returns>Returns the read byte array</returns>
        public byte[] ReadBytes(int length)
        {
            byte[] buf = new byte[length];
            _inPipe.Read(buf, 0, length);
            return buf;
        }

        /// <summary>
        /// Reads an Integer from the stream.
        /// </summary>
        /// <exception cref="TTransportException">If there is an error reading</exception>  
        /// <returns>Returns a read Integer</returns>
        public int ReadInt()
        {
            return _inProto.ReadI32();
        }

        /// <summary>
        /// Reads a string from the stream.
        /// </summary>
        /// <exception cref="TTransportException">If there is an error reading</exception>  
        /// <returns>Returns a read string</returns>
        public string ReadString()
        {
            return _inProto.ReadString();
        }
    }
}