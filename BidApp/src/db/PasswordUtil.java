package db;

import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class PasswordUtil {

    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    private static final int ITERATIONS = 65536; // Recomendado por NIST
    private static final int KEY_LENGTH = 256; // Longitud de la clave en bits

    public static String encryptPassword(String password) throws Exception {
        byte[] salt = generateSalt(); 
        char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH);
        
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();

        // Convertimos el sal y el hash a Base64 para almacenamiento
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        return saltBase64 + ":" + hashBase64;
    }

    public static boolean verifyPassword(String password, String storedPassword) {
        try {
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                return false; 
            }

            String saltBase64 = parts[0];
            String storedHashBase64 = parts[1];

            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] storedHash = Base64.getDecoder().decode(storedHashBase64);

            // Ahora verificamos la contraseña ingresada generando su hash con el mismo sal
            char[] passwordChars = password.toCharArray();
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(storedHash, hash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
}
