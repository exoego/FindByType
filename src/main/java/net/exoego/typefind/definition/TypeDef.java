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
    private static final Pattern EXCEPT_MODIFIERS = Pattern.compile(
            "\\b(?:public|protected|private|class|interface|enum|abstract|native|static|strictfp|final|synchronized) \\b");
    private static final Predicate<Method> IS_ABSTRACT;
    private static final Predicate<Method> UNDEFINED_IN_OBJECT = method -> {
        try {
            final Class<?>[] params = method.getParameterTypes();
            final String methodName = method.getName();
            final Method objectMethod = Object.class.getMethod(methodName, params);
            return false;
        } catch (NoSuchMethodException e) {
            return true;
        }
    };
    private final PackageDef packageDef;
    private final String name;
    private final Optional<String> lambda;
    private final TypeKind kind;

    private TypeDef(Type type) {
        this.packageDef = PackageDef.of(type);
        this.name = type.getTypeName().replace(packageDef.getName() + ".", "");
        this.kind = TypeKind.what(type);
        this.lambda = lambdaIfFunctionalInterface(type);
    }

    private TypeDef(Type type, String genericString) {
        this.packageDef = PackageDef.of(type);
        final String genericTypeName = EXCEPT_MODIFIERS.matcher(genericString).replaceAll("");
        this.name = genericTypeName.replace(packageDef.getName() + ".", "");
        this.kind = TypeKind.what(type);
        this.lambda = lambdaIfFunctionalInterface(type);
    }

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

    public static TypeDef forceGeneric(Class<?> klass) {
        return new TypeDef(klass, klass.toGenericString());
    }

    private static Optional<String> aaaa(Method sam, Map<TypeVariable, Type> relation) {
        final String lambda = relation.entrySet().stream().reduce(toLambda(sam), (l, entry) -> {
            final String tentative = entry.getKey().getTypeName();
            // TODO: Handle upper/lower bounds in better way...
            final String actual = entry.getValue().getTypeName().replaceAll("\\? (super|extends) ", "");
            return Pattern.compile("\\b" + tentative + "\\b").matcher(l).replaceAll(actual);
        }, (lambda1, lambda2) -> /* combiner never used !! */null);
        return Optional.of(lambda);
    }

    private static Map<TypeVariable, Type> typeParamRelation(TypeVariable[] variables, Type[] actual) {
        final Map<TypeVariable, Type> toActual = new HashMap<TypeVariable, Type>();
        if (actual.length == 0) {
            for (TypeVariable variable : variables) {
                toActual.put(variable, variable);
            }
        } else {
            for (int i = 0; i < variables.length; i++) {
                toActual.put(variables[i], actual[i]);
            }
        }
        return toActual;
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

    static {
        final Predicate<Method> isDefault = Method::isDefault;
        IS_ABSTRACT = isDefault.negate();
    }

    public Optional<String> lambdaIfFunctionalInterface(Type type) {
        if (kind != TypeKind.FUNCTIONAL_INTERFACE) {
            return Optional.empty();
        }
        final Type[] actualTypeArgsOfClass;
        if (type instanceof ParameterizedType) {
            actualTypeArgsOfClass = ((ParameterizedType) type).getActualTypeArguments();
            type = ((ParameterizedType) type).getRawType();
        } else {
            actualTypeArgsOfClass = new Type[0];
        }
        final Class klass = (Class) type;
        final TypeVariable[] typeParamsOfClass = klass.getTypeParameters();

        final Optional<Method> declaredSAM = Stream.of(klass.getDeclaredMethods())
                                                   .filter(m -> !Modifier.isStatic(m.getModifiers()))
                                                   .filter(IS_ABSTRACT)
                                                   .filter(UNDEFINED_IN_OBJECT)
                                                   .findFirst();
        if (declaredSAM.isPresent()) {
            final Map<TypeVariable, Type> typeVariableTypeMap = typeParamRelation(typeParamsOfClass,
                                                                                  actualTypeArgsOfClass);
            return aaaa(declaredSAM.get(), typeVariableTypeMap);
        }
        final Method inheritedSAM = Stream.of(klass.getMethods())
                                          .filter(m -> !Modifier.isStatic(m.getModifiers()))
                                          .filter(IS_ABSTRACT)
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

        final Map<TypeVariable, Type> toActual = typeParamRelation(typeParameters, actualTypeArguments);
        return aaaa(inheritedSAM, toActual);
    }
}
