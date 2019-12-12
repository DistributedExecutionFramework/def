package at.enfilo.def.transfer.util;

import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class RoutineBinaryFactory {
	private static final String MD5 = "MD5";

	/**
	 * Creates a RoutineBinaryDTO from a given file.
	 * @param file - file to create RoutineBinaryDTO object from
	 * @param isPrimary - true if the given file is the primary binary
	 * @param routineBinaryName - name of routine binary
	 * @return RoutineBinaryDTO instance based on the given file.
	 * @throws IOException in case of file read error
	 * @throws NoSuchAlgorithmException in case of MD5 algorithm cannot be found
	 */
	public static RoutineBinaryDTO createFromFile(File file, boolean isPrimary, String routineBinaryName)
	throws IOException, NoSuchAlgorithmException {
		return createFromFile(file, isPrimary, routineBinaryName, UUID.randomUUID().toString());
	}

	/**
	 * Creates a RoutineBinaryDTO from a given file.
	 * @param file - file to create RoutineBinaryDTO object from
	 * @param isPrimary - true if the given file is the primary binary
	 * @param routineBinaryName - name of routine binary
	 * @param rbId - routine binary id
	 * @return RoutineBinaryDTO instance based on the given file.
	 * @throws IOException in case of file read error
	 * @throws NoSuchAlgorithmException in case of MD5 algorithm cannot be found
	 */
	public static RoutineBinaryDTO createFromFile(File file, boolean isPrimary, String routineBinaryName, String rbId)
	throws IOException, NoSuchAlgorithmException {
		RoutineBinaryDTO rb = new RoutineBinaryDTO();
		rb.setId(rbId);
		rb.setName(routineBinaryName);
		rb.setUrl(file.toURI().toURL().toString());
		rb.setSizeInBytes(file.length());
		rb.setPrimary(isPrimary);
		rb.setMd5(md5(file));
		return rb;
	}

	/**
	 * Returns md5 checksum for the given file.
	 * @param file - file for checksum creation
	 * @return md5 checksum as string
	 * @throws IOException in case of file read error
	 * @throws NoSuchAlgorithmException in case of md5 algorithm cannot be found
	 */
	public static String md5(File file) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(MD5);
		try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md)) {
			while (dis.read() != -1) {
				// Read all
			}
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : md.digest()) {
			sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	/**
	 * Calculates the number of chunks for a given file and chunk size.
	 * @param file - file
	 * @param chunkSize - requested chunk size
	 * @return number of chunks
	 */
	public static short calculateChunks(File file, int chunkSize) {
		return calculateChunks(file.length(), chunkSize);
	}

	/**
	 * Calculates the number of chunks for a given RoutineBinary and chunk size.
	 * @param routineBinary - RoutineBinary DTO
	 * @param chunkSize - requested chunk size
	 * @return number of chunks
	 */
	public static short calculateChunks(RoutineBinaryDTO routineBinary, int chunkSize) {
		return calculateChunks(routineBinary.getSizeInBytes(), chunkSize);
	}

	private static short calculateChunks(long fileSize, int chunkSize) {
		short totalChunks = (short)Math.ceil((double) fileSize / (double) chunkSize);
		return totalChunks == 0 ? 1 : totalChunks;
	}

	/**
	 * Reads a chunk from a file.
	 * @param file - file to read chunk from
	 * @param chunk - nr of chunk
	 * @param chunkSize - chunk size
	 * @return chunk instance
	 * @throws IOException in case of file read error.
	 */
	public static RoutineBinaryChunkDTO readChunk(File file, short chunk, int chunkSize) throws IOException {
		RoutineBinaryChunkDTO rbc = new RoutineBinaryChunkDTO();
		rbc.setChunk(chunk);
		rbc.setChunkSize(chunkSize);
		byte[] buf = new byte[chunkSize];
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			short totalChunks = (short)(raf.length() / chunkSize);
			totalChunks = totalChunks == 0 ? 1 : totalChunks;
			rbc.setTotalChunks(totalChunks);
			raf.seek(chunk * chunkSize);
			int size = raf.read(buf);
			if (size != chunkSize) {
				buf = Arrays.copyOf(buf, size);
			}
			rbc.setData(buf);
		}
		return rbc;
	}

	/**
	 * Stores a chunk to a file.
	 * @param file - file to store the chunk.
	 * @param chunk - chunk to store.
	 * @throws IOException in case of file read/write error.
	 */
	public static void storeChunk(File file, RoutineBinaryChunkDTO chunk) throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.seek(chunk.getChunk() * chunk.getChunkSize());
			raf.write(chunk.getData());
		}
	}
}
