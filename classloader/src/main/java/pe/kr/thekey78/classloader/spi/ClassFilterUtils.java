package pe.kr.thekey78.classloader.spi;

import pe.kr.thekey78.classloader.plugin.NegatingClassFilter;

public class ClassFilterUtils {
    /**
     * Create negating class filter.
     *
     * @param filter the filter
     * @return negating filter for @param filter
     * @throws IllegalArgumentException for filter
     */
    public static ClassFilter negatingClassFilter(ClassFilter filter)
    {
        return new NegatingClassFilter(filter);
    }
}
