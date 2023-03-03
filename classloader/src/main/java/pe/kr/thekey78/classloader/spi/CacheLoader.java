package pe.kr.thekey78.classloader.spi;

/**
 * Can load from cache.
 */
public interface CacheLoader extends Loader{
    /**
     * Check the class cache.
     *
     * @param name the name of the class
     * @param path the path of the class resource
     * @param allExports whether to look at all exports
     * @return the class if cached
     */
    Class<?> checkClassCache(String name, String path, boolean allExports);
}
