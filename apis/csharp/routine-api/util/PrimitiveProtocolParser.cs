using System;
using routine_api.exception;

namespace routine_api.util
{
    public class PrimitiveProtocolParser
    {
        private static readonly int SHORT_SIZE = 2;
        private static readonly int LONG_SIZE = 8;
        private static readonly int INT_SIZE = 4;
        private static readonly int FLOAT_SIZE = 4;
        private static readonly int DOUBLE_SIZE = 8;
        private static readonly int CHAR_SIZE = 2;

        private static readonly byte[] boolIdx = new byte[] { (byte)0x80, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1 };

        public ParameterProtocol GetAssociation() { return ParameterProtocol.PRIMITIVE; }

        public static Object Decode(ResourceDTO parameter)
        {
            Object result;
            byte[] data = parameter.Data;
            if (parameter.DataTypeId == null)
            {
                throw new ProtocolParseException(new ArgumentException("Could not parse data. Type id is null."));
            }
            if (data == null)
            {
                return null;
            }

            switch (parameter.DataTypeId.ToLowerInvariant())
            {
                case "byte":
                    result = parameter.Data;
                    break;
                case "short":
                    if (data.Length % SHORT_SIZE != 0)
                    {
                        throw new ProtocolParseException(new ArgumentException("Could not parse data. Data length does not match short type."));
                    }
                    result = DecodeShort(data);
                    break;
                case "int":
                    if (data.Length % INT_SIZE != 0)
                    {
                        throw new ProtocolParseException(new ArgumentException("Could not parse data. Data length does not match int type."));
                    }
                    result = DecodeInt(data);
                    break;
                case "long":
                    if (data.Length % LONG_SIZE != 0)
                    {
                        throw new ProtocolParseException(new ArgumentException("Could not parse data. Data length does not match long type."));
                    }
                    result = DecodeLong(data);
                    break;
                case "float":
                    if (data.Length % FLOAT_SIZE != 0)
                    {
                        throw new ProtocolParseException(new ArgumentException("Could not parse data. Data length does not match float type."));
                    }
                    result = DecodeFloat(data);
                    break;
                case "double":
                    if (data.Length % DOUBLE_SIZE != 0)
                    {
                        throw new ProtocolParseException(new ArgumentException("Could not parse data. Data length does not match double type."));
                    }
                    result = DecodeDouble(data);
                    break;
                case "char":
                    if (data.Length % CHAR_SIZE != 0)
                    {
                        throw new ProtocolParseException(new ArgumentException("Could not parse data. Data length does not match char type."));
                    }
                    result = DecodeChar(data);
                    break;
                case "boolean":
                    result = DecodeBool(data);
                    break;
                default:
                    throw new ProtocolParseException(new ArgumentException("Unknown type id"));
            }
            return result;
        }

        public static ResourceDTO Encode(Object data, String typeId)
        {
            ResourceDTO result = new ResourceDTO();
            result.DataTypeId = typeId;

            if (data == null)
            {
                return result;
            }

            switch (typeId)
            {
                case "byte":
                    result.Data = (byte[])data;
                    break;
                case "short":
                    result.Data = Encode((short[])data);
                    break;
                case "int":
                    result.Data = Encode((int[])data);
                    break;
                case "long":
                    result.Data = Encode((long[])data);
                    break;
                case "float":
                    result.Data = Encode((float[])data);
                    break;
                case "double":
                    result.Data = Encode((double[])data);
                    break;
                case "char":
                    result.Data = Encode((char[])data);
                    break;
                case "boolean":
                    result.Data = Encode((bool[])data);
                    break;
                default:
                    throw new ProtocolParseException(new ArgumentException("Unknown type id."));
            }
            return result;
        }

        // SHORT
        private static byte[] Encode(short[] array)
        {
            byte[] byteArray = new byte[array.Length * SHORT_SIZE];
            Buffer.BlockCopy(array, 0, byteArray, 0, array.Length);
            return byteArray;
        }

        private static short[] DecodeShort(byte[] array)
        {
            short[] result = new short[array.Length / SHORT_SIZE];
            Buffer.BlockCopy(array, 0, result, 0, array.Length);
            return result;
        }

        // INT
        private static byte[] Encode(int[] array)
        {
            byte[] byteArray = new byte[array.Length * INT_SIZE];
            Buffer.BlockCopy(array, 0, byteArray, 0, array.Length);
            return byteArray;
        }

        private static int[] DecodeInt(byte[] array)
        {
            int[] result = new int[array.Length / INT_SIZE];
            Buffer.BlockCopy(array, 0, result, 0, array.Length);
            return result;
        }

        // LONG
        private static byte[] Encode(long[] array)
        {
            byte[] byteArray = new byte[array.Length * LONG_SIZE];
            Buffer.BlockCopy(array, 0, byteArray, 0, array.Length);
            return byteArray;
        }

        private static long[] DecodeLong(byte[] array)
        {
            long[] result = new long[array.Length / LONG_SIZE];
            Buffer.BlockCopy(array, 0, result, 0, array.Length);
            return result;
        }

        // FLOAT
        private static byte[] Encode(float[] array)
        {
            byte[] byteArray = new byte[array.Length * FLOAT_SIZE];
            Buffer.BlockCopy(array, 0, byteArray, 0, array.Length);
            return byteArray;
        }

        private static float[] DecodeFloat(byte[] array)
        {
            float[] result = new float[array.Length / FLOAT_SIZE];
            Buffer.BlockCopy(array, 0, result, 0, array.Length);
            return result;
        }

        // DOUBLE
        private static byte[] Encode(double[] array)
        {
            byte[] byteArray = new byte[array.Length * DOUBLE_SIZE];
            Buffer.BlockCopy(array, 0, byteArray, 0, array.Length);
            return byteArray;
        }

        private static double[] DecodeDouble(byte[] array)
        {
            double[] result = new double[array.Length / DOUBLE_SIZE];
            Buffer.BlockCopy(array, 0, result, 0, array.Length);
            return result;
        }

        // CHAR
        private static byte[] Encode(char[] array)
        {
            byte[] byteArray = new byte[array.Length * CHAR_SIZE];
            Buffer.BlockCopy(array, 0, byteArray, 0, array.Length);
            return byteArray;
        }

        private static char[] DecodeChar(byte[] array)
        {
            char[] result = new char[array.Length / CHAR_SIZE];
            Buffer.BlockCopy(array, 0, result, 0, array.Length);
            return result;
        }

        // BOOL
        private static byte[] Encode(bool[] array)
        {
            byte[] byteArray = new byte[array.Length / 8];
            for (int entry = 0; entry < byteArray.Length; entry++)
            {
                for (int bit = 0; bit < 8; bit++)
                {
                    if (array[entry * 8 + bit])
                    {
                        byteArray[entry] |= (byte)(128 >> bit);
                    }
                }
            }
            return byteArray;
        }

        private static bool[] DecodeBool(byte[] array)
        {
            bool[] result = new bool[array.Length * 8];
            for (int i = 0; i < array.Length; i++)
            {
                for (int j = i * 8, k = 0; k < 8; j++, k++)
                {
                    result[j] = (array[i] & boolIdx[k]) != 0;
                }
            }
            return result;
        }
    }
}
