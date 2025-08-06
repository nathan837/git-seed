import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESFileCrypto {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final int BUFFER_SIZE = 8192;

    private static SecretKeySpec getKeyFromPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes());
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static void encryptFile(String inputFile, String password) throws Exception {
        // Generate random IV
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        
        try (InputStream in = Files.newInputStream(Paths.get(inputFile));
             OutputStream out = Files.newOutputStream(Paths.get(inputFile + ".enc"))) {
            
            out.write(iv);
            
            // Initialize cipher with IV
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(password), new IvParameterSpec(iv));
            
            // Encrypt the file content
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) out.write(output);
            }
            
            // Write final crypted block
            byte[] output = cipher.doFinal();
            if (output != null) out.write(output);
        }
        System.out.println("[+] Encrypted: " + inputFile + " -> " + inputFile + ".enc");
    }

    public static void decryptFile(String inputFile, String password) throws Exception {
        try (InputStream in = Files.newInputStream(Paths.get(inputFile))) {
            // Read IV from the beginning of the file
            byte[] iv = new byte[IV_LENGTH];
            int ivBytesRead = in.read(iv);
            if (ivBytesRead != IV_LENGTH) {
                throw new IOException("Invalid IV in encrypted file (read " + ivBytesRead + " bytes, expected " + IV_LENGTH + ")");
            }
            
            // Determine output filename
            String outputFile = inputFile.replace(".enc", "");
            if (outputFile.equals(inputFile)) {
                outputFile = inputFile + ".dec";
            }
            
            try (OutputStream out = Files.newOutputStream(Paths.get(outputFile))) {
                // Initialize cipher with IV
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(password), new IvParameterSpec(iv));
                
                // Decrypt the file content
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) out.write(output);
                }
                
                // Write final block
                byte[] output = cipher.doFinal();
                if (output != null) out.write(output);
            }
            System.out.println("[+] Decrypted: " + inputFile + " -> " + outputFile);
        }
    }

}