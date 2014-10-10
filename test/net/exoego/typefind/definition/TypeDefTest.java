package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Enclosed.class)
public class TypeDefTest {
    @RunWith(Theories.class)
    public static class Primitives {
        @DataPoints
        public static final Class<?>[] PRIMITIVES = {
                boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class
        };

        @Test
        public void full_name_has_no_package() {
            assertThat(TypeDef.newInstance(int.class).getFullName(), is("int"));
        }

        @Theory
        public void full_name_equals_to_type_name_of_class(Class<?> primitive) {
            assertThat(TypeDef.newInstance(primitive).getFullName(), is(primitive.getTypeName()));
        }

        @Theory
        public void full_name_equlals_to_simple_name(Class<?> primitive) {
            final TypeDef primitiveDef = TypeDef.newInstance(primitive);
            assertThat(primitiveDef.getFullName(), is(primitiveDef.getSimpleName()));
        }
    }

    public static class Void {
        @Test
        public void parens() {
            assertThat(TypeDef.newInstance(java.lang.Void.TYPE).getFullName(), is("()"));
        }

        @Test
        public void full_name_equals_to_simple_name() {
            final TypeDef voidType = TypeDef.newInstance(java.lang.Void.TYPE);
            assertThat(voidType.getFullName(), is(voidType.getSimpleName()));
        }
    }

    @RunWith(Theories.class)
    public static class Array {
        @Test
        public void primitiveArray() {
            final TypeDef typeDef = TypeDef.newInstance(int[].class);
            assertThat(typeDef.getSimpleName(), is("int[]"));
            assertThat(typeDef.getFullName(), is("int[]"));
        }

        @Test
        public void primitiveArray2d() {
            final TypeDef typeDef = TypeDef.newInstance(int[][].class);
            assertThat(typeDef.getSimpleName(), is("int[][]"));
            assertThat(typeDef.getFullName(), is("int[][]"));
        }

        @Test
        public void referenceArray() {
            final TypeDef typeDef = TypeDef.newInstance(String[].class);
            assertThat(typeDef.getSimpleName(), is("String[]"));
            assertThat(typeDef.getFullName(), is("java.lang.String[]"));
        }

        @Test
        public void referenceArray2d() {
            final TypeDef typeDef = TypeDef.newInstance(String[][].class);
            assertThat(typeDef.getSimpleName(), is("String[][]"));
            assertThat(typeDef.getFullName(), is("java.lang.String[][]"));
        }

        @Test
        public void genericArray() throws NoSuchMethodException {
            final Type[] genericTypes = Arrays.class.getMethod("asList", Object[].class).getGenericParameterTypes();
            final TypeDef typeDef = TypeDef.newInstance(genericTypes[0]);
            assertThat(typeDef.getSimpleName(), is("T[]"));
            assertThat(typeDef.getFullName(), is("T[]"));
        }
    }

    @RunWith(Theories.class)
    public static class ParameterizedClass {
        @Test
        @Ignore
        public void a() {
            fail("no test");
        }
    }

    @RunWith(Theories.class)
    public static class TypeVariable {
        @Test
        @Ignore
        public void a() {
            fail("no test");
        }
    }

    @RunWith(Theories.class)
    public static class FunctionalInterface {
        @Test
        public void rawType() throws NoSuchMethodException {
            final TypeDef typeDef = TypeDef.newInstance(Function.class);
            assertThat(typeDef.getFullName(), is("java.util.function.Function"));
            assertThat(typeDef.getSimpleName(), is("(T -> R)"));
        }

        @Test
        public void parameteriezed() throws NoSuchMethodException {
            final Method streamMap = Stream.class.getMethod("map", Function.class);
            final TypeDef typeDef = TypeDef.newInstance(streamMap.getGenericParameterTypes()[0]);
            assertThat(typeDef.getFullName(), is("java.util.function.Function<? super T, ? extends R>"));
            assertThat(typeDef.getSimpleName(), is("(T -> R)"));
        }

        @Test
        public void noArguments() throws NoSuchMethodException {
            final Method streamMap = Stream.class.getMethod("generate", Supplier.class);
            final TypeDef typeDef = TypeDef.newInstance(streamMap.getGenericParameterTypes()[0]);
            assertThat(typeDef.getFullName(), is("java.util.function.Supplier<T>"));
            assertThat(typeDef.getSimpleName(), is("(() -> T)"));
        }

        @Test
        public void multipleArguments() throws NoSuchMethodException {
            final Method streamReduce = Stream.class.getMethod("reduce", BinaryOperator.class);
            final Type[] types = streamReduce.getGenericParameterTypes();
            final TypeDef typeDef = TypeDef.newInstance(types[0]);
            assertThat(typeDef.getFullName(), is("java.util.function.BinaryOperator<T>"));
            assertThat(typeDef.getSimpleName(), is("((T, T) -> T)"));
        }
    }

    @RunWith(Theories.class)
    public static class NonParameterizedClass {
        @Test
        @Ignore
        public void a() {
            fail("no test");
        }
    }
}
