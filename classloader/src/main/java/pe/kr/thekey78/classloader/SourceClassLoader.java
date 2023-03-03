package pe.kr.thekey78.classloader;

import org.bouncycastle.util.Arrays;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SourceClassLoader extends ClassLoader{
    private Map<String, byte[]> loadedMap = new HashMap<>();

    public Class<?> loadClass(final String name, final byte[] src) throws ClassNotFoundException {
        if(loadedMap.containsKey(name)) {
            if(Arrays.areEqual(loadedMap.get(name), src)) {
                return loadClass(name);
            }
        }
        return defineClass(name, src, 0, src.length);
    }
}
