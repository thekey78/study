package pe.kr.thekey78.classloader;

import org.junit.Before;
import org.junit.Test;
import pe.kr.thekey78.classloader.plugin.CombiningClassFilter;
import pe.kr.thekey78.classloader.plugin.PatternClassFilter;
import pe.kr.thekey78.classloader.spi.ClassFilter;
import pe.kr.thekey78.classloader.spi.ClassFilterUtils;
import pe.kr.thekey78.classloader.test.TestVo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class ClassLoaderTest {
    ReloadableClassLoader classLoader;

    @Before
    public void before() throws IOException {
        InputStream input = getClass().getClassLoader().getResourceAsStream("./reloadable-classloader.properties");
        Properties properties = new Properties();
        properties.load(input);

        ClassFilter classFilter = CombiningClassFilter.create(true,
                new PatternClassFilter(
                        properties.getProperty("match.class.patterns","").split(","),
                        properties.getProperty("match.resource.patterns","").split(","),
                        properties.getProperty("match.package.patterns","").split(",")
                ),
                ClassFilterUtils.negatingClassFilter(new PatternClassFilter(
                        properties.getProperty("negate.class.patterns","").split(","),
                        properties.getProperty("negate.resource.patterns","").split(","),
                        properties.getProperty("negate.package.patterns","").split(",")
                ))
        );
        classLoader = new ReloadableClassLoader(classFilter, ClassLoader.getSystemClassLoader());
    }

    @Test
    public void testClassLoader() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        do {
            Class<?> clazz = classLoader.loadClass("pe.kr.thekey78.classloader.test.TestVo");
            System.out.println("TestVo ClassLoader : " + clazz.getClassLoader());

            Constructor constructor = clazz.getConstructor(null);
            TestVo testVo = (TestVo) constructor.newInstance(null);
            testVo.print("thekey");
            testVo.printStatic("static for thekey");

            Thread.sleep(1000);
        }while (true);
    }
}
