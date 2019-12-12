package at.enfilo.def.dto.cache;

import at.enfilo.def.transfer.UnknownTaskException;
import org.apache.thrift.TBase;

import java.io.IOException;

public interface IDTOPersistenceDriver<T extends TBase> {

	/**
	 * Initialize persistence driver.
	 * @param baseUrl
	 */
	void init(String baseUrl, String context);

	/**
	 * Read a specific DTO from persistence.
	 * @param id - DTO id to read.
	 * @param instance - a new empty instance.
	 * @return task instance
	 * @throws IOException
	 * @throws UnknownTaskException
	 */
	T read(String id, T instance) throws IOException;

	/**
	 * Write a DTO to persistence layer.
	 * @param t - DTO to store.
	 * @throws IOException
	 */
	void write(String id, T t) throws IOException;

	/**
	 * Delete a specific DTO from persistence.
	 * @param id - DTO id to delete.
	 */
	void remove(String id);

	/**
	 * Delete all from persistence.
	 */
	void removeAll();

	/**
	 * Returns true if a DTO is stored under given id.
	 * @param id - DTO id.
	 * @return
	 */
	boolean exists(String id);
}
