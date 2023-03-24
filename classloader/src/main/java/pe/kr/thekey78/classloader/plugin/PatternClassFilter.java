package pe.kr.thekey78.classloader.plugin;

import pe.kr.thekey78.classloader.spi.ClassFilter;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternClassFilter implements ClassFilter {
    /** The class patterns as regular expressions */
    private Pattern[] classPatterns;

    /** The resource patterns as regular expressions */
    private Pattern[] resourcePatterns;

    /** The package patterns as regular expressions */
    private Pattern[] packagePatterns;

    /** Whether to include java */
    private boolean includeJava = false;

    /**
     * Create a new PatternClassFilter.
     *
     * @param classPatterns the class patterns
     * @param resourcePatterns the resource patterns
     * @param packagePatterns the package patterns
     * @throws IllegalArgumentException for a null pattern
     */
    public PatternClassFilter(String[] classPatterns, String[] resourcePatterns, String[] packagePatterns)
    {
        if (classPatterns == null)
            throw new IllegalArgumentException("Null patterns");

        this.classPatterns = new Pattern[classPatterns.length];
        for (int i = 0; i < classPatterns.length; ++i)
        {
            if (classPatterns[i] == null)
                throw new IllegalArgumentException("Null pattern in " + Arrays.asList(classPatterns));
            this.classPatterns[i] = Pattern.compile(classPatterns[i]);
        }

        if (resourcePatterns == null)
        {
            this.resourcePatterns = this.classPatterns;
            return;
        }

        this.resourcePatterns = new Pattern[resourcePatterns.length];
        for (int i = 0; i < resourcePatterns.length; ++i)
        {
            if (resourcePatterns[i] == null)
                throw new IllegalArgumentException("Null pattern in " + Arrays.asList(resourcePatterns));
            this.resourcePatterns[i] = Pattern.compile(resourcePatterns[i]);
        }

        if (packagePatterns == null)
        {
            this.packagePatterns = this.classPatterns;
            return;
        }

        this.packagePatterns = new Pattern[packagePatterns.length];
        for (int i = 0; i < packagePatterns.length; ++i)
        {
            if (packagePatterns[i] == null)
                throw new IllegalArgumentException("Null pattern in " + Arrays.asList(packagePatterns));
            this.packagePatterns[i] = Pattern.compile(packagePatterns[i]);
        }
    }

    /**
     * Get the includeJava.
     *
     * @return the includeJava.
     */
    public boolean isIncludeJava()
    {
        return includeJava;
    }

    /**
     * Set the includeJava.
     *
     * @param includeJava the includeJava.
     */
    public void setIncludeJava(boolean includeJava)
    {
        this.includeJava = includeJava;
    }

    public boolean matchesClassName(String className)
    {
        if (className == null)
            return false;

        for (int i = 0; i < classPatterns.length; ++i)
        {
            Matcher matcher = classPatterns[i].matcher(className);
            if (matcher.matches())
                return true;
        }
        return false;
    }

    public boolean matchesResourcePath(String resourcePath)
    {
        if (resourcePath == null)
            return false;

        for (int i = 0; i < resourcePatterns.length; ++i)
        {
            Matcher matcher = resourcePatterns[i].matcher(resourcePath);
            if (matcher.matches())
                return true;
        }
        return false;
    }

    public boolean matchesPackageName(String packageName)
    {
        if (packageName == null)
            return false;

        for (int i = 0; i < packagePatterns.length; ++i)
        {
            Matcher matcher = packagePatterns[i].matcher(packageName);
            if (matcher.matches())
                return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.asList(classPatterns));
        if (isIncludeJava())
            builder.append(" <INCLUDE_JAVA>");
        return builder.toString();
    }
}
