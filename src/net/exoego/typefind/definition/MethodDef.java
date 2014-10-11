package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
        this.declaringClass = TypeDef.forceGeneric(method.getDeclaringClass());

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

    private static String argumentsInSimpleNotation(List<TypeDef> arguments, Function<TypeDef, String> mapper) {
        switch (arguments.size()) {
            case 0:
                return "()";
            case 1:
                // arg
                return mapper.apply(arguments.get(0));
            default:
                // (arg1, arg2)
                final StringJoiner joiner = new StringJoiner(", ", "(", ")");
                for (TypeDef arg : arguments) {
                    joiner.add(mapper.apply(arg));
                }
                return joiner.toString();
        }
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

    public String getMethodName() {
        return name;
    }

    public String simple() {
        return full(TypeDef::getSimpleName, () -> "");
    }

    private String full(final Function<TypeDef, String> name, final Supplier<String> begin) {
        final List<TypeDef> args = new ArrayList<>();
        if (!getModifiers().contains(MethodModifier.Other.STATIC)) {
            args.add(declaringClass);
        }
        for (TypeDef arg : arguments) {
            args.add(arg);
        }
        final String argumentsString = argumentsInSimpleNotation(args, name);
        return String.format("%s%s -> %s", begin.get(), argumentsString, name.apply(returnType));
    }

    public String full() {
        return full(TypeDef::getFullName, () -> declaringClass.getFullName() +
                                                (getModifiers().contains(MethodModifier.Other.STATIC) ? "." : "#") +
                                                getMethodName() + ": ");
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
