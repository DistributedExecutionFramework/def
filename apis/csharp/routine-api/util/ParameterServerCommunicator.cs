using System;
using common.parameterserver;
using routine_api.exception;

namespace routine_api.util
{
    public class ParameterServerCommunicator
    {
        private ParameterServerClient client;
        private String programId;

        public ParameterServerCommunicator(ParameterServerClient client, String programId)
        {
            this.client = client;
            this.programId = programId;
        }

        public String DeleteParameter(String name)
        {
            try
            {
                return this.client.DeleteParameter(this.programId, name).Result;
            }
            catch (Exception e)
            {
                throw new ClientCommunicationException(e);
            }
        }

        public String DeleteAllParameters()
        {
            try
            {
                return this.client.DeleteAllParameters(this.programId).Result;
            }
            catch (Exception e)
            {
                throw new ClientCommunicationException(e);
            }
        }

        // BYTE
        public byte[] GetByteParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (byte[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, byte[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "byte");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, byte[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "byte");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        // SHORT
        public short[] GetShortParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (short[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, short[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "short");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, short[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "short");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        // INT
        public int[] GetIntParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (int[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, int[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "int");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, int[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "int");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        // LONG
        public long[] GetLongParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (long[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, long[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "long");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, long[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "long");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        // FLOAT
        public float[] GetFloatParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (float[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, float[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "float");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, float[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "float");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        // DOUBLE
        public double[] GetDoubleParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (double[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, double[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "double");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, double[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "double");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        // CHAR
        public char[] GetCharParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (char[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, char[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "char");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, char[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "char");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        // BOOL
        public bool[] GetBoolParameter(String name)
        {
            try
            {
                ResourceDTO resource = this.client.GetParameter(this.programId, name, ParameterProtocol.PRIMITIVE).Result;
                return (bool[])PrimitiveProtocolParser.Decode(resource);
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String SetParameter(String name, bool[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "bool");
                return this.client.SetParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }

        public String AddToParameter(String name, bool[] data)
        {
            try
            {
                ResourceDTO resource = PrimitiveProtocolParser.Encode(data, "bool");
                return this.client.AddToParameter(this.programId, name, resource, ParameterProtocol.PRIMITIVE).Result;
            }
            catch (Exception e)
            {
                throw new AccessParameterException(e);
            }
        }
    }
}
