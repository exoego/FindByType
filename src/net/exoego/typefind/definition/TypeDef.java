package net.exoego.typefind.definition;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TypeDef {
    private final PackageDef packageDef;
    private final String name;
    private final Optional<String> lambda;
    private final TypeKind kind;

    public static TypeDef newInstance(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType _type = (ParameterizedType) type;
            return new TypeDef(_type);
        } else if (type instanceof TypeVariable) {
            TypeVariable _type = (TypeVariable) type;
            return new TypeDef(_type);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType _type = (GenericArrayType) type;
            return new TypeDef(_type);
        } else if (type instanceof Class) {
            Class<?> _type = (Class<?>) type;
            return new TypeDef(_type);
        } else {
            throw new IllegalArgumentException("unknown subtype of Type: " + type.getClass());
        }
    }

    private TypeDef(Type type) {
        this.packageDef = PackageDef.of(type);
        this.name = type.getTypeName().replace(packageDef.getName() + ".", "");
        this.kind = TypeKind.what(type);
        this.lambda = lambdaIfFunctionalInterface(type);
    }

    @Override
    public String toString() {
        return "TypeDef{" +
               "packageDef=" + packageDef +
               ", name='" + name + '\'' +
               '}';
    }

    public String getFullName() {
        if (kind == TypeKind.VOID) {
            return "()";
        }
        final String s = packageDef.toString();
        return s.isEmpty() ? name : s + "." + name;
    }

    public String getSimpleName() {
        if (kind == TypeKind.VOID) {
            return "()";
        }
        if (kind == TypeKind.FUNCTIONAL_INTERFACE) {
            return lambda.orElse(name);
        }
        return name;
    }

    public Optional<String> lambdaIfFunctionalInterface(Type type) {
        if (kind != TypeKind.FUNCTIONAL_INTERFACE) {
            return Optional.empty();
        }
        if (type instanceof ParameterizedType) {
            return lambdaIfFunctionalInterface((((ParameterizedType) type).getRawType()));
        }
        final Class klass = (Class) type;
        final Predicate<Method> isDefault = Method::isDefault;
        final Predicate<Method> isAbstract = isDefault.negate();
        final Optional<Method> declaredSAM = Stream.of(klass.getDeclaredMethods())
                                                   .filter(m -> !Modifier.isStatic(m.getModifiers()))
                                                   .filter(isAbstract)
                                                   .findFirst();
        if (declaredSAM.isPresent()) {
            return declaredSAM.map(TypeDef::toLambda);
        }

        final Method inheritedSAM = Stream.of(klass.getMethods())
                                          .filter(m -> !Modifier.isStatic(m.getModifiers()))
                                          .filter(isAbstract)
                                          .findFirst()
                                          .get();
        final Class superClass = Stream.of(klass.getInterfaces())
                                       .filter(k -> inheritedSAM.getDeclaringClass().equals(k))
                                       .findFirst()
                                       .get();
        final TypeVariable[] typeParameters = superClass.getTypeParameters();

        final ParameterizedType parameterizedSuper = Stream.of(klass.getGenericInterfaces())
                                                           .filter(k -> k.getTypeName()
                                                                         .startsWith(superClass.getTypeName()))
                                                           .findFirst()
                                                           .map(ParameterizedType.class::cast)
                                                           .get();
        final Type[] actualTypeArguments = parameterizedSuper.getActualTypeArguments();

        if (typeParameters.length != actualTypeArguments.length) {
            final String message = String.format("number of type parameters unmatched: expected %s but %s",
                                                 typeParameters.length,
                                                 actualTypeArguments.length);
            throw new IllegalStateException(message);
        }

        final Map<TypeVariable, Type> toActual = new HashMap<TypeVariable, Type>();
        for (int i = 0; i < typeParameters.length; i++) {
            toActual.put(typeParameters[i], actualTypeArguments[i]);
        }

        final String lambda = toActual.entrySet().stream().reduce(toLambda(inheritedSAM), (l, entry) -> {
            final String tentative = entry.getKey().getTypeName();
            final String actual = entry.getValue().getTypeName();
            return Pattern.compile("\\b" + tentative + "\\b").matcher(l).replaceAll(actual);
        }, (lambda1, lambda2) -> /* combiner never used !! */null);
        return Optional.of(lambda);
    }

    private static String toLambda(final Method method) {
        final Type[] arguments = method.getGenericParameterTypes();
        final Type returnType = method.getGenericReturnType();
        return String.format("(%s -> %s)",
                             argumentsInSimpleNotation(arguments),
                             TypeDef.newInstance(returnType).getSimpleName());
    }

    private static String argumentsInSimpleNotation(Type[] arguments) {
        switch (arguments.length) {
            case 0:
                return "()";
            case 1:
                // arg
                return TypeDef.newInstance(arguments[0]).getSimpleName();
            default:
                // (arg1, arg2)
                final StringJoiner joiner = new StringJoiner(", ", "(", ")");
                for (Type arg : arguments) {
                    joiner.add(TypeDef.newInstance(arg).getSimpleName());
                }
                return joiner.toString();
        }
    }
}
