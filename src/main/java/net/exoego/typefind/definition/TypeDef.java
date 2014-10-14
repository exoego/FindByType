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

    static {
        final Predicate<Method> isDefault = Method::isDefault;
        IS_ABSTRACT = isDefault.negate();
    }

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
        if (type instanceof ParameterizedType || type instanceof TypeVariable ||
            type instanceof GenericArrayType || type instanceof Class) {
            return new TypeDef(type);
        } else {
            throw new IllegalArgumentException("unknown subtype of Type: " + type.getClass());
        }
    }

    public static TypeDef forceGeneric(Class<?> klass) {
        return new TypeDef(klass, klass.toGenericString());
    }

    private static Optional<String> typeVariableToActual(
            Method sam,
            TypeVariable[] variables,
            Type[] actual) {
        final Map<TypeVariable, Type> relation = typeParamRelation(variables, actual);
        final String lambda = relation.entrySet().stream().reduce(toLambda(sam), (l, entry) -> {
            final String tentative = entry.getKey().getTypeName();
            // TODO: Handle upper/lower bounds in better way...
            final String actualName = entry.getValue().getTypeName().replaceAll("\\? (super|extends) ", "");
            return Pattern.compile("\\b" + tentative + "\\b").matcher(l).replaceAll(actualName);
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
        final Type[] parameterTypes = method.getGenericParameterTypes();
        final Type returnType = method.getGenericReturnType();
        return String.format("(%s -> %s)",
                             argumentsInSimpleNotation(parameterTypes),
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

    private Optional<String> lambdaIfFunctionalInterface(Type type) {
        if (kind != TypeKind.FUNCTIONAL_INTERFACE) {
            return Optional.empty();
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterized = (ParameterizedType) type;
            return chooseDeclaredSamOrInheritedSam((Class) parameterized.getRawType(),
                                                   parameterized.getActualTypeArguments());
        }
        return chooseDeclaredSamOrInheritedSam((Class) type, new Type[]{});
    }

    private static Optional<String> chooseDeclaredSamOrInheritedSam(
            final Class thisClass,
            final Type[] actualTypeArgsOfClass) {
        final Optional<Method> declaredSAM = findDeclaredSAM(thisClass);
        if (declaredSAM.isPresent()) {
            return typeVariableToActual(declaredSAM.get(),
                                        thisClass.getTypeParameters(),
                                        actualTypeArgsOfClass);
        }
        final Method inheritedSAM = findInheritedSAM(thisClass);
        final Class superClass = findDeclaringClassOfInheritedSAM(thisClass, inheritedSAM);
        final ParameterizedType parameterizedSuper = superClassAsParameterized(thisClass, superClass);
        return typeVariableToActual(inheritedSAM,
                                    superClass.getTypeParameters(),
                                    parameterizedSuper.getActualTypeArguments());
    }

    private static Optional<Method> findDeclaredSAM(final Class thisClass) {
        return Stream.of(thisClass.getDeclaredMethods())
                     .filter(method -> !Modifier.isStatic(method.getModifiers()))
                     .filter(IS_ABSTRACT)
                     .filter(UNDEFINED_IN_OBJECT)
                     .findFirst();
    }

    private static ParameterizedType superClassAsParameterized(final Class thisClass, final Class superClass) {
        return Stream.of(thisClass.getGenericInterfaces())
                     .filter(k -> k.getTypeName().startsWith(superClass.getTypeName()))
                     .findFirst()
                     .map(ParameterizedType.class::cast)
                     .get();
    }

    private static Class findDeclaringClassOfInheritedSAM(final Class thisClass, final Method inheritedSAM) {
        return Stream.of(thisClass.getInterfaces())
                     .filter(klass -> inheritedSAM.getDeclaringClass().equals(klass))
                     .findFirst()
                     .get();
    }

    private static Method findInheritedSAM(final Class thisClass) {
        return Stream.of(thisClass.getMethods())
                     .filter(method -> !Modifier.isStatic(method.getModifiers()))
                     .filter(IS_ABSTRACT)
                     .findFirst()
                     .get();
    }
}

