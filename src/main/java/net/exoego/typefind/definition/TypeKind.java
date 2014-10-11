package net.exoego.typefind.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum TypeKind {
    PRIMITIVE,
    VOID,
    ARRAY,
    GENERIC_ARRAY,
    PARAMETERIZED_TYPE,
    FUNCTIONAL_INTERFACE,
    TYPE_VARIABLE,
    CLASS;

    public static TypeKind what(Type type) {
        if (type instanceof ParameterizedType) {
            final Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class && isFunctionalInterface((Class) rawType)) {
                return FUNCTIONAL_INTERFACE;
            }
            return PARAMETERIZED_TYPE;
        }
        if (type instanceof TypeVariable) {
            return TYPE_VARIABLE;
        }
        if (type instanceof Class && isFunctionalInterface((Class) type)) {
            return FUNCTIONAL_INTERFACE;
        }
        final String typeName = type.getTypeName();
        switch (typeName) {
            case "void":
                return VOID;
            case "int":
            case "long":
            case "float":
            case "double":
            case "short":
            case "boolean":
            case "byte":
            case "char":
                return PRIMITIVE;
        }
        if (typeName.endsWith("[]")) {
            if (type instanceof GenericArrayType) {
                return GENERIC_ARRAY;
            }
            return ARRAY;
        }
        return CLASS;
    }

    public static boolean isFunctionalInterface(Class<?> klass) {
        if (klass.isInterface()) {
            final Annotation annotation = klass.getAnnotation(FunctionalInterface.class);
            if (annotation != null) {
                return true;
            }
            final Stream<Method> declaredMethods = Stream.of(klass.getDeclaredMethods());
            final Predicate<Method> isDefault = Method::isDefault;
            final Predicate<Method> isAbstract = isDefault.negate();
            final boolean hasOnlyOneAbstractMethod = declaredMethods.filter(isAbstract).count() == 1;
            return hasOnlyOneAbstractMethod;
        }
        return false;
    }

}
