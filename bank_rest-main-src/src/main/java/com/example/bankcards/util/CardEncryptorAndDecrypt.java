package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class CardEncryptorAndDecrypt {

    private final String secretKey;

    public CardEncryptorAndDecrypt(@Value("${app.card-encryption.key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String encrypt(String cardNumber) {
        try {
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования карты", e);
        }
    }

    public String decrypt(String encryptedCardNumber) {
        try {
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
