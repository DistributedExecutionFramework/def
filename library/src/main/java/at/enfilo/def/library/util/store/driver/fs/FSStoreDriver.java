package at.enfilo.def.library.util.store.driver.fs;

import at.enfilo.def.library.Library;
import at.enfilo.def.transfer.util.RoutineBinaryFactory;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class FSStoreDriver implements IBinaryStoreDriver {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(FSStoreDriver.class);
	private static final Path BASE_PATH;

	static {
		String basePath;
		try {
			basePath = Library.getInstance().getConfiguration().getStoreEndpointUrl().getPath();
		} catch (MalformedURLException e) {
			basePath = Paths.get("/tmp/def/routine-binaries").toString();
		}
		BASE_PATH = Paths.get(basePath);
		if (!Files.exists(BASE_PATH)) {
			try {
				Files.createDirectories(BASE_PATH);
			} catch (IOException e) {
				LOGGER.error("Error while setup base directory {}.", BASE_PATH, e);
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean exists(String routineBinaryId) {
		File f = createPathToFile(routineBinaryId).toFile();
		return f.exists();
	}

	private boolean executionLinkExists(String routineId, String routineBinaryName) {
		File f = createPathToLink(routineId, routineBinaryName).toFile();
		return f.exists();
	}

	@Override
	public RoutineBinaryDTO read(String routineBinaryId, String routineBinaryName) throws IOException, NoSuchAlgorithmException {
		return read(routineBinaryId, routineBinaryName, createPathToFile(routineBinaryId).toUri().toURL());
	}

	@Override
	public RoutineBinaryDTO read(String routineBinaryId, String routineBinaryName, URL url) throws IOException, NoSuchAlgorithmException {
		return RoutineBinaryFactory.createFromFile(new File(url.getFile()), false, routineBinaryName, routineBinaryId);
	}

	@Override
	public RoutineBinaryChunkDTO readChunk(URL url, short chunk, int chunkSize) throws IOException {
		return RoutineBinaryFactory.readChunk(new File(url.getFile()), chunk, chunkSize);
	}

	@Override
	public String md5(String routineBinaryId) throws IOException, NoSuchAlgorithmException {
		File file = createPathToFile(routineBinaryId).toFile();
		return RoutineBinaryFactory.md5(file);
	}

	@Override
	public URL create(String rId, RoutineBinaryDTO routineBinaryDTO) throws IOException {
		Path filePath = createPathToFile(routineBinaryDTO.getId());
		File file = filePath.toFile();

		if (!file.exists()) {
			file.createNewFile();
		}

		boolean result = file.setExecutable(true);
		if (!result) {
			LOGGER.warn("Cannot set File {} executable.", file);
		}

		createExecutionLink(rId, routineBinaryDTO.getName(), filePath);

		return file.toURI().toURL();
	}

	@Override
	public void storeChunk(String routineBinaryId, RoutineBinaryChunkDTO chunk) throws IOException {
		File file = createPathToFile(routineBinaryId).toFile();
		RoutineBinaryFactory.storeChunk(file, chunk);
	}

	@Override
	public void delete(String routineId, String routineBinaryId, String routineBinaryName) throws IOException {
		Files.deleteIfExists(createPathToFile(routineBinaryId));
	    Files.deleteIfExists(createPathToLink(routineId, routineBinaryName));
	}

	@Override
	public URL getFileURL(String rbId) throws MalformedURLException {
		return createPathToFile(rbId).toUri().toURL();
	}

	@Override
	public URL getExecutionURL(String routineId, String routineBinaryId, String routineBinaryName) throws IOException {
		if (!executionLinkExists(routineId, routineBinaryName)) {
			createExecutionLink(routineId, routineBinaryName, createPathToFile(routineBinaryId));
		}
		return createPathToLink(routineId, routineBinaryName).toUri().toURL();
	}

	@Override
	public long getSizeInBytes(String routineBinaryId) throws IOException {
		Path p = createPathToFile(routineBinaryId);
		if (Files.exists(p)) {
			return Files.size(p);
		}
		return -1;
	}

	private Path createPathToFile(String routineBinaryId) {
		return BASE_PATH.resolve(routineBinaryId);
	}

	private Path createPathToLink(String routineId, String binaryName) {
	    return BASE_PATH.resolve(routineId).resolve(binaryName);
    }

    private void createExecutionLink(String routineId, String binaryName, Path filePath) throws IOException{
		Path linkPath = createPathToLink(routineId, binaryName);
		Path parentDirectoryPath = linkPath.getParent();
		if (!Files.exists(parentDirectoryPath)) {
			try {
				Files.createDirectories(parentDirectoryPath);
			} catch (IOException e) {
				LOGGER.error("Error while setup routine binary directory {}.", linkPath, e);
				throw new RuntimeException(e);
			}
		}

		// Remove link if exists
		if (Files.exists(linkPath)) {
			Files.delete(linkPath);
		}

		// Create hard link from origin to /base/path/{routineId}/{routineBinaryName}
		Files.createLink(linkPath, filePath);
		if (!linkPath.toFile().setExecutable(true)) {
			LOGGER.warn("Can not set link {} to executable.", linkPath);
		}
	}
}
