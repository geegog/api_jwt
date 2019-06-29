package com.icefire.api.common.infrastructure.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

public class MyKeyGenerator {

    private final static String PATH = "src/main/resources/keys/";

    public static SecretKey keyGenerator() {
        try {
            KeyGenerator kpg = KeyGenerator.getInstance("AES");
            kpg.init(256);
            return kpg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] generatorIV() {
        SecureRandom secureRandom = new SecureRandom();
        return secureRandom.generateSeed(16);
    }

    public static byte[] keyPairGenerator(String username, SecretKey secretKey, byte[] iv) {
        byte[] publicKeyBytes = new byte[0];
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            PublicKey pub = kp.getPublic(); // X.509 format
            PrivateKey pvt = kp.getPrivate(); // PKCS#8 format

            //save private
            savePrivateKeyToFile(pvt, username);

            //save iv
            saveIVToFile(iv, username);

            RSACipher rsaCipher = new RSACipher(pub);
            publicKeyBytes = rsaCipher.encryptedKey(Objects.requireNonNull(secretKey));

            //publicKeyBytes = pub.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return publicKeyBytes;
    }

    public static PrivateKey getPrivateKey(String username) {
        try {
            /* Read all bytes from the private key file */
            Path path = Paths.get(PATH + username + "_private" + ".key");
            byte[] bytes = Files.readAllBytes(path);

            /* Generate private key. */
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(ks);

        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static byte[] getIV(String username) {
        try {
            /* Read all bytes from the private key file */
            Path path = Paths.get(PATH + username + "_iv" + ".txt");
            return Files.readAllBytes(path);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static SecretKey getSecretKey(String username, byte[] decryptedKey) {
        return new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES");
    }

    public static PublicKey getPublicKey(String base64PublicKey){
        PublicKey publicKey;
        try{
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void savePrivateKeyToFile(PrivateKey privateKey, String username) {
        creatFolderIfNotExit();
        try {
            Path path = Paths.get(PATH + username + "_private" + ".key");
            Files.write(path, privateKey.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveIVToFile(byte[] vi, String username) {
        creatFolderIfNotExit();
        try {
            Path path = Paths.get(PATH + username + "_iv" + ".txt");
            Files.write(path, vi);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void creatFolderIfNotExit() {
        File directory = new File(PATH);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

}
