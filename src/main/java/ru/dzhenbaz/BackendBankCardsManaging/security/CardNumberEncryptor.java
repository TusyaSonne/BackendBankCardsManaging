package ru.dzhenbaz.BackendBankCardsManaging.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Converter
public class CardNumberEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";

    @Value("${security.encryption-key}")
    private String secretKey;



    private Cipher getEncryptCipher() throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        return c;
    }

    private Cipher getDecryptCipher() throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        return c;
    }

    @Override
    public String convertToDatabaseColumn(String cardNumber) {
        try {
            if (cardNumber == null) return null;
            Cipher cipher = getEncryptCipher();
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt card number", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encrypted) {
        try {
            if (encrypted == null) return null;
            Cipher cipher = getDecryptCipher();
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt card number", e);
        }
    }
}
