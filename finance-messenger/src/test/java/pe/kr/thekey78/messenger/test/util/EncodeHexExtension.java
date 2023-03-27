package pe.kr.thekey78.messenger.test.util;

import pe.kr.thekey78.messenger.MessageExtension;

public class EncodeHexExtension implements MessageExtension {
    @Override
    public byte[] doExtension(byte[] value) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : value) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString().toUpperCase().getBytes();
    }
}
