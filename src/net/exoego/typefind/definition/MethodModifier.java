package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

public interface MethodModifier {
    public static Stream<MethodModifier> extract(Method method) {
        final int modifier = method.getModifiers();
        final Stream.Builder<MethodModifier> builder = Stream.builder();
        // Access Level
        if (Modifier.isPublic(modifier)) {
            builder.accept(AccessLevel.PUBLIC);
        } else if (Modifier.isProtected(modifier)) {
            builder.accept(AccessLevel.PROTECTED);
        } else if (Modifier.isPrivate(modifier)) {
            builder.accept(AccessLevel.PRIVATE);
        }

        if (Modifier.isStatic(modifier)) {
            builder.accept(Other.STATIC);
        }
        if (Modifier.isAbstract(modifier)) {
            builder.accept(Other.ABSTRACT);
        }
        if (Modifier.isFinal(modifier)) {
            builder.accept(Other.FINAL);
        }
        if (Modifier.isNative(modifier)) {
            builder.accept(Other.NATIVE);
        }
        if (Modifier.isSynchronized(modifier)) {
            builder.accept(Other.SYNCHRONIZED);
        }
        return builder.build();
    }

    public static enum AccessLevel implements MethodModifier {
        PRIVATE, PROTECTED, PUBLIC
    }

    public static enum Other implements MethodModifier {
        ABSTRACT, FINAL, NATIVE, STATIC, SYNCHRONIZED
    }
}
