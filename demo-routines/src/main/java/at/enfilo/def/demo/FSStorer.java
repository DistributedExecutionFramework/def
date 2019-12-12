package at.enfilo.def.demo;

import at.enfilo.def.routine.LogLevel;
import at.enfilo.def.routine.RoutineException;
import at.enfilo.def.routine.StoreRoutine;
import at.enfilo.def.routine.api.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

public class FSStorer extends StoreRoutine {
	private static final String ROOT_PATH_KEY = "root_path";

	private Path rootPath = Paths.get("");

	@Override
	protected Result store(String key, byte[] data, int tupleSeq) throws IOException {
		// create destiniation file
		File dest = createDestination();

		// Store value
		try (FileOutputStream out = new FileOutputStream(dest)) {
			out.write(data);
			out.flush();
		}

		return new Result(tupleSeq, key, dest.toURI().toURL().toString(), null);
	}

	/**
	 * Create destination file
	 * @return
	 */
	File createDestination() throws IOException {
		Path path = Paths.get(rootPath.toAbsolutePath().toString());
		path.toFile().mkdirs();
		File dest = path.resolve(UUID.randomUUID().toString()).toFile();
		dest.createNewFile();
		return dest;
	}

	@Override
	protected void shutdownStorage() {
		// Nothing to do
	}

	@Override
	protected void setupStorage() {
		rootPath.toFile().mkdirs();
	}

	@Override
	protected void configure(String configFile) {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader(configFile));
			rootPath = Paths.get(properties.getProperty(ROOT_PATH_KEY, rootPath.toString()));

		} catch (IOException e) {
			log(LogLevel.ERROR, String.format("Error while reading configuration file: %s", e.getMessage()));
			throw new RoutineException(e);
		}
	}

	public Path getRootPath() {
		return rootPath;
	}

	public void setRootPath(Path rootPath) {
		this.rootPath = rootPath;
	}
}
