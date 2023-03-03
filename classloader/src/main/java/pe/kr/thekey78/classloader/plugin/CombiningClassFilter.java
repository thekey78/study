package pe.kr.thekey78.classloader.plugin;

import pe.kr.thekey78.classloader.spi.ClassFilter;

public class CombiningClassFilter implements ClassFilter{
    /** The serialVersionUID */
    private static final long serialVersionUID = -2504634309000740765L;

    /** Whether it is an "and" filter */
    private boolean and = false;

    /** The filters */
    private ClassFilter[] filters;

    /**
     * Create a new CombiningClassFilter.
     *
     * @param filters the filters
     * @return the filter
     * @throws IllegalArgumentException for null filters
     */
    public static CombiningClassFilter create(ClassFilter... filters)
    {
        return new CombiningClassFilter(false, filters);
    }

    /**
     * Create a new CombiningClassFilter.
     *
     * @param and whether it is an "and" filter
     * @param filters the filters
     * @return the filter
     * @throws IllegalArgumentException for null filters
     */
    public static CombiningClassFilter create(boolean and, ClassFilter... filters)
    {
        return new CombiningClassFilter(and, filters);
    }

    /**
     * Create a new CombiningClassFilter.
     *
     * @param and whether it is an "and" filter
     * @param filters the filters
     * @throws IllegalArgumentException for null filters
     */
    public CombiningClassFilter(boolean and, ClassFilter[] filters)
    {
        if (filters == null)
            throw new IllegalArgumentException("Null filters");
        this.and = and;
        this.filters = filters;
    }

    public boolean matchesClassName(String className)
    {
        for (ClassFilter filter : filters)
        {
            if (filter.matchesClassName(className))
                return true;
            else if (and)
                return false;
        }
        return false;
    }

    public boolean matchesResourcePath(String resourcePath)
    {
        for (ClassFilter filter : filters)
        {
            if (filter.matchesResourcePath(resourcePath))
                return true;
            else if (and)
                return false;
        }
        return false;
    }

    public boolean matchesPackageName(String packageName)
    {
        for (ClassFilter filter : filters)
        {
            if (filter.matchesPackageName(packageName))
                return true;
            else if (and)
                return false;
        }
        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < filters.length; ++i)
        {
            builder.append(filters[i]);
            if (i < filters.length-1)
                builder.append(", ");
        }
        return builder.toString();
    }}
