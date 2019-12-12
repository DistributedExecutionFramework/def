package at.enfilo.def.library.util.store.driver.fs;

import at.enfilo.def.library.Library;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
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
	public boolean exists(String routineBinaryID) {
		File f = createPath(routineBinaryID).toFile();
		if (f.exists()) {
			f.setExecutable(true);
			return true;
		}
		return false;
	}

	@Override
	public RoutineBinaryDTO read(String routineBinaryId) throws IOException, NoSuchAlgorithmException {
		return read(routineBinaryId, createPath(routineBinaryId).toUri().toURL());
	}

	@Override
	public RoutineBinaryDTO read(String routineBinaryId, URL url) throws IOException, NoSuchAlgorithmException {
		Path p = Paths.get(url.getFile());

		RoutineBinaryDTO rb = new RoutineBinaryDTO();
		rb.setId(routineBinaryId);
		rb.setUrl(url.toString());
		rb.setData(Files.readAllBytes(p));
		rb.setSizeInBytes(Files.size(p));
		try (InputStream is = new ByteArrayInputStream(rb.getData())) {
			rb.setMd5(md5(is));
		}
		return rb;
	}

	@Override
	public String md5(String routineBinaryId) throws IOException, NoSuchAlgorithmException {
		try (InputStream is = Files.newInputStream(createPath(routineBinaryId))) {
			return md5(is);
		}
	}

	private String md5(InputStream is) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		try (DigestInputStream dis = new DigestInputStream(is, md))
		{
			while (dis.read() != -1);
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : md.digest()) {
			sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();

	}

	@Override
	public URL store(RoutineBinaryDTO routineBinary) throws IOException {
		File file = createPath(routineBinary.getId()).toFile();
		try (OutputStream os = new FileOutputStream(file)) {
			os.write(routineBinary.getData());
		}
		file.setExecutable(true);
		return file.toURI().toURL();
	}

	@Override
	public void delete(String routineBinaryId) throws IOException {
		Files.deleteIfExists(createPath(routineBinaryId));
	}

	@Override
	public URL getURL(String rbId) throws MalformedURLException {
		return createPath(rbId).toUri().toURL();
	}

	@Override
	public long getSizeInBytes(String routineBinaryId) throws IOException {
		Path p = createPath(routineBinaryId);
		if (Files.exists(p)) {
			return Files.size(p);
		}
		return -1;
	}

	private Path createPath(String routineBinaryId) {
		return BASE_PATH.resolve(routineBinaryId);
	}
}
