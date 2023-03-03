package pe.kr.thekey78.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class ObjectUtils {
    public static <T> T toObject(final byte[] input, Class<T> clazz) {
        try {
            String str = new String(input);
            Constructor<T> constructor = clazz.getConstructor(new Class[] {String.class});
            if (constructor == null)
                throw new IllegalArgumentException("There is no constructor that takes a String as an argument value.");
            else
                return constructor.newInstance(str);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getLocalizedMessage(), e);
        } catch (InvocationTargetException e) {
            log.error(e.getLocalizedMessage(), e.getTargetException());
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new IllegalArgumentException(e.getLocalizedMessage(), e.getTargetException());
        }
    }

    public static <E extends Serializable> int sizeof(E obj) {
        if(obj == null) return 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        int size = -1;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            size = baos.size();
        } catch(IOException ignored) {
            log.error(ignored.getLocalizedMessage(), ignored);
        } finally {
            try { oos.close(); } catch(IOException ignored) {}
        }//try-catch-finally statement ended

        return size;
    }//sizeof()
}