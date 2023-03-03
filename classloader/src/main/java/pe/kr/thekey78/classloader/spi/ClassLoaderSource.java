package pe.kr.thekey78.classloader.spi;

public class ClassLoaderSource {
    /** The classloader */
    private ClassLoader classLoader;

    /**
     * Create a nuew ClassLoaderSource.
     * @param classLoader
     */
    public ClassLoaderSource(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Get the classloader
     * @return
     */
    protected ClassLoader getClassLoader() {
        return this.classLoader;
    }
}
