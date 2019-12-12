package at.enfilo.def.library.api.util;

import at.enfilo.def.domain.entity.RoutineBinary;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class RoutineBinaryFactory {
	private static final String SHA1 = "SHA-1";
	private static final String MD5 = "MD5";

	/**
	 * Helper method that load jar file and returns it as a routine binary.
	 *
	 * @param file jar to be used as source.
	 * @param isPrimary value that will be set for isPrimary routine binary property.
	 * @return interpreted routine binary.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public RoutineBinary createRoutineBinary(File file, boolean isPrimary) throws IOException, NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance(SHA1);
		MessageDigest md5 = MessageDigest.getInstance(MD5);

		ByteBuffer bb = ByteBuffer.allocate((int)file.length());
		byte[] buffer = new byte[1024];
		int bytesCount;

		try (FileInputStream fis = new FileInputStream(file)) {
			while ((bytesCount = fis.read(buffer)) != -1) {
				bb.put(buffer, 0, bytesCount);
				md5.update(buffer, 0, bytesCount);
				sha1.update(buffer, 0, bytesCount);
			}
		}

		HexBinaryAdapter hexBinaryAdapter = new HexBinaryAdapter();
		RoutineBinary binary = new RoutineBinary(
				UUID.nameUUIDFromBytes(file.getName().getBytes()).toString(),
				hexBinaryAdapter.marshal(md5.digest()),
				hexBinaryAdapter.marshal(sha1.digest()),
				file.length(),
				isPrimary,
				file.toURI().toURL().toString()
		);
		binary.setData(bb.array());
		return binary;
	}
}
