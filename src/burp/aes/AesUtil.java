package burp.aes;

import burp.utils.OutFormat;
import burp.utils.Utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesUtil {
    private Cipher cipher;
    private AesAlgorithms algorithms;
    private byte[] IV;
    private SecretKey sKey;
    private OutFormat outFormat;

    public void setConfig(AesConfig config) {
        try {
            this.algorithms = config.Algorithms;
            this.cipher = Cipher.getInstance(algorithms.name().replace('_', '/'));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw fail(e);
        }
        sKey = new SecretKeySpec(config.Key, "AES");
        IV = config.IV;
        outFormat = config.OutFormat;
    }

    public String encrypt(byte[] plaintext) {
        byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, sKey, IV, plaintext);
        return outFormat == OutFormat.Base64 ? Utils.base64(encrypted) : Utils.hex(encrypted);
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, sKey, IV, Utils.base64(cipherText));
            return new String(decrypted, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw fail(e);
        }
    }

    private byte[] doFinal(int encryptMode, SecretKey key, byte[] iv, byte[] bytes) {
        try {
            if (algorithms.name().startsWith("AES_ECB_")) {
                cipher.init(encryptMode, key);
            } else {
                cipher.init(encryptMode, key, new IvParameterSpec(iv));
            }
            return cipher.doFinal(bytes);
        } catch (InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            throw fail(e);
        }
    }

    private IllegalStateException fail(Exception e) {
        return new IllegalStateException(e);
    }
}

