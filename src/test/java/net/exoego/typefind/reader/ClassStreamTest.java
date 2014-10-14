package net.exoego.typefind.reader;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Theories.class)
public class ClassStreamTest {
    private static String JRE_LIB;

    @BeforeClass
    public static void setUp() {
        final String value = System.getProperty("sun.boot.class.path");
        if (value == null) {
            throw new IllegalStateException("JRE lib directory not found");
        }
        final String jar = value.split(System.getProperty("path.separator"))[0];
        final String fileSeparator = System.getProperty("file.separator");
        // Fxxk windows
        final String escapedFileSeparator = fileSeparator.equals("\\") ? "\\\\" : fileSeparator;
        // remove last path such as "/rt.jar"
        JRE_LIB = jar.replaceFirst(escapedFileSeparator + "[^" + escapedFileSeparator + "]+$", "");
        if (JRE_LIB == null) {
            fail("jrelib is null");
        }
    }

    @Test
    public void countJreClasses() throws IOException {
        final Predicate<Path> isJarFile = path -> path.toFile().getName().endsWith(".jar");
        final Stream<Class<?>> jdkClasses = Files.walk(Paths.get(JRE_LIB))
                                                 .filter(isJarFile)
                                                 .flatMap(ClassStream::from)
                                                 .filter(JdkFilters::isPublicDocumentedJdkClass);
        assertThat(jdkClasses.count(), is(greaterThan(4000L)));
    }

    @DataPoints
    public static final String[] PUBLIC_BUT_ENCLOSING_IS_NOT_PUBLIC = {
            "java.util.stream.Node$OfInt", "java.util.stream.Node$Builder$OfInt"
    };

    @Theory
    public void exclude_public_inner_class_if_enclosing_class_is_non_public(String className) throws IOException, ClassNotFoundException {
        final Class<?> aClass = Class.forName(className, false, ClassLoader.getSystemClassLoader());
        assertThat(Modifier.isPublic(aClass.getModifiers()), is(true));
        assertThat(JdkFilters.isPublicDocumentedJdkClass(aClass), is(false));
    }

    private static <T extends Comparable<T>> Matcher<T> greaterThan(final T base) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(final Object o) {
                final T actual = (T) o;
                return actual.compareTo(base) > 0;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("greater than ");
                description.appendValue(base);
            }
        };
    }
}
