package pe.kr.thekey78.messenger.test.util;

import pe.kr.thekey78.messenger.MessageExtension;

import java.math.BigInteger;

public class Hex2Byte implements MessageExtension {
    @Override
    public byte[] doExtension(byte[] value) {
        BigInteger bigInteger = new BigInteger(new String(value), 16);
        return bigInteger.toByteArray();
    }

}