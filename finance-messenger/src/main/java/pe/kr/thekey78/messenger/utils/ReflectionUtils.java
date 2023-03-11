package pe.kr.thekey78.messenger.utils;

import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.cglib.core.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ReflectionUtils {
    @SuppressWarnings("cast")
    public static <E> Class<E> getGenericClass(Type t) throws ClassNotFoundException {
        if (t instanceof ParameterizedType) {
            Type tType = ((ParameterizedType)t).getActualTypeArguments()[0];
            String className = tType.getTypeName();
            return (Class<E>) Class.forName(className);
        }
        else {
            String className = t.getTypeName();
            return (Class<E>) Class.forName(className);
        }
    }

    public static <T> T newInstance(Class<T> type) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return newInstance(type, Constants.EMPTY_CLASS_ARRAY, null);
    }

    public static <T> T newInstance(Class<T> type, Class<?>[] parameterTypes, Object[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return newInstance(getConstructor(type, parameterTypes), args);
    }

    public static <T> T newInstance(Constructor<T> cstruct, Object[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        boolean flag = cstruct.isAccessible();

        try {
            if (!flag) {
                cstruct.setAccessible(true);
            }

            return cstruct.newInstance(args);
        } finally {
            if (!flag) {
                cstruct.setAccessible(flag);
            }

        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>[] parameterTypes) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            if (System.getSecurityManager() != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    constructor.setAccessible(true);
                    return null;
                });
            }
            else {
                constructor.setAccessible(true);
            }

            return constructor;
        } catch (NoSuchMethodException var3) {
            throw new CodeGenerationException(var3);
        }
    }

}
