package pe.kr.thekey78.classloader.spi;

import java.io.Serializable;

public interface ClassFilter extends Serializable {
    /**
     * Whether the class name matches the filter
     *
     * @param className the class name
     * @return true when it matches the filter
     */
    boolean matchesClassName(String className);

    /**
     * Whether the resource name matches the filter
     *
     * @param resourcePath the resource path
     * @return true when it matches the filter
     */
    boolean matchesResourcePath(String resourcePath);

    /**
     * Whether the package name matches the filter
     *
     * @param packageName the package path
     * @return true when it matches the filter
     */
    boolean matchesPackageName(String packageName);
}
