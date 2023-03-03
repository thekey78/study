package pe.kr.thekey78.classloader.plugin;

import pe.kr.thekey78.classloader.spi.ClassFilter;

public class NegatingClassFilter implements ClassFilter {
    /** The filter to negate */
    private ClassFilter filter;

    public NegatingClassFilter(ClassFilter filter)
    {
        if (filter == null)
            throw new IllegalArgumentException("Null filter");
        this.filter = filter;
    }

    public boolean matchesClassName(String className)
    {
        return filter.matchesClassName(className) == false;
    }

    public boolean matchesResourcePath(String resourcePath)
    {
        return filter.matchesResourcePath(resourcePath) == false;
    }

    public boolean matchesPackageName(String packageName)
    {
        return filter.matchesPackageName(packageName) == false;
    }

    @Override
    public String toString()
    {
        return "EXCLUDE " + filter;
    }
}
