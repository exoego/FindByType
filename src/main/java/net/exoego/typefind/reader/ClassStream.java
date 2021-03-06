package net.exoego.typefind.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import net.exoego.util.MoreCollectors;

/**
 * Stream of {@code Class} that is loaded from {@code JarFile} and alike.
 */
public class ClassStream implements Stream<Class<?>> {
    private static final Path JRE_LIB;

    static {
        final String value = System.getProperty("sun.boot.class.path");
        if (value == null) {
            throw new IllegalStateException("JRE lib directory not found");
        }
        final String jar = value.split(System.getProperty("path.separator"))[0];
        final String fileSeparator = System.getProperty("file.separator");
        // Fxxk windows
        final String escapedFileSeparator = fileSeparator.equals("\\") ? "\\\\" : fileSeparator;
        // remove last path such as "/rt.jar"
        String lib = jar.replaceFirst(escapedFileSeparator + "[^" + escapedFileSeparator + "]+$", "");
        if (lib == null || lib.isEmpty()) {
            throw new IllegalStateException("failed to get JRE lib path");
        }
        JRE_LIB = Paths.get(lib);
    }

    private static final boolean CLASS_INITIALIZATION_NOT_REQUIRED = false;
    private final Stream<Class<?>> source;

    private ClassStream(final Stream<Class<?>> source) {
        this.source = source;
    }

    private static ClassStream newInstance(final Stream<Class<?>> source) {
        return new ClassStream(source);
    }

    /**
     * Shortcut to <code>ClassStream.from(path.toFile())</code>.
     *
     * @param path the path represents a file as sources of {@code Class}es.
     * @return {@code ClassStream} instance to load {@code Class}es from the given {@code JarFile}.
     */
    public static ClassStream from(final Path path) {
        return from(path.toFile());
    }

    /**
     * Shortcut to <code>ClassStream.from(new JarFile(file))</code>.
     *
     * @param file the file as sources of {@code Class}es.
     * @return {@code ClassStream} instance to load {@code Class}es from the given {@code JarFile}.
     */
    public static ClassStream from(final File file) {
        try {
            return from(new JarFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getJreLibPath() {
        return JRE_LIB;
    }

    /**
     * Create an instance from {@code JarFile} instance.
     * The returned instance closes the given {@code JarFile} on {@link java.util.stream.Stream#close()}.
     *
     * @param jar the jar file as sources of {@code Class}es.
     * @return {@code ClassStream} instance to load {@code Class}es from the given {@code JarFile}.
     */
    public static ClassStream from(final JarFile jar) {
        final Supplier<Stream<Class<?>>> lazyLoad = () -> jar.stream()
                                                             .map(ClassStream::jarEntryAsClass)
                                                             .flatMap(MoreCollectors::optionalToStream)
                                                             .onClose(() -> {
                                                                 try {
                                                                     jar.close();
                                                                 } catch (IOException e) {
                                                                     throw new RuntimeException(e);
                                                                 }
                                                             });
        final Stream<Class<?>> generateOnceAndFlat = Stream.generate(lazyLoad)
                                                           .limit(1)
                                                           .flatMap(Function.identity());
        return new ClassStream(generateOnceAndFlat);
    }

    private static Optional<Class<?>> jarEntryAsClass(final JarEntry entry) {
        if (entry.getName().endsWith(".class")) {
            try {
                final Class<?> aClass = Class.forName(pathToCanonicalName(entry),
                                                      CLASS_INITIALIZATION_NOT_REQUIRED,
                                                      ClassLoader.getSystemClassLoader());
                return Optional.of(aClass);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // do nothing
            }
        }
        return Optional.empty();
    }

    private static String pathToCanonicalName(final JarEntry entry) {
        final String directoryPath = entry.getName();
        // path/to/Hoge.class -> path.to.Hoge
        return directoryPath.substring(0, directoryPath.length() - 6)
                            .replace('/', '.');
    }

    @Override
    public ClassStream filter(final Predicate<? super Class<?>> predicate) {
        return ClassStream.newInstance(source.filter(predicate));
    }

    @Override
    public <R> Stream<R> map(final Function<? super Class<?>, ? extends R> mapper) {
        return source.map(mapper);
    }

    @Override
    public IntStream mapToInt(final ToIntFunction<? super Class<?>> mapper) {
        return source.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(final ToLongFunction<? super Class<?>> mapper) {
        return source.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(final ToDoubleFunction<? super Class<?>> mapper) {
        return source.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(final Function<? super Class<?>, ? extends Stream<? extends R>> mapper) {
        return source.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(final Function<? super Class<?>, ? extends IntStream> mapper) {
        return source.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(final Function<? super Class<?>, ? extends LongStream> mapper) {
        return source.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(final Function<? super Class<?>, ? extends DoubleStream> mapper) {
        return source.flatMapToDouble(mapper);
    }

    @Override
    public ClassStream distinct() {
        return newInstance(source.distinct());
    }

    @Override
    public ClassStream sorted() {
        return newInstance(source.sorted());
    }

    @Override
    public ClassStream sorted(final Comparator<? super Class<?>> comparator) {
        return newInstance(source.sorted(comparator));
    }

    @Override
    public ClassStream peek(final Consumer<? super Class<?>> action) {
        return newInstance(source.peek(action));
    }

    @Override
    public ClassStream limit(final long maxSize) {
        return newInstance(source.limit(maxSize));
    }

    @Override
    public ClassStream skip(final long n) {
        return newInstance(source.skip(n));
    }

    @Override
    public void forEach(final Consumer<? super Class<?>> action) {
        source.forEach(action);
    }

    @Override
    public void forEachOrdered(final Consumer<? super Class<?>> action) {
        source.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return source.toArray();
    }

    @Override
    public <A> A[] toArray(final IntFunction<A[]> generator) {
        return source.toArray(generator);
    }

    @Override
    public Class<?> reduce(final Class<?> identity, final BinaryOperator<Class<?>> accumulator) {
        return source.reduce(identity, accumulator);
    }

    @Override
    public Optional<Class<?>> reduce(final BinaryOperator<Class<?>> accumulator) {
        return source.reduce(accumulator);
    }

    @Override
    public <U> U reduce(
            final U identity,
            final BiFunction<U, ? super Class<?>, U> accumulator,
            final BinaryOperator<U> combiner) {
        return source.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(
            final Supplier<R> supplier,
            final BiConsumer<R, ? super Class<?>> accumulator,
            final BiConsumer<R, R> combiner) {
        return source.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(final Collector<? super Class<?>, A, R> collector) {
        return source.collect(collector);
    }

    @Override
    public Optional<Class<?>> min(final Comparator<? super Class<?>> comparator) {
        return source.min(comparator);
    }

    @Override
    public Optional<Class<?>> max(final Comparator<? super Class<?>> comparator) {
        return source.max(comparator);
    }

    @Override
    public long count() {
        return source.count();
    }

    @Override
    public boolean anyMatch(final Predicate<? super Class<?>> predicate) {
        return source.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(final Predicate<? super Class<?>> predicate) {
        return source.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(final Predicate<? super Class<?>> predicate) {
        return source.noneMatch(predicate);
    }

    @Override
    public Optional<Class<?>> findFirst() {
        return source.findFirst();
    }

    @Override
    public Optional<Class<?>> findAny() {
        return source.findAny();
    }

    @Override
    public Iterator<Class<?>> iterator() {
        return source.iterator();
    }

    @Override
    public Spliterator<Class<?>> spliterator() {
        return source.spliterator();
    }

    @Override
    public boolean isParallel() {
        return source.isParallel();
    }

    @Override
    public ClassStream sequential() {
        return newInstance(source.sequential());
    }

    @Override
    public ClassStream parallel() {
        return newInstance(source.parallel());
    }

    @Override
    public ClassStream unordered() {
        return newInstance(source.unordered());
    }

    @Override
    public ClassStream onClose(final Runnable closeHandler) {
        return newInstance(source.onClose(closeHandler));
    }

    @Override
    public void close() {
        source.close();
    }
}