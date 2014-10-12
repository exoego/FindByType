package net.exoego.typefind.reader;

import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class JdkPublicPackagesTest {
    private static class MyMatcher extends BaseMatcher<CharSequence> {
        private static final MyMatcher INSTANCE = new MyMatcher();
        private static final Pattern JDK_ALL = JdkPublicPackages.jdkAll();

        @Override
        public boolean matches(final Object o) {
            if (o instanceof CharSequence) {
                return JDK_ALL.matcher((CharSequence) o).matches();
            }
            return false;
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("package");
        }
    }

    private static MyMatcher jdkPackage() {
        return MyMatcher.INSTANCE;
    }

    @Test
    public void jdk_package() {
        assertThat("java.awt", is(jdkPackage()));
        assertThat("java.awt.color", is(jdkPackage()));
        assertThat("java.awt.datatransfer", is(jdkPackage()));
    }

    @Test
    public void has_no_class() {
        assertThat("java", is(not(jdkPackage())));
        assertThat("org", is(not(jdkPackage())));
    }

    @Test
    public void no_such_package() {
        assertThat("java.awttttt", is(not(jdkPackage())));
        assertThat("java.awt.no_such", is(not(jdkPackage())));
        assertThat("!java.awttttt", is(not(jdkPackage())));
    }

    @Test
    public void no_dot_separator() {
        assertThat("javautil", is(not(jdkPackage())));
        assertThat("javaawt", is(not(jdkPackage())));
    }
}
