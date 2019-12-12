using System;
using System.IO;
using routine_api.api;
using Thrift.Protocol;
using Thrift.Transport;

namespace routine_api.util
{
    /// <summary>
    /// Writes data to a Stream in the Thrift Binary Protocol.
    /// </summary>
    public class DataWriter : IDisposable, IPipeWriter
    {
        private bool _disposed;

        private readonly TStreamTransport _outPipe;
        private readonly TProtocol _outProto;

        /// <inheritdoc />
        /// <summary>
        /// Builds a DataWriter that writes data to a File. A Filestream will be opened to access a named pipe.
        /// </summary>
        /// <param name="outPipePath">The path to the file to open the stream</param>
        public DataWriter(string outPipePath) : this(File.OpenWrite(outPipePath))
        {
        }

        /// <summary>
        /// Builds a DataWriter that writes data to a stream. This uses the provided stream directly.
        /// </summary>
        /// <param name="outPipeStream">The stream to use</param>
        public DataWriter(Stream outPipeStream)
        {
            _outPipe = new TStreamTransport(null, outPipeStream);
            _outProto = new TBinaryProtocol(_outPipe);
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
                _outPipe?.Dispose();
            }

            _disposed = true;
        }

        /// <summary>
        /// Writes an instance of type T to the stream.
        /// </summary>
        /// <param name="instance">The instance to write</param>
        /// <exception cref="IOException">If there is an error reading</exception>  
        /// <typeparam name="T">The type of the instance. Needs to be a Thrift generated type</typeparam>
        public void Store<T>(T instance) where T : TBase
        {
            instance.Write(_outProto);
            _outPipe.Flush();
        }

        /// <summary>
        /// Writes a byte array to the stream.
        /// </summary>
        /// <param name="data">The data to write</param>
        /// <exception cref="TTransportException">If there is an error reading</exception>  
        public void Store(byte[] data)
        {
            _outPipe.Write(data, 0, data.Length);
        }

        /// <summary>
        /// Write a string to the stream.
        /// </summary>
        /// <param name="str">The string to write</param>
        /// <exception cref="TTransportException">If there is an error reading</exception>  
        public void Store(string str)
        {
            _outProto.WriteString(str);
        }

        /// <summary>
        /// Writes an integer to the stream.
        /// </summary>
        /// <param name="i">The integer to write</param>
        /// <exception cref="TTransportException">If there is an error reading</exception>  
        public void Store(int i)
        {
            _outProto.WriteI32(i);
        }
    }
}