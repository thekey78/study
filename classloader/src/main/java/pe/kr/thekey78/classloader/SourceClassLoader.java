package pe.kr.thekey78.classloader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SourceClassLoader extends ClassLoader{
    private final Map<String, byte[]> loadedMap = new HashMap<>();

    public Class<?> loadClass(final String name, final byte[] src) throws ClassNotFoundException {
        if(loadedMap.containsKey(name)) {
            if(Arrays.equals(loadedMap.get(name), src)) {
                return loadClass(name);
            }
        }
        return defineClass(name, src, 0, src.length);
    }
}
