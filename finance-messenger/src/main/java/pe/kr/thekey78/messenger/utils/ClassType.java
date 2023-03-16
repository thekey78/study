package pe.kr.thekey78.messenger.utils;

import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum ClassType {
    BYTE(byte.class), SHORT(short.class), INTEGER(int.class), LONG(long.class),
    FLOAT(float.class), DOUBLE(double.class), BOOLEAN(boolean.class), VOID(void.class),
    BIG_INTEGER(BigInteger.class), BIG_DECIMAL(BigDecimal.class),
    LIST(List.class), MAP(Map.class),
    CHARACTER(char.class), STRING(String.class), OBJECT(Object.class);

    private static String str;
    private String className;

    private ClassType(Class clazz) {
        if(clazz == int.class)
            this.className = Integer.class.getSimpleName();
        else if(clazz == char.class)
            this.className = Character.class.getSimpleName();
        else
            this.className = clazz.getSimpleName().toUpperCase(Locale.ROOT);
    }

    public static ClassType getClassType(@NonNull Class c) {
        if(c == int.class)
            return INTEGER;
        else if (c == char.class)
            return CHARACTER;
        else if (List.class.isAssignableFrom(c))
            return LIST;
        else if (Map.class.isAssignableFrom(c))
            return MAP;
        else
            try {
                return getClassType(c.getSimpleName().toUpperCase());
            } catch (IllegalArgumentException exception) {
                return OBJECT;
            }
    }

    public static ClassType getClassType(@NonNull String str) {
        for(ClassType type : values()) {
            if(type.className.equals(str))
                return type;
        }
        return valueOf(str);
    }
}
