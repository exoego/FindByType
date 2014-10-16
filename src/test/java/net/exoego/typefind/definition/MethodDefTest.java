package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
        @Test
        public void staticAndNoReturnType() throws NoSuchMethodException {
            final Method method = System.class.getMethod("gc");
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
            assertThat(MethodDef.newInstance(method).simple(), is("List<E> -> ()"));
            assertThat(MethodDef.newInstance(method).full(), is("java.util.List<E>#clear: java.util.List<E> -> ()"));
        }

        @Test
        public void instanceAndReturnType() throws NoSuchMethodException {
            final Method method = List.class.getMethod("isEmpty");
            assertThat(MethodDef.newInstance(method).simple(), is("List<E> -> boolean"));
            assertThat(MethodDef.newInstance(method).full(),
                       is("java.util.List<E>#isEmpty: java.util.List<E> -> boolean"));
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

        @Test
        public void methodHasFunctionalInterfaceArguments() throws NoSuchMethodException {
            final Method method = Stream.class.getMethod("map", Function.class);
            final MethodDef methodDef = MethodDef.newInstance(method);
            assertThat(methodDef.simple(), is("(Stream<T>, (T -> R)) -> Stream<R>"));
            assertThat(methodDef.full(),
                       is("java.util.stream.Stream<T>#map: " +
                          "(java.util.stream.Stream<T>, java.util.function.Function<? super T, ? extends R>) " +
                          "-> java.util.stream.Stream<R>"));
        }
    }

    public static class TwoArguments {
        @Test
        public void instanceAndNoReturnType() throws NoSuchMethodException {
            final Method method = List.class.getMethod("sort", Comparator.class);
            final MethodDef methodDef = MethodDef.newInstance(method);
            assertThat(methodDef.full(),
                       is("java.util.List<E>#sort: (java.util.List<E>, java.util.Comparator<? super E>) -> ()"));
            assertThat(methodDef.simple(), is("(List<E>, ((E, E) -> int)) -> ()"));
        }

        @Test
        public void instanceAndReturnType() throws NoSuchMethodException {
            final Method method = List.class.getMethod("add", Object.class);
            assertThat(MethodDef.newInstance(method).simple(), is("(List<E>, E) -> boolean"));
            assertThat(MethodDef.newInstance(method).full(),
                       is("java.util.List<E>#add: (java.util.List<E>, E) -> boolean"));
        }

        @Test
        public void staticAndNoReturnType() throws NoSuchMethodException {
            final Method method = Collections.class.getMethod("copy", List.class, List.class);
            final MethodDef methodDef = MethodDef.newInstance(method);
            assertThat(methodDef.simple(), is("(List<? super T>, List<? extends T>) -> ()"));
            assertThat(methodDef.full(),
                       is("java.util.Collections.copy: (java.util.List<? super T>, java.util.List<? extends T>) -> ()"));
        }

        @Test
        public void staticAndReturnType() throws NoSuchMethodException {
            final Method method = Collections.class.getMethod("binarySearch", List.class, Object.class);
            final MethodDef methodDef = MethodDef.newInstance(method);
            assertThat(methodDef.simple(), is("(List<? extends java.lang.Comparable<? super T>>, T) -> int"));
            assertThat(methodDef.full(),
                       is("java.util.Collections.binarySearch: (java.util.List<? extends java.lang.Comparable<? super T>>, T) -> int"));
        }
    }
}
