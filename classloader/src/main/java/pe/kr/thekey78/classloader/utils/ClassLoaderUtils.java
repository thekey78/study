package pe.kr.thekey78.classloader.utils;

import java.io.*;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Comparator;

/**
 * ClassLoaderUtils.
 */
public class ClassLoaderUtils {
    /**
     * Check the class name makes sense
     *
     * @param className the class name
     * @throws ClassNotFoundException for a malformed class name
     */
    public static final void checkClassName(final String className) throws ClassNotFoundException
    {
        if (className == null)
            throw new ClassNotFoundException("Null class name");
        if (className.trim().length() == 0)
            throw new ClassNotFoundException("Empty class name '" + className + "'");
    }

    /**
     * Convert a class name into a path
     *
     * @param className the class name
     * @return the path
     */
    public static final String classNameToPath(final String className)
    {
        if (className == null)
            throw new IllegalArgumentException("Null className");

        return className.replace('.', File.pathSeparatorChar) + ".class";
    }

    /**
     * Convert a class into a path
     *
     * @param clazz the class
     * @return the path
     */
    public static final String classNameToPath(final Class<?> clazz)
    {
        if (clazz == null)
            throw new IllegalArgumentException("Null class");

        return classNameToPath(clazz.getName());
    }

    /**
     * Convert a resource name to a class name
     *
     * @param resourceName the resource name
     * @return the class name or null if it is not a class
     */
    public static final String resourceNameToClassName(String resourceName)
    {
        if (resourceName.endsWith(".class") == false)
            return null;
        resourceName = resourceName.substring(0, resourceName.length()-6);
        return resourceName.replace('/', '.');
    }

    /**
     * Convert a class's package name into a path
     *
     * @param className the class name
     * @return the path
     */
    public static final String packageNameToPath(final String className)
    {
        String packageName = getClassPackageName(className);
        return packageName.replace('.', '/');
    }

    /**
     * Convert a package name into a path
     *
     * @param packageName the package name
     * @return the path
     */
    public static final String packageToPath(final String packageName)
    {
        return packageName.replace('.', '/');
    }

    /**
     * Convert a path name into a package
     *
     * @param pathName the path name
     * @return the package
     */
    public static final String pathToPackage(final String pathName)
    {
        return pathName.replace('/', '.');
    }

    /**
     * Get the package name for a class
     *
     * @param className the class name
     * @return the package name or the empty string if there is no package
     */
    public static final String getClassPackageName(final String className)
    {
        int end = className.lastIndexOf('.');
        if (end == -1)
            return "";
        return className.substring(0, end);
    }

    /**
     * Get the package name for a class
     *
     * @param className the class name
     * @return the package name or the empty string if there is no package
     */
    public static final String getResourcePackageName(final String className)
    {
        int i = className.lastIndexOf('/');
        if (i == -1)
            return "";
        return className.substring(0, i).replace('/', '.');
    }

    /**
     * Load bytecode from a stream
     *
     * @param name the class name
     * @param is the input stream
     * @return the byte code
     */
    public static final byte[] loadByteCode(String name, final InputStream is)
    {
        try
        {
            return readBytes(is);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to load class byte code " + name, e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                // pointless
            }
        }
    }

    /**
     * Load bytes from a stream
     *
     * @param is the input stream
     * @return the bytes
     * @throws IOException for any error
     */
    public static final byte[] loadBytes(final InputStream is) throws IOException
    {
        try
        {
            return readBytes(is);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                // pointless
            }
        }
    }

    /**
     * Read bytes.
     * Doesn't close inputstream.
     *
     * @param is the input stream
     * @return the bytes
     * @throws IOException for any error
     * @throws IllegalArgumentException for null is parameter
     */
    protected static final byte[] readBytes(final InputStream is) throws IOException
    {
        if (is == null)
            throw new IllegalArgumentException("Null input stream.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int read ;
        while ((read = is.read(tmp)) >= 0)
        {
            baos.write(tmp, 0, read);
        }
        return baos.toByteArray();
    }

    /**
     * Formats the class as a string
     *
     * @param clazz the class
     * @return the string
     */
    public static final String classToString(final Class<?> clazz)
    {
        if (clazz == null)
            return "null";

        StringBuilder builder = new StringBuilder();
        classToString(clazz, builder);
        return builder.toString();
    }

    /**
     * Formats a class into a string builder
     *
     * @param clazz the class
     * @param builder the builder
     */
    public static final void classToString(final Class<?> clazz, StringBuilder builder)
    {
        if (clazz == null)
        {
            builder.append("null");
            return;
        }

        builder.append(clazz);
        builder.append('{');
        ClassLoader cl = getClassLoader(clazz);
        builder.append("cl=").append(cl);
        builder.append(" codeSource=");
        builder.append(getCodeSource(clazz));
        builder.append("}");
    }

    /**
     * Get the classloader for a class
     *
     * @param clazz the class
     * @return the classloader or null if it doesn't have one
     */
    private static final ClassLoader getClassLoader(final Class<?> clazz)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null)
            return clazz.getClassLoader();

        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
        {
            public ClassLoader run()
            {
                return clazz.getClassLoader();
            }
        });
    }

    /**
     * Get the protected domain for a class
     *
     * @param clazz the class
     * @return the protected domain or null if it doesn't have one
     */
    private static final ProtectionDomain getProtectionDomain(final Class<?> clazz)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null)
            return clazz.getProtectionDomain();

        return AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>()
        {
            public ProtectionDomain run()
            {
                return clazz.getProtectionDomain();
            }
        });
    }

    /**
     * Get the code source for a class
     *
     * @param clazz the class
     * @return the code source or null if it doesn't have one
     */
    private static final CodeSource getCodeSource(final Class<?> clazz)
    {
        ProtectionDomain protectionDomain = getProtectionDomain(clazz);
        if (protectionDomain == null)
            return null;
        return protectionDomain.getCodeSource();
    }

    /**
     * Compare two urls
     *
     * @param one the first url
     * @param two the second url
     * @return whether one is less than two
     */
    public static int compareURL(URL one, URL two)
    {
        if (one == null)
            throw new IllegalArgumentException("Null one");
        if (two == null)
            throw new IllegalArgumentException("Null one");

        String a = one.getProtocol();
        String b = two.getProtocol();
        int result = compare(a, b);
        if (result != 0)
            return result;

        a = one.getHost();
        b = two.getHost();
        result = compare(a, b);
        if (result != 0)
            return result;

        a = one.getFile();
        b = two.getFile();
        result = compare(a, b);
        if (result != 0)
            return result;

        int c = one.getPort();
        int d = two.getPort();
        result = c - d;
        if (result != 0)
            return result;

        String ref1 = one.getRef();
        String ref2 = two.getRef();
        return compare(ref1, ref2);
    }

    private static int compare(String one, String two)
    {
        if (one == null &&  two == null)
            return 0;
        if (one == null)
            return -1;
        if (two == null)
            return +1;
        return one.compareTo(two);
    }

    /**
     * URLComparator.
     */
    public static class URLComparator implements Comparator<URL>, Serializable
    {
        /** The serialVersionUID */
        private static final long serialVersionUID = 169805004616968144L;

        public static final URLComparator INSTANCE = new URLComparator();

        public int compare(URL o1, URL o2)
        {
            return compareURL(o1, o2);
        }
    }
}
