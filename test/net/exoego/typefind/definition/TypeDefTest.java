package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public static class NonFunctionlInterface_ParameterizedClass {
        @Test
        public void has_upper_bound() throws NoSuchMethodException {
            final Method method = Collections.class.getMethod("unmodifiableList", List.class);
            final TypeDef typeDef = TypeDef.newInstance(method.getGenericParameterTypes()[0]);
            assertThat(typeDef.getFullName(), is("java.util.List<? extends T>"));
            assertThat(typeDef.getSimpleName(), is("List<? extends T>"));
        }
    }

    @RunWith(Theories.class)
    public static class TypeVariable {
        @Test
        public void typeVariable() throws NoSuchMethodException {
            final Method add = List.class.getMethod("add", Object.class);
            final TypeDef typeDef = TypeDef.newInstance(add.getGenericParameterTypes()[0]);
            assertThat(typeDef.getFullName(),is("E"));
            assertThat(typeDef.getSimpleName(), is("E"));
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
        public void parameterized() throws NoSuchMethodException {
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
        public void noArguments_primitive_return() throws NoSuchMethodException {
            final TypeDef typeDef = TypeDef.newInstance(BooleanSupplier.class);
            assertThat(typeDef.getFullName(), is("java.util.function.BooleanSupplier"));
            assertThat(typeDef.getSimpleName(), is("(() -> boolean)"));
        }

        @Test
        public void multiple_reference_arguments() throws NoSuchMethodException {
            final Method streamReduce = Stream.class.getMethod("reduce", BinaryOperator.class);
            final TypeDef typeDef = TypeDef.newInstance(streamReduce.getGenericParameterTypes()[0]);
            assertThat(typeDef.getFullName(), is("java.util.function.BinaryOperator<T>"));
            assertThat(typeDef.getSimpleName(), is("((T, T) -> T)"));
        }

        @Test
        public void multiple_primitive_arguments() throws NoSuchMethodException {
            final Method reduce = IntStream.class.getMethod("reduce", IntBinaryOperator.class);
            final TypeDef typeDef = TypeDef.newInstance(reduce.getGenericParameterTypes()[0]);
            assertThat(typeDef.getFullName(), is("java.util.function.IntBinaryOperator"));
            assertThat(typeDef.getSimpleName(), is("((int, int) -> int)"));
        }

        @Test
        public void ignore_method_defined_in_Object() throws NoSuchMethodException {
            // Comparator has 2 abstract method but can be considered to have single one.
            // Because abstract methods which is defined in Object is can be ignored.
            final Method reduce = List.class.getMethod("sort", Comparator.class);
            final TypeDef typeDef = TypeDef.newInstance(reduce.getGenericParameterTypes()[0]);
            assertThat(typeDef.getFullName(), is("java.util.Comparator<? super E>"));
            assertThat(typeDef.getSimpleName(), is("((E, E) -> int)"));
        }

    }

    @RunWith(Theories.class)
    public static class NonParameterizedClass {
        @Test
        public void non_parameterized_interface() {
            final TypeDef typeDef = TypeDef.newInstance(Cloneable.class);
            assertThat(typeDef.getFullName(), is("java.lang.Cloneable"));
            assertThat(typeDef.getSimpleName(), is("Cloneable"));
        }

        @Test
        public void non_parameterized_class() {
            final TypeDef typeDef = TypeDef.newInstance(Calendar.class);
            assertThat(typeDef.getFullName(), is("java.util.Calendar"));
            assertThat(typeDef.getSimpleName(), is("Calendar"));
        }
    }
}
