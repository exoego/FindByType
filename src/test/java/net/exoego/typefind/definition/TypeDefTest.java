package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
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
import org.w3c.dom.views.AbstractView;

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
        public void canonical_name_has_no_package() {
            assertThat(TypeDef.newInstance(int.class).getCanonicalName(), is("int"));
        }

        @Theory
        public void canonical_name_equals_to_type_name_of_class(Class<?> primitive) {
            assertThat(TypeDef.newInstance(primitive).getCanonicalName(), is(primitive.getTypeName()));
        }

        @Theory
        public void canonical_name_equlals_to_type_name(Class<?> primitive) {
            final TypeDef primitiveDef = TypeDef.newInstance(primitive);
            assertThat(primitiveDef.getCanonicalName(), is(primitiveDef.getTypeName()));
            assertThat(primitiveDef.getCanonicalName(), is(primitiveDef.getSimpleForm()));
        }
    }

    public static class Void {
        @Test
        public void parens() {
            assertThat(TypeDef.newInstance(java.lang.Void.TYPE).getCanonicalName(), is("()"));
        }

        @Test
        public void canonical_name_equals_to_simple_name() {
            final TypeDef voidType = TypeDef.newInstance(java.lang.Void.TYPE);
            assertThat(voidType.getCanonicalName(), is(voidType.getSimpleForm()));
            assertThat(voidType.getCanonicalName(), is(voidType.getTypeName()));
        }
    }

    public static class Array {
        @Test
        public void primitiveArray() {
            final TypeDef typeDef = TypeDef.newInstance(int[].class);
            assertThat(typeDef.getTypeName(), is("int[]"));
            assertThat(typeDef.getSimpleForm(), is("int[]"));
            assertThat(typeDef.getCanonicalName(), is("int[]"));
        }

        @Test
        public void primitiveArray2d() {
            final TypeDef typeDef = TypeDef.newInstance(int[][].class);
            assertThat(typeDef.getTypeName(), is("int[][]"));
            assertThat(typeDef.getSimpleForm(), is("int[][]"));
            assertThat(typeDef.getCanonicalName(), is("int[][]"));
        }

        @Test
        public void referenceArray() {
            final TypeDef typeDef = TypeDef.newInstance(String[].class);
            assertThat(typeDef.getSimpleForm(), is("String[]"));
            assertThat(typeDef.getCanonicalName(), is("java.lang.String[]"));
        }

        @Test
        public void referenceArray2d() {
            final TypeDef typeDef = TypeDef.newInstance(String[][].class);
            assertThat(typeDef.getTypeName(), is("String[][]"));
            assertThat(typeDef.getSimpleForm(), is("String[][]"));
            assertThat(typeDef.getCanonicalName(), is("java.lang.String[][]"));
        }

        @Test
        public void genericArray() throws NoSuchMethodException {
            final Type[] genericTypes = Arrays.class.getMethod("asList", Object[].class).getGenericParameterTypes();
            final TypeDef typeDef = TypeDef.newInstance(genericTypes[0]);
            assertThat(typeDef.getTypeName(), is("T[]"));
            assertThat(typeDef.getSimpleForm(), is("T[]"));
            assertThat(typeDef.getCanonicalName(), is("T[]"));
        }
    }

    public static class NonFunctionlInterface_ParameterizedClass {
        @Test
        public void has_upper_bound() throws NoSuchMethodException {
            final Method method = Collections.class.getMethod("unmodifiableList", List.class);
            final TypeDef typeDef = TypeDef.newInstance(method.getGenericParameterTypes()[0]);
            assertThat(typeDef.getTypeName(), is("List<? extends T>"));
            assertThat(typeDef.getSimpleForm(), is("List<? extends T>"));
            assertThat(typeDef.getCanonicalName(), is("java.util.List<? extends T>"));
        }
    }

    public static class TypeVariable {
        public static Supplier<Spliterator.OfInt> get() {
            return null;
        }

        @Test
        public void typeVariable() throws NoSuchMethodException {
            final Method add = List.class.getMethod("add", Object.class);
            final TypeDef typeDef = TypeDef.newInstance(add.getGenericParameterTypes()[0]);
            assertThat(typeDef.getTypeName(), is("E"));
            assertThat(typeDef.getSimpleForm(), is("E"));
            assertThat(typeDef.getCanonicalName(), is("E"));
        }

        @Test
        public void return_type_of_SAM_is_show_its_class_name_form() throws NoSuchMethodException {
            final TypeDef typeDef = TypeDef.newInstance(AbstractView.class);
            assertThat(typeDef.getTypeName(), is("AbstractView"));
            assertThat(typeDef.getSimpleForm(), is("(() -> DocumentView)"));
            assertThat(typeDef.getCanonicalName(), is("org.w3c.dom.views.AbstractView"));
        }

        @Test
        public void argument_of_SAM_is_show_its_class_name() throws NoSuchMethodException {
            final TypeDef typeDef = TypeDef.newInstance(Odd.class);
            assertThat(typeDef.getSimpleForm(), is("((TypeDefTest$TypeVariable$Even, int) -> int)"));
        }

        @java.lang.FunctionalInterface
        public static interface Odd {
            int test(Even predicate, int index);
        }

        @java.lang.FunctionalInterface
        public static interface Even {
            int test(Even predicate, int index);
        }
    }

    public static class FunctionalInterface {
        @Test
        public void rawType() throws NoSuchMethodException {
            final TypeDef typeDef = TypeDef.newInstance(Function.class);
            assertThat(typeDef.getTypeName(), is("Function"));
            assertThat(typeDef.getSimpleForm(), is("(T -> R)"));
            assertThat(typeDef.getCanonicalName(), is("java.util.function.Function"));
        }

        @Test
        public void parameterized() throws NoSuchMethodException {
            final Method streamMap = Stream.class.getMethod("map", Function.class);
            final TypeDef typeDef = TypeDef.newInstance(streamMap.getGenericParameterTypes()[0]);
            assertThat(typeDef.getTypeName(), is("Function<? super T, ? extends R>"));
            assertThat(typeDef.getSimpleForm(), is("(T -> R)"));
            assertThat(typeDef.getCanonicalName(), is("java.util.function.Function<? super T, ? extends R>"));
        }

        @Test
        public void noArguments() throws NoSuchMethodException {
            final Method streamMap = Stream.class.getMethod("generate", Supplier.class);
            final TypeDef typeDef = TypeDef.newInstance(streamMap.getGenericParameterTypes()[0]);
            assertThat(typeDef.getTypeName(), is("Supplier<T>"));
            assertThat(typeDef.getSimpleForm(), is("(() -> T)"));
            assertThat(typeDef.getCanonicalName(), is("java.util.function.Supplier<T>"));
        }

        @Test
        public void noArguments_primitive_return() throws NoSuchMethodException {
            final TypeDef typeDef = TypeDef.newInstance(BooleanSupplier.class);
            assertThat(typeDef.getTypeName(), is("BooleanSupplier"));
            assertThat(typeDef.getSimpleForm(), is("(() -> boolean)"));
            assertThat(typeDef.getCanonicalName(), is("java.util.function.BooleanSupplier"));
        }

        @Test
        public void multiple_reference_arguments() throws NoSuchMethodException {
            final Method streamReduce = Stream.class.getMethod("reduce", BinaryOperator.class);
            final TypeDef typeDef = TypeDef.newInstance(streamReduce.getGenericParameterTypes()[0]);
            assertThat(typeDef.getTypeName(), is("BinaryOperator<T>"));
            assertThat(typeDef.getSimpleForm(), is("((T, T) -> T)"));
            assertThat(typeDef.getCanonicalName(), is("java.util.function.BinaryOperator<T>"));
        }

        @Test
        public void multiple_primitive_arguments() throws NoSuchMethodException {
            final Method reduce = IntStream.class.getMethod("reduce", IntBinaryOperator.class);
            final TypeDef typeDef = TypeDef.newInstance(reduce.getGenericParameterTypes()[0]);
            assertThat(typeDef.getTypeName(), is("IntBinaryOperator"));
            assertThat(typeDef.getSimpleForm(), is("((int, int) -> int)"));
            assertThat(typeDef.getCanonicalName(), is("java.util.function.IntBinaryOperator"));
        }

        @Test
        public void ignore_method_defined_in_Object() throws NoSuchMethodException {
            // Comparator has 2 abstract method but can be considered to have single one.
            // Because abstract methods which is defined in Object is can be ignored.
            final Method reduce = List.class.getMethod("sort", Comparator.class);
            final TypeDef typeDef = TypeDef.newInstance(reduce.getGenericParameterTypes()[0]);
            assertThat(typeDef.getTypeName(), is("Comparator<? super E>"));
            assertThat(typeDef.getSimpleForm(), is("((E, E) -> int)"));
            assertThat(typeDef.getCanonicalName(), is("java.util.Comparator<? super E>"));
        }
    }

    public static class NonParameterizedClass {
        @Test
        public void non_parameterized_interface() {
            final TypeDef typeDef = TypeDef.newInstance(Cloneable.class);
            assertThat(typeDef.getTypeName(), is("Cloneable"));
            assertThat(typeDef.getSimpleForm(), is("Cloneable"));
            assertThat(typeDef.getCanonicalName(), is("java.lang.Cloneable"));
        }

        @Test
        public void non_parameterized_class() {
            final TypeDef typeDef = TypeDef.newInstance(Calendar.class);
            assertThat(typeDef.getTypeName(), is("Calendar"));
            assertThat(typeDef.getSimpleForm(), is("Calendar"));
            assertThat(typeDef.getCanonicalName(), is("java.util.Calendar"));
        }
    }
}
