package net.exoego.typefind.reader;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Theories.class)
public class ClassStreamTest {
    @DataPoints
    public static final String[] PUBLIC_BUT_ENCLOSING_IS_NOT_PUBLIC = {
            "java.util.stream.Node$OfInt", "java.util.stream.Node$Builder$OfInt"
    };
    private static final Path JRE_LIB = ClassStream.getJreLibPath();

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

    @Test
    public void countJreClasses() throws IOException {
        final Predicate<Path> isJarFile = path -> path.toFile().getName().endsWith(".jar");
        final Stream<Class<?>> jdkClasses = Files.walk(JRE_LIB)
                                                 .filter(isJarFile)
                                                 .flatMap(ClassStream::from)
                                                 .filter(JdkFilters::isPublicDocumentedJdkClass);
        assertThat(jdkClasses.count(), is(greaterThan(4000L)));
    }

    @Theory
    public void exclude_public_inner_class_if_enclosing_class_is_non_public(String className) throws IOException, ClassNotFoundException {
        final Class<?> aClass = Class.forName(className, false, ClassLoader.getSystemClassLoader());
        assertThat(Modifier.isPublic(aClass.getModifiers()), is(true));
        assertThat(JdkFilters.isPublicDocumentedJdkClass(aClass), is(false));
    }
}
