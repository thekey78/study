package pe.kr.thekey78.messenger.utils;

import static pe.kr.thekey78.messenger.utils.ClassType.*;

public class ClassUtils {
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || isNumber(clazz) || isBoolean(clazz) || isCharacter(clazz) || isVoid(clazz);
    }

    public static boolean isVoid(Class<?> clazz) {
        return ClassType.getClassType(clazz) == VOID;
    }

    public static boolean isNumber(Class<?> clazz) {
        return isWholeNumber(clazz) || isActualNumber(clazz) || Number.class.isAssignableFrom(clazz);
    }

    public static boolean isBoolean(Class<?> clazz) {
        return ClassType.getClassType(clazz) == BOOLEAN;
    }

    public static boolean isCharacter(Class clazz) {
        return ClassType.getClassType(clazz) == CHARACTER;
    }

    public static boolean isWholeNumber(Class<?> clazz) {
        switch (ClassType.getClassType(clazz)) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case BIG_INTEGER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isActualNumber(Class<?> clazz) {
        switch (ClassType.getClassType(clazz)) {
            case FLOAT:
            case DOUBLE:
            case BIG_DECIMAL:
                return true;
            default:
                return false;
        }
    }
}
