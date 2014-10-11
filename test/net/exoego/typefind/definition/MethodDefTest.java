package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Enclosed.class)
public class MethodDefTest {
    public static class BasicStringRepresentation {
        @Test
        public void LambdaNotation() throws NoSuchMethodException {
            final Method method = String.class.getMethod("valueOf", int.class);
            assertThat(MethodDef.newInstance(method).simple(), is("int -> String"));
        }

        @Test
        public void method_name() throws NoSuchMethodException {
            final Method method = String.class.getMethod("valueOf", int.class);
            assertThat(MethodDef.newInstance(method).getMethodName(), is("valueOf"));
        }

        @Test
        public void full_name_of_static_method() throws NoSuchMethodException {
            final Method method = String.class.getMethod("valueOf", int.class);
            final String expected = "java.lang.String.valueOf: int -> java.lang.String";
            assertThat(MethodDef.newInstance(method).full(), is(expected));
        }

        @Test
        public void full_name_of_instance_method() throws NoSuchMethodException {
            final Method method = String.class.getMethod("length");
            final String expected = "java.lang.String#length: java.lang.String -> int";
            assertThat(MethodDef.newInstance(method).full(), is(expected));
        }
    }

    public static class NoArgument {
        public static void doNothing() {}

        @Test
        public void staticAndNoReturnType() throws NoSuchMethodException {
            final Method method = NoArgument.class.getMethod("doNothing");
            assertThat(MethodDef.newInstance(method).simple(), is("() -> ()"));
        }

        @Test
        public void staticAndReturnType() throws NoSuchMethodException {
            final Method method = Math.class.getMethod("random");
            assertThat(MethodDef.newInstance(method).simple(), is("() -> double"));
            assertThat(MethodDef.newInstance(method).full(), is("java.lang.Math.random: () -> double"));
        }
    }

    public static class SingleArgument {
        @Test
        public void instanceAndNoReturnType() throws NoSuchMethodException {
            final Method method = List.class.getMethod("clear");
            assertThat(MethodDef.newInstance(method).simple(), is("List -> ()"));
            assertThat(MethodDef.newInstance(method).full(), is("java.util.List#clear: java.util.List -> ()"));
        }

        @Test
        public void instanceAndReturnType() throws NoSuchMethodException {
            final Method method = List.class.getMethod("isEmpty");
            assertThat(MethodDef.newInstance(method).simple(), is("List -> boolean"));
            assertThat(MethodDef.newInstance(method).full(), is("java.util.List#isEmpty: java.util.List -> boolean"));
        }

        @Test
        public void staticAndNoReturnType() throws NoSuchMethodException {
            final Method method = Arrays.class.getMethod("sort", Object[].class);
            assertThat(MethodDef.newInstance(method).simple(), is("Object[] -> ()"));
            assertThat(MethodDef.newInstance(method).full(), is("java.util.Arrays.sort: java.lang.Object[] -> ()"));
        }

        @Test
        public void staticAndReturnType() throws NoSuchMethodException {
            final Method method = Arrays.class.getMethod("asList", Object[].class);
            assertThat(MethodDef.newInstance(method).simple(), is("T[] -> List<T>"));
            assertThat(MethodDef.newInstance(method).full(), is("java.util.Arrays.asList: T[] -> java.util.List<T>"));
        }
    }
}
