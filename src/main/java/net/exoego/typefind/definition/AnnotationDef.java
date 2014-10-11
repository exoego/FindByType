package net.exoego.typefind.definition;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA.
 */
public class AnnotationDef {
    private final String name;

    public AnnotationDef(final Annotation name) {
        this(name.toString());
    }

    private AnnotationDef(final String name) {
        this.name = name;
    }
}
