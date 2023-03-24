package pe.kr.thekey78.messenger.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NumberUtils extends org.apache.commons.lang3.math.NumberUtils {
    public static Double doubleValue(String str) {
        return toDouble(str);
    }

    public static Float floatValue(String str) {
        return toFloat(str);
    }

    public static Byte byteValue(String str) {
        return toByte(str);
    }

    public static Short shortValue(String str) {
        return toShort(str);
    }

    public static Integer intValue(String str) {
        return toInt(str);
    }

    public static Long longValue(String str) {
        return toLong(str);
    }

    public static Double doubleValue(byte[] str) {
        return toDouble(new String(str));
    }

    public static Float floatValue(byte[] str) {
        return toFloat(new String(str));
    }

    public static Byte byteValue(byte[] str) {
        return toByte(new String(str));
    }

    public static Short shortValue(byte[] str) {
        return toShort(new String(str));
    }

    public static Integer intValue(byte[] str) {
        return toInt(new String(str));
    }

    public static Long longValue(byte[] str) {
        return toLong(new String(str));
    }

    public static BigInteger createBigInteger(byte[] str) {
        return createBigInteger(new String(str));
    }

    public static BigDecimal createBigDecimal(byte[] str) {
        return createBigDecimal(new String(str));
    }

    public static int compare(final Object x, final Object y) {
        BigDecimal l = objectToBigDecimal(x);
        BigDecimal r = objectToBigDecimal(y);

        return l.compareTo(r);
    }

    private static BigDecimal objectToBigDecimal(Object obj) {
        BigDecimal l;
        if (Objects.isNull(obj))
            l = BigDecimal.ZERO;
        else if (ClassUtils.isNumber(obj.getClass()) || obj instanceof String)
            l = NumberUtils.createBigDecimal(obj.toString());
        else if (obj instanceof List)
            l = new BigDecimal(((List) obj).size());
        else if (obj instanceof Map)
            l = new BigDecimal(((Map) obj).size());
        else
            l = new BigDecimal(VoUtils.length(obj));
        return l;
    }
}
