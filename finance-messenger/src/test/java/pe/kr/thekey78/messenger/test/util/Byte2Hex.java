package pe.kr.thekey78.messenger.test.util;

import pe.kr.thekey78.messenger.MessageExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Byte2Hex implements MessageExtension {
    @Override
    public byte[] doExtension(byte[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sha256Hash = digest.digest(value);
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : sha256Hash) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString().toUpperCase().getBytes();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

}
