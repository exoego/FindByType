package net.exoego.typefind.indexer;

import java.util.stream.Stream;

import net.exoego.typefind.definition.MethodDef;

public interface Indexer {
    long index();

    void addSource(Stream<MethodDef> from);
}
