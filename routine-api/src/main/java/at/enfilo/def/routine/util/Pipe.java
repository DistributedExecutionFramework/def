package at.enfilo.def.routine.util;

import at.enfilo.def.routine.api.IPipeReader;
import at.enfilo.def.routine.api.IPipeWriter;

import java.io.*;
import java.nio.file.Paths;

/**
 * Pipe - connects PipeInConnector to PipeOutConnector.
 */
public class Pipe {

	private final File pipe;

	private IPipeWriter writer;
	private IPipeReader reader;

	/**
	 * Create pipe with a name.
	 *
	 * @param pipe - pipe reference.
	 */
	public Pipe(File pipe) {
		this.pipe = pipe;
	}

	/**
	 * Create pipe with a name.
	 *
	 * @param path - path to pipe (base dir).
	 * @param name - name of the pipe.
	 */
	public Pipe(String path, String name) {
		this(Paths.get(path, name).toFile());
	}

	/**
	 * Returns connection state of both in and out connector as boolean.
	 *
	 * @return {@code true} if both in and out connector are set, otherwise returns {@code false}.
	 */
	public boolean isFullyConnected() {
		return (writer != null) && (reader != null);
	}

	/**
	 * Get output connector.
	 *
	 * @return instance of IPipeReader (input connector).
	 */
	public IPipeReader getReader() {
		return reader;
	}

	/**
	 * Sets reader (input connector).
	 *
	 * @param reader - instance of input connector.
	 */
	public void setReader(IPipeReader reader) {
		this.reader = reader;
	}

	/**
	 * Get input connector.
	 * @return instance of IPipeWriter (output connector).
	 */
	public IPipeWriter getWriter() {
		return writer;
	}

	/**
	 * Sets writer (output connector).
	 *
	 * @param writer - instance of output connector.
	 */
	public void setWriter(IPipeWriter writer) {
		this.writer = writer;
	}

	/**
	 * Returns InputStream for this Pipe.
	 *
	 * @return InputStream for specified pipe name and path.
	 * @throws FileNotFoundException if Pipe file not exists.
	 */
	public InputStream getInputStream()
	throws FileNotFoundException {
		return new FileInputStream(resolve());
	}

	/**
	 * Returns OutputStream for this Pipe.
	 *
	 * @return OutputStream for specified pipe name and path.
	 * @throws FileNotFoundException if Pipe file not exists.
	 */
	public OutputStream getOutputStream()
	throws FileNotFoundException {
		return new FileOutputStream(resolve());
	}

	/**
	 * Returns name of pipe.
	 *
	 * @return name of the pipe.
	 */
	public String getName() {
		return pipe.getName();
	}

	/**
	 * Construct full pipe file name / path by a given root path from configuration.
	 *
	 * @return path of the pipe.
	 */
	public File resolve() {
		return pipe.getAbsoluteFile();
	}

	@Override
	public String toString() {
		return String.format("Pipe{pipePath=%s}", resolve());
	}
}
