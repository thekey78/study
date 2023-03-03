package pe.kr.thekey78.classloader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pe.kr.thekey78.classloader.plugin.CombiningClassFilter;
import pe.kr.thekey78.classloader.plugin.PatternClassFilter;
import pe.kr.thekey78.classloader.spi.ClassFilter;
import pe.kr.thekey78.classloader.spi.ClassFilterUtils;
import pe.kr.thekey78.classloader.utils.ClassLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClassFilterTest {
    Properties properties = new Properties();

    @Before
    public void before() throws IOException {
        InputStream input = getClass().getClassLoader().getResourceAsStream("./reloadable-classloader.properties");
        properties.load(input);
    }
    @Test
    public void matchClassFilter() {
        String[] matchClassPatterns = properties.getProperty("match.class.patterns","").split(",");
        String[] matchResources = properties.getProperty("match.resource.patterns","").split(",");
        String[] matchPackages = properties.getProperty("match.package.patterns","").split(",");

        ClassFilter matchClassFilter = new PatternClassFilter(matchClassPatterns, matchResources, matchPackages);

        Assert.assertEquals(true , matchClassFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName("pe.kr.thekey78.classloader.test.TestVo")));
        Assert.assertNotEquals(true, matchClassFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName("java.lang.Object")));
    }

    @Test
    public void negateMatchClassFilter() {
        String[] classPattern = properties.getProperty("negate.class.patterns","").split(",");
        String[] resourcePattern = properties.getProperty("negate.resource.patterns","").split(",");
        String[] packagePattern = properties.getProperty("negate.package.patterns","").split(",");

        ClassFilter matchClassFilter = new PatternClassFilter(classPattern, resourcePattern, packagePattern);
        matchClassFilter = ClassFilterUtils.negatingClassFilter(matchClassFilter);

        Assert.assertEquals(false, matchClassFilter.matchesClassName("pe.kr.thekey78.classloader.test.vo1.TestVo1"));
        Assert.assertEquals(true, matchClassFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName("pe.kr.thekey78.classloader.test.TestVo")));
        Assert.assertEquals(true, matchClassFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName("java.lang.Object")));
    }

    @Test
    public void combineClassFilter() {
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

        Assert.assertEquals(true, classFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName("pe.kr.thekey78.classloader.test.TestVo")));
        Assert.assertEquals(false, classFilter.matchesClassName("pe.kr.thekey78.classloader.test.TestVo"));
        Assert.assertEquals(false, classFilter.matchesPackageName(ClassLoaderUtils.getClassPackageName("pe.kr.thekey78.classloader.test.no1.TestVo1")));
        Assert.assertEquals(false, classFilter.matchesClassName("pe.kr.thekey78.classloader.test.no1.TestVo1"));
        Assert.assertEquals(false, classFilter.matchesClassName("java.lang.Object"));
    }
}
