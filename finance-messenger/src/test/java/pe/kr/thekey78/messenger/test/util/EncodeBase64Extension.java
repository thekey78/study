package pe.kr.thekey78.messenger.test.util;

import org.apache.commons.io.HexDump;
import pe.kr.thekey78.messenger.MessageExtension;

import java.util.Base64;

public class EncodeBase64Extension implements MessageExtension {
    @Override
    public byte[] doExtension(byte[] value) {
        return Base64.getEncoder().encode(value);
    }
}
