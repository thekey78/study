package pe.kr.thekey78.messenger.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ClassUtils {
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || isNumber(clazz) || isBoolean(clazz) || isCharacter(clazz) || isVoid(clazz);
    }

    public static boolean isVoid(Class<?> clazz) {
        return clazz == Void.class;
    }

    public static boolean isNumber(Class<?> clazz) {
        return isWholeNumber(clazz) || isActualNumber(clazz) || clazz.getSuperclass() == Number.class;
    }

    public static boolean isBoolean(Class<?> clazz) {
        return clazz == Boolean.class;
    }

    public static boolean isCharacter(Class clazz) {
        return clazz == Character.class;
    }

    public static boolean isWholeNumber(Class<?> clazz) {
        return clazz == byte.class
                || clazz == short.class
                || clazz == int.class
                || clazz == long.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == BigInteger.class
                ;
    }

    public static boolean isActualNumber(Class<?> clazz) {
        return clazz == float.class
                || clazz == double.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == BigDecimal.class
                ;
    }
}
