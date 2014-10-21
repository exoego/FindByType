package net.exoego.typefind.indexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.exoego.typefind.definition.MethodDef;
import net.exoego.typefind.definition.MethodModifier;
import net.exoego.typefind.definition.TypeDef;
import net.exoego.typefind.reader.ClassStream;
import net.exoego.typefind.reader.JdkFilters;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Ignore
public class IndexerTest {
    private static final Predicate<Path> isJarFile = path -> path.toFile().getName().endsWith(".jar");
    private static final Predicate<MethodDef> isPublicMethod = method -> method.getModifiers()
                                                                               .contains(MethodModifier.AccessLevel.PUBLIC);

    @Test
    public void addJDK8() throws IOException {
        Indexer indexer = new ElasticSearchIndexer("JDK8u25_");
        final Path jreLibPath = ClassStream.getJreLibPath();
        final Stream<Class<?>> jdkClasses = publicClasses(jreLibPath).filter(JdkFilters::isPublicDocumentedJdkClass);
        final Stream<MethodDef> methods = jdkClasses.flatMap(MethodDef::allMethods).filter(isPublicMethod);
        indexer.addSource(methods);
        final long index = indexer.index();
        assertThat(index, is(4300));
    }

    private Stream<Class<?>> publicClasses(final Path jreLibPath) throws IOException {
        return Files.walk(jreLibPath).filter(isJarFile).flatMap(ClassStream::from).filter(TypeDef::isPublic);
    }

    @Test
    public void addJUnit() throws IOException {
        Indexer indexer = new ElasticSearchIndexer("JUnit4.12-beta-2");
        final Path jreLibPath = Paths.get("C:\\Users\\yt\\.m2\\repository\\junit\\junit\\4.12-beta-2");
        final Pattern legacy = Pattern.compile("^junit.framework");
        final Predicate<MethodDef> isJunit4 = method -> !legacy.matcher(method.getDeclaringClass()
                                                                              .getPackageDef()
                                                                              .getName()).find();
        final Stream<MethodDef> methods = publicClasses(jreLibPath).flatMap(MethodDef::allMethods)
                                                                   .filter(isPublicMethod)
                                                                   .filter(isJunit4);
        indexer.addSource(methods);
        final long index = indexer.index();
        assertThat(index, is(4300));
    }

    @Test
    public void addElasticsearch() throws IOException {
        Indexer indexer = new ElasticSearchIndexer("Elasticsearch1.4.0.Beta1");
        final Path path = Paths.get("C:\\Users\\yt\\.m2\\repository\\org\\elasticsearch\\elasticsearch\\1.4.0.Beta1");
        final Stream<MethodDef> methods = publicClasses(path).filter(k -> k.getName().contains("$"))
                                                             .flatMap(MethodDef::allMethods)
                                                             .filter(isPublicMethod);

        indexer.addSource(methods);
        final long index = indexer.index();
        assertThat(index, is(4300));
    }
}
