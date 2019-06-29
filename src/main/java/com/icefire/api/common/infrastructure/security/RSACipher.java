package com.icefire.api.common.infrastructure.security;

import com.google.common.io.BaseEncoding;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class RSACipher {

    private static final String ALGORITHM_RSA = "RSA/ECB/PKCS1Padding";
    private Key key;

    /**
     * Create RSACipher based on existing {@link Key}
     *
     * @param key Key
     */
    public RSACipher(Key key) {
        this.key = key;
    }

    /**
     * Takes message and encrypts with Key
     *
     * @param message String
     * @return String Base64 encoded
     */
    public String getEncryptedMessage(String message) {
        String encValBase64 = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedTextBytes = cipher.doFinal(message.getBytes());
            encValBase64 = BaseEncoding.base64().encode(encryptedTextBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return encValBase64;
    }

    /**
     * Takes Base64 encoded String and decodes with provided key
     *
     * @param message String encoded with Base64
     * @return String
     */
    public String getDecryptedMessage(String message) {

        String decValBase64 = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedTextBytes = BaseEncoding.base64().decode(message);
            byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
            decValBase64 = new String(decryptedTextBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decValBase64;
    }

    /**
     * Takes SecretKey key and encodes with provided key
     *
     * @param secretKey SecretKey
     * @return String
     */
    byte[] encryptedKey(SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.PUBLIC_KEY, key);
            return cipher.doFinal(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Takes encryptedKey key and decodes with provided key
     *
     * @param encryptedKey byte[]
     * @return String
     */
    public byte[] decryptedKey(byte[] encryptedKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.PRIVATE_KEY, key);
            return cipher.doFinal(encryptedKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

}
