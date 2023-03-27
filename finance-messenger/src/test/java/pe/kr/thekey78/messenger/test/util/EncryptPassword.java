package pe.kr.thekey78.messenger.test.util;

import pe.kr.thekey78.messenger.MessageExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptPassword implements MessageExtension {
    @Override
    public byte[] doExtension(byte[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
