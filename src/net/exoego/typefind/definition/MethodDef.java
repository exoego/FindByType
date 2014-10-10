package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.exoego.util.MoreCollectors.toImmutableList;
import static net.exoego.util.MoreCollectors.toImmutableSet;

public class MethodDef {
    private final Set<AnnotationDef> annotations;
    private final Set<TypeDef> exceptionTypes;
    private final List<TypeDef> arguments;
    private final Set<TypeDef> typeParameters;
    private final TypeDef returnType;
    private final TypeDef declaringClass;
    private final Set<MethodModifier> modifiers;
    private final String name;

    private MethodDef(Method method) {
        this.name = method.getName();
        this.returnType = TypeDef.newInstance(method.getGenericReturnType());
        this.declaringClass = TypeDef.newInstance(method.getDeclaringClass());
        this.annotations = Stream.of(method.getAnnotations()).map(AnnotationDef::new).collect(toImmutableSet());
        this.exceptionTypes = Stream.of(method.getExceptionTypes()).map(TypeDef::newInstance).collect(toImmutableSet());
        this.arguments = Stream.of(method.getGenericParameterTypes())
                               .map(TypeDef::newInstance)
                               .collect(toImmutableList());
        this.typeParameters = Stream.of(method.getTypeParameters()).map(TypeDef::newInstance).collect(toImmutableSet());
        this.modifiers = MethodModifier.extract(method).collect(toImmutableSet());
    }

    public static MethodDef newInstance(Method method) {
        return new MethodDef(method);
    }

    public Set<AnnotationDef> getDeclaredAnnotations() {
        return annotations;
    }

    public TypeDef getDeclaringClass() {
        return declaringClass;
    }

    public Set<TypeDef> getExceptionType() {
        return exceptionTypes;
    }

    public List<TypeDef> getArguments() {
        return arguments;
    }

    public TypeDef getReturnType() {
        return returnType;
    }

    public Set<TypeDef> getTypeParameters() {
        return typeParameters;
    }

    public Set<MethodModifier> getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    public String toLambdaNotation() {
        return full(TypeDef::getSimpleName, () -> "");
    }

    private String full(final Function<TypeDef, String> name, final Supplier<String> begin) {
        final StringJoiner stringJoiner = new StringJoiner(" -> ", begin.get(), "");

        if (getModifiers().contains(MethodModifier.Other.STATIC)) {
            if (arguments.isEmpty()) {
                stringJoiner.add("()");
            }
        } else {
            stringJoiner.add(name.apply(declaringClass));
        }

        for (TypeDef arg : arguments) {
            stringJoiner.add(name.apply(arg));
        }

        stringJoiner.add(name.apply(returnType));
        return stringJoiner.toString();
    }

    public String full() {
        return full(TypeDef::getFullName, () -> declaringClass.getFullName() +
                                                (getModifiers().contains(MethodModifier.Other.STATIC) ? "." : "#") +
                                                getName() + ": ");
    }

    @Override
    public String toString() {
        return "MethodDef{" +
               "\n    " + full() +
               "\n    annotations=" + annotations +
               "\n    exceptionTypes=" + exceptionTypes +
               "\n    arguments=" + arguments +
               "\n    typeParameters=" + typeParameters +
               "\n    returnType=" + returnType +
               "\n    declaringClass=" + declaringClass +
               "\n    modifiers=" + modifiers +
               "\n    name='" + name + '\'' +
               '}';
    }
}
