package net.exoego.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoreCollectors {
    public static <T> Collector<T, ?, List<T>> toImmutableList() {
        return Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList);
    }

    public static <T> Collector<T, ?, Set<T>> toImmutableSet() {
        return Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet);
    }

    public static <T> Stream<T> optionalToStream(Optional<? extends T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }
}
