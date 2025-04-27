package ru.dzhenbaz.BackendBankCardsManaging.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Конвертер для шифрования и расшифровки номеров банковских карт при сохранении в базу данных.
 * Использует алгоритм AES для обеспечения безопасности хранения данных.
 */
@Component
@Converter
public class CardNumberEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";

    @Value("${security.encryption-key}")
    private String secretKey;

    /**
     * Создает Cipher для шифрования данных.
     *
     * @return объект {@link Cipher} в режиме шифрования
     * @throws Exception если не удалось создать Cipher
     */
    private Cipher getEncryptCipher() throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        return c;
    }

    /**
     * Создает Cipher для расшифровки данных.
     *
     * @return объект {@link Cipher} в режиме расшифровки
     * @throws Exception если не удалось создать Cipher
     */
    private Cipher getDecryptCipher() throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        return c;
    }

    /**
     * Шифрует номер карты перед сохранением в базу данных.
     *
     * @param cardNumber номер карты в открытом виде
     * @return зашифрованная строка для хранения в базе данных
     */
    @Override
    public String convertToDatabaseColumn(String cardNumber) {
        try {
            if (cardNumber == null) {
                return null;
            }
            Cipher cipher = getEncryptCipher();
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt card number", e);
        }
    }

    /**
     * Расшифровывает номер карты при чтении из базы данных.
     *
     * @param encrypted зашифрованная строка
     * @return номер карты в открытом виде
     */
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
