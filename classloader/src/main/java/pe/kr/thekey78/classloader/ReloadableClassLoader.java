package pe.kr.thekey78.classloader;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pe.kr.thekey78.classloader.plugin.CombiningClassFilter;
import pe.kr.thekey78.classloader.plugin.PatternClassFilter;
import pe.kr.thekey78.classloader.spi.ClassFilter;
import pe.kr.thekey78.classloader.spi.ClassFilterUtils;
import pe.kr.thekey78.classloader.utils.ClassLoaderUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@ToString
public class ReloadableClassLoader extends ClassLoader {
    String classPath;
    private ClassFilter classFilter;
    private Map<String, Class<?>> loadedClasses = new HashMap<>();

    public ReloadableClassLoader(ClassLoader parent) {
        super(parent);
    }

    public ReloadableClassLoader(String classPath, ClassFilter classFilter, ClassLoader parent) {
        super(parent);
        this.classPath = classPath;
        this.classFilter = classFilter;
    }

    public ReloadableClassLoader(ClassFilter classFilter, ClassLoader parent) {
        super(parent);
        this.classPath = String.valueOf(getClass().getClassLoader().getResource("./"));
        this.classFilter = classFilter;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ClassLoaderUtils.checkClassName(name);
        if (classFilter == null || !classFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName(name)) || !classFilter.matchesClassName(name)) {
            return super.findClass(name);
        }
        return loadClassLocally(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassLoaderUtils.checkClassName(name);
        if (classFilter == null || !classFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName(name)) || !classFilter.matchesClassName(name)) {
            return super.loadClass(name);
        }

        return loadClassLocally(name);
    }

    private Class<?> loadClassLocally(String name) throws ClassNotFoundException {
        // 클래스 파일 경로 구하기
        String path = classPath + File.pathSeparator + ClassLoaderUtils.classNameToPath(name);
        Path classFilePath = Paths.get(path);

        // WatchService 생성
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            classFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        final WatchService service = watchService;

        Thread eventListenerThread = new Thread(() -> {
            while (true) {
                try {
                    WatchKey key = service.take();
                    key.reset();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && event.context().toString().equals(ClassLoaderUtils.classNameToPath(name))) {
                            // 파일이 변경되면 클래스를 다시 로드
                            byte[] bytes = Files.readAllBytes(classFilePath);
                            Class<?> cls = defineClass(name, bytes, 0, bytes.length);
                            loadedClasses.put(name, cls);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        eventListenerThread.setDaemon(true);
        eventListenerThread.start();

        return loadedClasses.get(name);
    }
}
