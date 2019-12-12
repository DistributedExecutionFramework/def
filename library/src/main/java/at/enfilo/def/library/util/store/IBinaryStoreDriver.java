package at.enfilo.def.library.util.store;

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
	 * @return RoutineBinary instance
	 * @throws IOException - if the read operation failed
	 * @throws NoSuchAlgorithmException - if MD5 algorithm is missing
	 */
	RoutineBinaryDTO read(String routineBinaryId) throws IOException, NoSuchAlgorithmException;

	/**
	 * Read RoutineBinary from a given URL.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @param url - location of binary
	 * @return RoutineBinary instance
	 * @throws IOException - if the read operation failed
	 * @throws NoSuchAlgorithmException - if MD5 algorithm is missing
	 */
	RoutineBinaryDTO read(String routineBinaryId, URL url) throws IOException, NoSuchAlgorithmException;

	/**
	 * Returns MD5 sum of the requested RoutineBinary (id)
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @return MD5 sum as string
	 * @throws IOException - if the read operation failed
	 * @throws NoSuchAlgorithmException - if MD5 algorithm is missing
	 */
	String md5(String routineBinaryId) throws IOException, NoSuchAlgorithmException;

	/**
	 * Stores the given RoutineBinary data and returns the store location.
	 * @param routineBinary - RoutineBinary to store
	 * @return Store location as URL.
	 * @throws IOException - if the write operation failed
	 */
	URL store(RoutineBinaryDTO routineBinary) throws IOException;

	/**
	 * Delete a RoutineBinary from storage.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @throws IOException - if the delete operation failed
	 */
	void delete(String routineBinaryId) throws IOException;

	/**
	 * Returns the URL of store location.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @return Store location as URL
	 * @throws MalformedURLException - if the URL has the wrong format.
	 */
	URL getURL(String routineBinaryId) throws MalformedURLException;

	/**
	 * Returns the size in bytes from the stored RoutineBinary.
	 * @param routineBinaryId - Unique RoutineBinary id
	 * @return size in bytes. -1 if RoutineBinary is missing.
	 */
	long getSizeInBytes(String routineBinaryId) throws IOException;
}
