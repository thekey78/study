package pe.kr.thekey78.classloader.spi;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

public interface Loader {
    /**
     * Load a class
     *
     * @param className the class name
     * @return the class or null if not found
     */
    Class<?> loadClass(String className);

    /**
     * Get a resource
     *
     * @param name the resource name
     * @return the url or null if not found
     */
    URL getResource(String name);

    /**
     * Get resources
     *
     * @param name the resource name
     * @param urls the list of urls to add to
     * @throws IOException for any error
     */
    // FindBugs: The Set doesn't use equals/hashCode
    void getResources(String name, Set<URL> urls) throws IOException;

    /**
     * Get a package
     *
     * @param name the package name
     * @return the package
     */
    Package getPackage(String name);

    /**
     * Get all the packages visible from this  loader
     *
     * @param packages the packages
     */
    void getPackages(Set<Package> packages);}
