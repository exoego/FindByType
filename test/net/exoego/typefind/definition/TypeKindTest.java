package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
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
public class TypeKindTest {
    @RunWith(Theories.class)
    public static class Primitive {
        @DataPoints
        public static final Class<?>[] PRIMITIVES = {
                boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class
        };

        @Theory
        public void primitive(Class<?> primitive) {
            assertThat(TypeKind.what(primitive), is(TypeKind.PRIMITIVE));
        }
    }

    @RunWith(Theories.class)
    public static class Array {
        @DataPoints
        public static final Class<?>[] ARRAYS = {
                int[].class, String[].class, String[][].class, List[].class
        };

        @Theory
        public void primitive(Class<?> array) {
            assertThat(TypeKind.what(array), is(TypeKind.ARRAY));
        }

        @Test
        public void genericArray() throws NoSuchMethodException {
            final Method asList = Arrays.class.getMethod("asList", Object[].class);
            assertThat(TypeKind.what(asList.getParameterTypes()[0]), is(TypeKind.ARRAY));
            assertThat(TypeKind.what(asList.getGenericParameterTypes()[0]), is(TypeKind.GENERIC_ARRAY));
        }
    }

    public static class Other {
        @Test
        public void voidType() throws NoSuchMethodException {
            final Type returnType = List.class.getDeclaredMethod("clear").getReturnType();
            assertThat(TypeKind.what(returnType), is(TypeKind.VOID));
            assertThat(TypeKind.what(Void.TYPE), is(TypeKind.VOID));
        }

        @Test
        public void parameterizedType() throws NoSuchMethodException {
            final Type[] params = List.class.getDeclaredMethod("addAll", Collection.class).getGenericParameterTypes();
            assertThat(TypeKind.what(params[0]), is(TypeKind.PARAMETERIZED_TYPE));
        }

        @Test
        public void typeVariable() throws NoSuchMethodException {
            // typeVariable E of List<E>
            final Type E = List.class.getDeclaredMethod("get", int.class).getGenericReturnType();
            assertThat(TypeKind.what(E), is(TypeKind.TYPE_VARIABLE));
        }

        @Test
        public void rawType_is_class() throws NoSuchMethodException {
            final Type rawType = List.class;
            assertThat(TypeKind.what(rawType), is(TypeKind.CLASS));
        }

        @Test
        public void class_and_interface_is_class() throws NoSuchMethodException {
            assertThat(TypeKind.what(String.class), is(TypeKind.CLASS));
            assertThat(TypeKind.what(Cloneable.class), is(TypeKind.CLASS));
        }
    }

    public static class FunctionalInterfaceTest {
        @Test
        public void FunctionalInterface_if_annotated_so() throws NoSuchMethodException {
            assertThat(TypeKind.isFunctionalInterface(Function.class), is(true));
            assertThat(TypeKind.isFunctionalInterface(UnaryOperator.class), is(true));
        }

        @Test
        public void FunctionalInterface_if_not_annotated_but_SAM_interface() throws NoSuchMethodException {
            assertThat(TypeKind.isFunctionalInterface(SAM.class), is(true));
            assertThat(TypeKind.isFunctionalInterface(EffectivelySAM.class), is(true));
        }

        @Test
        public void Not_FunctionalInterface_if_not_SAM() throws NoSuchMethodException {
            assertThat(TypeKind.isFunctionalInterface(List.class), is(false));
            assertThat(TypeKind.isFunctionalInterface(NonSAM.class), is(false));
            assertThat(TypeKind.isFunctionalInterface(Cloneable.class), is(false));
        }

        @Test
        public void Not_FunctionalInterface_if_SAM_but_abstract_class() throws NoSuchMethodException {
            assertThat(TypeKind.isFunctionalInterface(SAMButAbstractClass.class), is(false));
        }

        @Test
        public void classified_as_functional_interface() {
            final Predicate<TypeKind> isFunctionalInterfaceKind = Predicate.isEqual(TypeKind.FUNCTIONAL_INTERFACE);
            assertThat(Stream.of(Function.class, UnaryOperator.class, SAM.class, EffectivelySAM.class)
                             .map(TypeKind::what)
                             .allMatch(isFunctionalInterfaceKind), is(true));
            assertThat(Stream.of(List.class, NonSAM.class, Cloneable.class, SAMButAbstractClass.class)
                             .map(TypeKind::what)
                             .noneMatch(isFunctionalInterfaceKind), is(true));
        }

        // SAM interface, no @FunctionalInterface
        static interface SAM {
            boolean get();
        }

        // SAM but Abstract class
        static abstract class SAMButAbstractClass {
            abstract boolean get();
        }

        // Effectively SAM with one default method
        static interface EffectivelySAM extends SAM {
            int other();

            default boolean get() {
                return other() == 0;
            }
        }

        // Non-SAM interface
        static interface NonSAM extends SAM {
            int other();

            String wow();

            default boolean get() {
                return other() == 0;
            }
        }
    }
}
