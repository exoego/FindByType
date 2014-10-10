package net.exoego.typefind.definition;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageDef {
    private final String name;

    private PackageDef(Package instance) {
        this.name = instance == null ? "" : instance.getName();
    }

    private PackageDef(String instance) {
        this.name = instance == null ? "" : instance;
    }

    private static final Pattern PACKAGE_NAME = Pattern.compile("^(?<package>(?:\\w+\\.)+)\\w+[^.]");

    public static PackageDef of(Type type) {
        if (type instanceof Class) {
            final Package aPackage = ((Class) type).getPackage();
            if (aPackage != null) {
                return new PackageDef(aPackage);
            }
        }
        final String typeName = type.getTypeName();
        final Matcher matcher = PACKAGE_NAME.matcher(typeName);
        if (matcher.find()) {
            final String aPackage = matcher.group("package");
            final String lastDotRemoved = aPackage.substring(0, aPackage.length() - 1);
            return new PackageDef(lastDotRemoved);
        }
        return new PackageDef("");
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
