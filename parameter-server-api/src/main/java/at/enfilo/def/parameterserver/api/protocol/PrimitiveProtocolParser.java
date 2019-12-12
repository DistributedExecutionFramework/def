package at.enfilo.def.parameterserver.api.protocol;

import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.nio.*;

public class PrimitiveProtocolParser implements IParameterProtocolParser {

    private static final int SHORT_SIZE = 2;
    private static final int LONG_SIZE = 8;
    private static final int INT_SIZE = 4;
    private static final int FLOAT_SIZE = 4;
    private static final int DOUBLE_SIZE = 8;
    private static final int CHAR_SIZE = 2;

    private static final byte[] boolIdx = new byte[]{(byte)0x80, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1};

    @Override
    public ParameterProtocol getAssociation() {
        return ParameterProtocol.PRIMITIVE;
    }

    @Override
    public Object decode(ResourceDTO parameter) throws ProtocolParseException {
        Object result;
        byte[] data = parameter.getData();
        if (parameter.getDataTypeId() == null) {
            throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Type id is null"));
        }
        if (data == null) {
            return null;
        }

        switch (parameter.getDataTypeId().toLowerCase()) {
            case "byte":
                result = parameter.getData();
                break;
            case "short":
                if (data.length % SHORT_SIZE != 0) {
                    throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Data length does not match short type"));
                }
                result = decodeShort(data);
                break;
            case "int":
                if (data.length % INT_SIZE != 0) {
                    throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Data length does not match int type"));
                }
                result = decodeInt(data);
                break;
            case "long":
                if (data.length % LONG_SIZE != 0) {
                    throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Data length does not match long type"));
                }
                result = decodeLong(data);
                break;
            case "float":
                if (data.length % FLOAT_SIZE != 0) {
                    throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Data length does not match float type"));
                }
                result = decodeFloat(data);
                break;
            case "double":
                if (data.length % DOUBLE_SIZE != 0) {
                    throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Data length does not match double type"));
                }
                result = decodeDouble(data);
                break;
            case "char":
                if (data.length % CHAR_SIZE != 0) {
                    throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Data length does not match char type"));
                }
                result = decodeChar(data);
                break;
            case "boolean":
                result = decodeBoolean(data);
                break;
            default:
                throw new ProtocolParseException(new IllegalArgumentException("Unknown type id"));
        }
        return result;
    }

    @Override
    public ResourceDTO encode(Object data, String typeId) throws ProtocolParseException {
        ResourceDTO result = new ResourceDTO();
        result.setDataTypeId(typeId);

        if (data == null) {
            return result;
        }

        switch (typeId) {
            case "byte":
                result.setData((byte[]) data);
                break;
            case "short":
                result.setData(encode((short[]) data));
                break;
            case "int":
                result.setData(encode((int[]) data));
                break;
            case "long":
                result.setData(encode((long[]) data));
                break;
            case "float":
                result.setData(encode((float[]) data));
                break;
            case "double":
                result.setData(encode((double[]) data));
                break;
            case "char":
                result.setData(encode((char[]) data));
                break;
            case "boolean":
                result.setData(encode((boolean[]) data));
                break;
            default:
                throw new ProtocolParseException(new IllegalArgumentException("Unknown type id"));
        }
        return result;
    }

    private static byte[] encode(boolean[] array) {
        byte[] byteArray = new byte[array.length / 8];
        for (int entry = 0; entry < byteArray.length; entry++) {
            for (int bit = 0; bit < 8; bit++) {
                if (array[entry * 8 + bit]) {
                    byteArray[entry] |= (128 >> bit);
                }
            }
        }
        return byteArray;
    }

    private static byte[] encode(float[] array) {
        byte[] byteArray = new byte[array.length * FLOAT_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        FloatBuffer buf = byteBuf.asFloatBuffer();
        buf.put(array);
        return byteArray;
    }

    private static byte[] encode(double[] array) {
        byte[] byteArray = new byte[array.length * DOUBLE_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        DoubleBuffer buf = byteBuf.asDoubleBuffer();
        buf.put(array);
        return byteArray;
    }

    private static byte[] encode(int[] array) {
        byte[] byteArray = new byte[array.length * INT_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        IntBuffer buf = byteBuf.asIntBuffer();
        buf.put(array);
        return byteArray;
    }

    private static byte[] encode(long[] array) {
        byte[] byteArray = new byte[array.length * LONG_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        LongBuffer buf = byteBuf.asLongBuffer();
        buf.put(array);
        return byteArray;
    }

    private static byte[] encode(char[] array) {
        byte[] byteArray = new byte[array.length * CHAR_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        CharBuffer buf = byteBuf.asCharBuffer();
        buf.put(array);
        return byteArray;
    }

    private static byte[] encode(short[] array) {
        byte[] byteArray = new byte[array.length * SHORT_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        ShortBuffer buf = byteBuf.asShortBuffer();
        buf.put(array);
        return byteArray;
    }


    private static float[] decodeFloat(byte[] array) {
        float[] result = new float[array.length / FLOAT_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(array);
        FloatBuffer buf = byteBuf.asFloatBuffer();
        buf.get(result);
        return result;
    }


    private static double[] decodeDouble(byte[] array) {
        double[] result = new double[array.length / DOUBLE_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(array);
        DoubleBuffer buf = byteBuf.asDoubleBuffer();
        buf.get(result);
        return result;
    }

    private static int[] decodeInt(byte[] array) {
        int[] result = new int[array.length / INT_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(array);
        IntBuffer buf = byteBuf.asIntBuffer();
        buf.get(result);
        return result;
    }

    private static long[] decodeLong(byte[] array) {
        long[] result = new long[array.length / LONG_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(array);
        LongBuffer buf = byteBuf.asLongBuffer();
        buf.get(result);
        return result;
    }

    private static char[] decodeChar(byte[] array) {
        char[] result = new char[array.length / CHAR_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(array);
        CharBuffer buf = byteBuf.asCharBuffer();
        buf.get(result);
        return result;
    }

    private static short[] decodeShort(byte[] array) {
        short[] result = new short[array.length / SHORT_SIZE];
        ByteBuffer byteBuf = ByteBuffer.wrap(array);
        ShortBuffer buf = byteBuf.asShortBuffer();
        buf.get(result);
        return result;
    }

    private static boolean[] decodeBoolean(byte[] array) {
        boolean [] result = new boolean[array.length * 8];
        for(int i = 0; i < array.length; i++){
            for(int j = i * 8, k = 0; k < 8; j++, k++){
                result[j] = (array[i] & boolIdx[k]) != 0;
            }
        }
        return result;
    }
}
