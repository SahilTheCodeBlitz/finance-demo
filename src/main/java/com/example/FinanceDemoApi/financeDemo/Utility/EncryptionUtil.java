package com.example.FinanceDemoApi.financeDemo.Utility;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class EncryptionUtil {

    @Value("${aes.key}")
    private String key;

    @Value("${aes.initVector}")
    private String initVector;

    @Value("${aes.algorithm}")
    private String algo;

    // Utility method for encryption
    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8)));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Utility method for decryption
    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8)));
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
