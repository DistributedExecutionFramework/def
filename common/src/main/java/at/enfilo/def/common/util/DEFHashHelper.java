package at.enfilo.def.common.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by mase on 18.08.2016.
 */
public class DEFHashHelper {

    private static final String DEFAULT_HASH_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int DEFAULT_SECURE_RANDOM_LENGTH = 128;
    private static final int DEFAULT_HASH_LENGTH = 512;
    private static final int DEFAULT_HASH_ITERATIONS = 65536;

    private DEFHashHelper() {
        // Hiding public constructor
    }

    public static String generateNewToken() {
        byte[] uuidBytes = UUID.randomUUID().toString().getBytes();
        byte[] randomBytes = getSecureRandomValue();

        // Merging two generated values into one byte array.
        byte[] generatedBytes = new byte[uuidBytes.length + randomBytes.length];
        System.arraycopy(uuidBytes, 0, generatedBytes, 0, uuidBytes.length);
        System.arraycopy(randomBytes, 0, generatedBytes, uuidBytes.length, randomBytes.length);

        // Encoding token to suitable format (not necessary).
        return Base64.getEncoder().encodeToString(generatedBytes);
    }

    public static String generateNewSalt() {
        return Base64.getEncoder().encodeToString(getSecureRandomValue());
    }

    public static String getPasswordHash(String password, String salt)
    throws SecurityException {
        try {

            KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt.getBytes(),
                DEFAULT_HASH_ITERATIONS,
                DEFAULT_HASH_LENGTH
            );

            SecretKeyFactory f = SecretKeyFactory.getInstance(DEFAULT_HASH_ALGORITHM);
            byte[] hash = f.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    public static boolean doPasswordMatch(String password, String salt, String storedPasswordHash)
    throws SecurityException {
        String receivedPasswordHash = getPasswordHash(password, salt);
        return receivedPasswordHash.equals(storedPasswordHash);
    }

    private static byte[] getSecureRandomValue() {
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[DEFAULT_SECURE_RANDOM_LENGTH];
        random.nextBytes(values);

        return values;
    }
}
