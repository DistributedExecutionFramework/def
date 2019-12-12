package at.enfilo.def.library.util.store.driver.redis;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by mase on 11.05.2017.
 */
public class RedisClient implements Closeable, AutoCloseable {

    private static final byte ASTERISK_SIGN = '*';
    private static final byte PLUS_SIGN = '+';
    private static final byte MINUS_SIGN = '-';
    private static final byte COLON_SIGN = ':';
    private static final byte DOLLAR_SIGN = '$';
    private static final byte CARRIAGE_RETURN_SIGN = '\r';
    private static final byte LINE_FEED_SIGN = '\n';

    private static final Pattern ARRAY_ENTRY_PATTERN = Pattern.compile("[*+$:-]\\d+[^*+$:-]*");

    private final Socket socket;

    public RedisClient(String host, int port)
    throws IOException {
        this.socket = new Socket(host, port);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.setReuseAddress(true);
        socket.setSoLinger(true, 0);
    }

    public List<byte[]> time()
    throws IOException {
        try {
            byte[] data = assembleBulk(RedisCommand.TIME);
            Object response = submit(data);

            //noinspection unchecked
            return (List<byte[]>) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public long exists(byte[]... keys)
    throws IOException {
        try {
            byte[][] bulkKeys = Arrays.stream(keys).map(this::prepareBulk).toArray(byte[][]::new);

            byte[] data = assembleBulk(RedisCommand.EXISTS, bulkKeys);
            Object response = submit(data);

            return (Long) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public long strlen(byte[] key)
    throws IOException {
        try {
            byte[] bulkKey = prepareBulk(key);

            byte[] data = assembleBulk(RedisCommand.STRLEN, bulkKey);
            Object response = submit(data);

            return (Long) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String set(byte[] key, byte[] value)
    throws IOException {
        try {
            byte[] bulkKey = prepareBulk(key);
            byte[] bulkValue = prepareBulk(value);

            byte[] data = assembleBulk(RedisCommand.SET, bulkKey, bulkValue);
            Object response = submit(data);

            return (String) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public byte[] get(byte[] key)
    throws IOException {
        try {
            byte[] bulkKey = prepareBulk(key);

            byte[] data = assembleBulk(RedisCommand.GET, bulkKey);
            Object response = submit(data);

            return (byte[]) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public long del(byte[]... keys)
    throws IOException {
        try {
            byte[][] bulkKeys = Arrays.stream(keys).map(this::prepareBulk).toArray(byte[][]::new);

            byte[] data = assembleBulk(RedisCommand.DEL, bulkKeys);
            Object response = submit(data);

            return (Long) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public long setrange(byte[] key, int offset, byte[] value)
    throws IOException {
        try {
            byte[] bulkKey = prepareBulk(key);
            byte[] bulkOffset = prepareBulk(intToBytes(offset));
            byte[] bulkValue = prepareBulk(value);

            byte[] data = assembleBulk(RedisCommand.SETRANGE, bulkKey, bulkOffset, bulkValue);
            Object response = submit(data);

            return (Long) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public byte[] getrange(byte[] key, int offset, int endOffset)
    throws IOException {
        try {
            byte[] bulkKey = prepareBulk(key);
            byte[] bulkOffset = prepareBulk(intToBytes(offset));
            byte[] bulkEndOffset = prepareBulk(intToBytes(endOffset));

            byte[] data = assembleBulk(RedisCommand.GETRANGE, bulkKey, bulkOffset, bulkEndOffset);
            Object response = submit(data);

            return (byte[]) response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close()
    throws IOException {
        socket.close();
    }

    private Object submit(byte[] data)
    throws IOException {
        // Sending data.
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(data);
        outputStream.flush();

        // Reading response.
        InputStream inputStream = socket.getInputStream();
        return handleResponse(inputStream);
    }

    private byte[] assembleBulk(RedisCommand command, byte[]... bulkParts) {
        // Bulk header in this case should indicate the number of bulk parts + 1 (command part).
        byte[] bulkHeader = prepareBulkHeader(ASTERISK_SIGN, bulkParts.length + 1);

        // Preparing final bulk array.
        int bulkPartsLength = Arrays.stream(bulkParts).mapToInt(part -> part.length).sum();
        byte[] assembledBulk = new byte[bulkHeader.length + command.getBulkByteLength() + bulkPartsLength];

        // Writing bulk header.
        System.arraycopy(bulkHeader, 0, assembledBulk, 0, bulkHeader.length);

        // Writing bulk command bulkBytes.
        System.arraycopy(command.getBulkBytes(), 0, assembledBulk, bulkHeader.length, command.getBulkByteLength());

        // Writing all remaining bulk parts.
        int bulkOffset = bulkHeader.length + command.getBulkByteLength();
        for (byte[] bulkPart : bulkParts) {
            System.arraycopy(bulkPart, 0, assembledBulk, bulkOffset, bulkPart.length);
            bulkOffset += bulkPart.length;
        }

        return assembledBulk;
    }

    private byte[] prepareBulkHeader(byte leadingByte, int lengthBytes) {
        byte[] contentLengthPart = intToBytes(lengthBytes);

        // This array stores bytes that represents following format: $%d\r\n
        byte[] lengthPart = new byte[contentLengthPart.length + 3];

        // Adding leading byte - according to protocol may be of value: $ or *.
        lengthPart[0] = leadingByte;

        // Copying content length value in byte form into data array.
        System.arraycopy(contentLengthPart, 0, lengthPart, 1, contentLengthPart.length);

        // Adding closing separators.
        lengthPart[contentLengthPart.length + 1] = CARRIAGE_RETURN_SIGN;
        lengthPart[contentLengthPart.length + 2] = LINE_FEED_SIGN;

        return lengthPart;
    }

    private byte[] prepareBulk(byte[] bytes) {
        return prepareBulk(DOLLAR_SIGN, bytes);
    }

    private byte[] prepareBulk(byte leadingByte, byte[] bytes) {
        // Preparing bulk header part.
        byte[] bulkLengthPart = prepareBulkHeader(leadingByte, bytes.length);

        // This array stores bytes that represents following format: $%d\r\n%s\r\n
        byte[] data = new byte[bulkLengthPart.length + bytes.length + 2];

        // Copying bulk header in byte form into data array.
        System.arraycopy(bulkLengthPart, 0, data, 0, bulkLengthPart.length);

        // Copying content in byte form into data array.
        System.arraycopy(bytes, 0, data, bulkLengthPart.length, bytes.length);

        // Adding closing separators.
        data[data.length - 2] = CARRIAGE_RETURN_SIGN;
        data[data.length - 1] = LINE_FEED_SIGN;

        return data;
    }

    private Object handleResponse(InputStream inputStream)
    throws IOException {
        final byte responseIndicator = (byte) inputStream.read();
        switch (responseIndicator) {
            case PLUS_SIGN: return handleSimpleResponse(inputStream);

            case DOLLAR_SIGN: return handleBulkResponse(inputStream);

            case ASTERISK_SIGN: return handleArrayResponse(inputStream);

            case COLON_SIGN: return handleLongResponse(inputStream);

            case MINUS_SIGN: return handleExceptionResponse(inputStream);

            default: throw new IllegalArgumentException("Unknown response format: \"" + responseIndicator + "\".");
        }
    }

    private byte[] handleNextResponsePart(InputStream inputStream)
    throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int lastByte = -1;
        int currentByte;

        while ((currentByte = inputStream.read()) != -1) {
            buffer.write(currentByte);

            if (((byte) lastByte == CARRIAGE_RETURN_SIGN) && ((byte) currentByte == LINE_FEED_SIGN)) {
                byte[] headerBytes = buffer.toByteArray();
                return Arrays.copyOfRange(headerBytes, 0, headerBytes.length - 2);
            }

            lastByte = currentByte;
        }

        throw new IOException("Server has closed the connection. While response header was read.");
    }

    private String handleSimpleResponse(InputStream inputStream)
    throws IOException {
        return new String(handleNextResponsePart(inputStream));
    }

    private List<Object> handleArrayResponse(InputStream inputStream)
    throws IOException {
        // Will read only header (length part).
        int bodyLength = (int) handleLongResponse(inputStream);
        if (bodyLength == -1) return null;

        // Handling array entries.
        List<Object> responseList = new ArrayList<>(bodyLength);
        for (int i = 0; i < bodyLength; ++i) {
            responseList.add(handleResponse(inputStream));
        }

        return responseList;
    }

    private byte[] handleBulkResponse(InputStream inputStream)
    throws IOException {
        // Will read only header (length part).
        int bodyLength = (int) handleLongResponse(inputStream);
        if (bodyLength == -1) return null;

        // Preparing to read bulk body.
        int offset = 0;
        byte[] data = new byte[bodyLength];

        // Reading bulk body.
        while (offset < bodyLength) {
            int size = doCheckResponse(
                inputStream.read(data, offset, (bodyLength - offset))
            );

            offset += size;
        }

        // Read 2 closing bytes (separators).
        inputStream.read();
        inputStream.read();

        return data;
    }

    private long handleLongResponse(InputStream inputStream)
    throws IOException {
        return Long.parseLong(handleSimpleResponse(inputStream));
    }

    private Exception handleExceptionResponse(InputStream inputStream)
    throws IOException {
        Exception e = new Exception(handleSimpleResponse(inputStream));
        throw new IOException("Redis endpoint internal error.", e);
    }

    private int doCheckResponse(int response)
    throws IOException {
        if (response != -1) return response;
        else throw new IOException("Server has closed the connection.");
    }

    private enum RedisCommand {
        TIME("$4\r\nTIME\r\n"),
        SET("$3\r\nSET\r\n"),
        GET("$3\r\nGET\r\n"),
        DEL("$3\r\nDEL\r\n"),
        EXISTS("$6\r\nEXISTS\r\n"),
        STRLEN("$6\r\nSTRLEN\r\n"),
        SETRANGE("$8\r\nSETRANGE\r\n"),
        GETRANGE("$8\r\nGETRANGE\r\n");

        private final byte[] bulkBytes;

        RedisCommand(String bulkBytes) {
            this.bulkBytes = bulkBytes.getBytes();
        }

        private byte[] getBulkBytes() {
            return bulkBytes;
        }

        private int getBulkByteLength() {
            return bulkBytes.length;
        }
    }

    @Deprecated
    private static byte charToByte(char c) {
        return (byte) (c & 0x00FF);
    }

    private static byte[] intToBytes(int i) {
        return Integer.toString(i).getBytes();
    }

    private static int indexOf(byte b, byte[] array){
        int index = 0;
        while (index < array.length && b != array[index]) { ++index; }

        return index;
    }
}
