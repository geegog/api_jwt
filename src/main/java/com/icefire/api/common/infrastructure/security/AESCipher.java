package com.icefire.api.common.infrastructure.security;

import com.google.common.io.BaseEncoding;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESCipher {

    private static final String ALGORITHM_AES256 = "AES/CBC/PKCS5Padding";
    private final SecretKey secretKey;
    private IvParameterSpec iv;

    /**
     * Create AESCipher based on existing {@link SecretKey }
     *
     * @param secretKey SecretKey
     */
    public AESCipher(SecretKey  secretKey, byte[] iv) {
        this.secretKey = secretKey;
        this.iv = new IvParameterSpec(iv);
    }

    /**
     * Takes message and encrypts with Key
     *
     * @param message String
     * @return String Base64 encoded
     */
    public String getEncryptedMessage(String message) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES256);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            byte[] encryptedTextBytes = cipher.doFinal(message.getBytes("UTF-8"));

            return BaseEncoding.base64().encode(encryptedTextBytes);
        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Takes Base64 encoded String and decodes with provided key
     *
     * @param message String encoded with Base64
     * @return String
     */
    public String getDecryptedMessage(String message) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES256);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] encryptedTextBytes = BaseEncoding.base64().decode(message);
            byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);

            return new String(decryptedTextBytes);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
