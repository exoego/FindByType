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
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TypeDef {
    private static final Pattern EXCEPT_MODIFIERS = Pattern.compile(
            "\\b(?:public|protected|private|class|interface|enum|abstract|native|static|strictfp|final|synchronized) \\b");
    private final PackageDef packageDef;
    private final String typeName;
    private final String canonicalName;
    private final String simpleForm;
    private final TypeKind kind;
    private final boolean isDeprecated;

    private TypeDef(Type type) {
        this(type, LambdaExpression.USE);
    }

    private TypeDef(Type type, LambdaExpression flag) {
        this(type, (packageDef) -> type.getTypeName().replace(packageDef.getName() + ".", ""), flag);
    }

    private TypeDef(Type type, String genericString, LambdaExpression flag) {
        this(type, (packageDef) -> {
            final String genericTypeName = EXCEPT_MODIFIERS.matcher(genericString).replaceAll("");
            return genericTypeName.replace(packageDef.getName() + ".", "");
        }, flag);
    }

    private TypeDef(Type type, Function<PackageDef, String> a, LambdaExpression flag) {
        this.packageDef = PackageDef.of(type);
        this.kind = TypeKind.what(type);
        if (kind == TypeKind.VOID) {
            this.typeName = "()";
            this.simpleForm = "()";
            this.canonicalName = "()";
        } else {
            this.typeName = a.apply(packageDef);
            if (kind == TypeKind.FUNCTIONAL_INTERFACE && flag == LambdaExpression.USE) {
                this.simpleForm = lambdaIfFunctionalInterface(type).orElse(typeName);
            } else {
                this.simpleForm = typeName;
            }
            final String s = packageDef.toString();
            this.canonicalName = s.isEmpty() ? typeName : s + "." + typeName;
        }
        isDeprecated = isDeprecated(type);
    }

    private static boolean isDeprecated(Type type) {
        if (type instanceof Class) {
            return ((Class) type).getAnnotation(Deprecated.class) != null;
        } else if (type instanceof ParameterizedType) {
            try {
                final Type rawType = ((ParameterizedType) type).getRawType();
                return ((Class) rawType).getAnnotation(Deprecated.class) != null;
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static TypeDef newInstance(Type type) {
        if (type instanceof ParameterizedType || type instanceof TypeVariable ||
            type instanceof GenericArrayType || type instanceof Class) {
            return new TypeDef(type);
        } else {
            throw new IllegalArgumentException("unknown subtype of Type: " + type.getClass());
        }
    }

    private static TypeDef forceClassNameFormEvenIfFunctionalInterface(Type type) {
        if (type instanceof ParameterizedType || type instanceof TypeVariable ||
            type instanceof GenericArrayType || type instanceof Class) {
            return new TypeDef(type, LambdaExpression.NOT_USE);
        } else {
            throw new IllegalArgumentException("unknown subtype of Type: " + type.getClass());
        }
    }

    public static TypeDef forceGeneric(Class<?> klass) {
        return new TypeDef(klass, klass.toGenericString(), LambdaExpression.USE);
    }

    private static Optional<String> typeVariableToActual(
            Method sam, TypeVariable[] variables, Type[] actual) {
        final Map<TypeVariable, Type> relation = typeParamRelation(variables, actual);
        final BinaryOperator<String> neverUsed = (lambda1, lambda2) -> null;
        final String lambda = relation.entrySet().stream().reduce(toLambda(sam), (l, entry) -> {
            final String tentative = entry.getKey().getTypeName();
            final String actualName = entry.getValue()
                                           .getTypeName()
                                           .replaceAll("\\? (super|extends) ", "")
                                           .replace("$", "__");
            // TODO: Handle upper/lower bounds in better way...
            return Pattern.compile("\\b" + tentative + "\\b").matcher(l).replaceAll(actualName);
        }, neverUsed);
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
        final TypeDef typeDef = TypeDef.forceClassNameFormEvenIfFunctionalInterface(returnType);
        final String returnTypeString = typeDef.getSimpleForm();
        return String.format("%s -> %s", argumentsInSimpleNotation(parameterTypes), returnTypeString);
    }

    private static String argumentsInSimpleNotation(Type[] arguments) {
        switch (arguments.length) {
            case 0:
                return "()";
            case 1:
                // arg
                return TypeDef.forceClassNameFormEvenIfFunctionalInterface(arguments[0]).getSimpleForm();
            default:
                // (arg1, arg2)
                final StringJoiner joiner = new StringJoiner(", ", "(", ")");
                for (Type arg : arguments) {
                    joiner.add(TypeDef.forceClassNameFormEvenIfFunctionalInterface(arg).getSimpleForm());
                }
                return joiner.toString();
        }
    }

    private static Optional<String> chooseDeclaredSamOrInheritedSam(
            final Class thisClass, final Type[] actualTypeArgsOfClass) {
        final Optional<Method> declaredSAM = findDeclaredSAM(thisClass);
        if (declaredSAM.isPresent()) {
            return typeVariableToActual(declaredSAM.get(), thisClass.getTypeParameters(), actualTypeArgsOfClass);
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
                     .filter(MethodDef::isAbstract)
                     .filter(MethodDef::undefinedInObject)
                     .findFirst();
    }

    private static ParameterizedType superClassAsParameterized(final Class thisClass, final Class superClass) {
        return Stream.of(thisClass.getGenericInterfaces())
                     .filter(k -> k.getTypeName().startsWith(superClass.getTypeName()))
                     .findFirst()
                     .map(ParameterizedType.class::cast)
                     .orElseThrow(() -> new IllegalStateException(String.format("thisClass:%s superclass:%s",
                                                                                thisClass,
                                                                                superClass)));
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
                     .filter(MethodDef::isAbstract)
                     .findFirst()
                     .get();
    }

    /**
     * Test if the class itself is public and all enclosing classes are public.
     *
     * @param self the Class instance.
     * @return true if given class is public and its all enclosing classes are public.
     */
    public static boolean isPublic(Class<?> self) {
        try {
            while (true) {
                if (!Modifier.isPublic(self.getModifiers())) {
                    return false;
                }
                self = self.getEnclosingClass();
                if (self == null) {
                    return true;
                }
            }
        } catch (NoClassDefFoundError ex) {
            return false;
        }
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public TypeKind getKind() {
        return kind;
    }

    public PackageDef getPackageDef() {
        return packageDef;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Returns lambda-expression if this type is functional interface, otherwise just type name.
     *
     * @return
     */
    public String getSimpleForm() {
        return simpleForm;
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

    @Override
    public int hashCode() {
        return canonicalName.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TypeDef typeDef = (TypeDef) o;
        return canonicalName.equals(typeDef.canonicalName);
    }

    @Override
    public String toString() {
        return "TypeDef{" +
               "packageDef=" + packageDef +
               ", canonicalName='" + canonicalName + '\'' +
               ", typeName='" + typeName + '\'' +
               ", simpleForm='" + simpleForm + '\'' +
               ", kind=" + kind +
               '}';
    }

    private static enum LambdaExpression {USE, NOT_USE}
}

