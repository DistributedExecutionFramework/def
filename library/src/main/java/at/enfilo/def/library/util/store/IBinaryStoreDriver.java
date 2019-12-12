package at.enfilo.def.library.util.store;

import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public interface IBinaryStoreDriver {
	/**
	 * Returns true if a binary exists in storage for the given id.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @return false if not
	 */
	boolean exists(String routineBinaryId);

	/**
	 * Read RoutineBinary from storage.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @param routineBinaryName - name of the routine binary
	 * @return RoutineBinary instance
	 * @throws IOException - if the read operation failed
	 * @throws NoSuchAlgorithmException - if MD5 algorithm is missing
	 */
	RoutineBinaryDTO read(String routineBinaryId, String routineBinaryName) throws IOException, NoSuchAlgorithmException;

	/**
	 * Read RoutineBinary from a given URL.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @param routineBinaryName - name of the routine binary
	 * @param url - location of binary
	 * @return RoutineBinary instance
	 * @throws IOException - if the read operation failed
	 * @throws NoSuchAlgorithmException - if MD5 algorithm is missing
	 */
	RoutineBinaryDTO read(String routineBinaryId, String routineBinaryName, URL url) throws IOException, NoSuchAlgorithmException;

	/**
	 * Reads a chunk from given URL (RoutineBinary)
	 * offset = chunk * chunkSize, len = chunkSize
	 * @param url - URL to RoutineBinary
	 * @param chunk - # of chunk to read
	 * @param chunkSize - size of chunk
	 * @return RoutineBinaryChunk instance
	 */
	RoutineBinaryChunkDTO readChunk(URL url, short chunk, int chunkSize) throws IOException;

	/**
	 * Returns MD5 sum of the requested RoutineBinary (id)
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @return MD5 sum as string
	 * @throws IOException - if the read operation failed
	 * @throws NoSuchAlgorithmException - if MD5 algorithm is missing
	 */
	String md5(String routineBinaryId) throws IOException, NoSuchAlgorithmException;

	/**
	 * Create a space for RoutineBinary data and returns the store location.
	 * @param routineId - Id of Routine
	 * @param routineBinary - RoutineBinary to create
	 * @return Store location as URL.
	 * @throws IOException - if the write operation failed
	 */
	URL create(String routineId, RoutineBinaryDTO routineBinary) throws IOException;

	/**
	 * Stores a chunk to given RoutineBinary.
	 * @param routineBinaryId - RoutineBinary
	 * @param chunk - Binary chunk to store.
	 * @throws IOException - if the write operation failed
	 */
	void storeChunk(String routineBinaryId, RoutineBinaryChunkDTO chunk) throws IOException;

	/**
	 * Delete a RoutineBinary from storage.
	 * @param routineId - id of the routine the routine binary belongs to
	 * @param routineBinaryId - id of the routine binary
	 * @param routineBinaryName - name of the routine binary
	 * @throws IOException - if the delete operation failed
	 */
	void delete(String routineId, String routineBinaryId, String routineBinaryName) throws IOException;

	/**
	 * Returns the URL of store location.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @return Store location as URL
	 * @throws MalformedURLException - if the URL has the wrong format.
	 */
	URL getFileURL(String routineBinaryId) throws MalformedURLException;

	/**
	 * Returns the URL of the exeuction link.
	 * @param routineId - id of the routine the binary belongs to
	 * @param routineBinaryName - name of the routine binary (and its link)
	 * @return Execution link location as URL
	 * @throws MalformedURLException
	 */
	URL getExecutionURL(String routineId, String routineBinaryId, String routineBinaryName) throws MalformedURLException, IOException;

	/**
	 * Returns the size in bytes from the stored RoutineBinary.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @return size in bytes. -1 if RoutineBinary is missing.
	 */
	long getSizeInBytes(String routineBinaryId) throws IOException;
}
