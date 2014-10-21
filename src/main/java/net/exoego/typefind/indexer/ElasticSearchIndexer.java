package net.exoego.typefind.indexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import net.exoego.typefind.definition.MethodDef;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ElasticSearchIndexer implements Indexer {
    private static final int BUFFER_SIZE = 1000;
    private final List<Stream<MethodDef>> sources = new ArrayList<>();
    private final String artifactName;

    public ElasticSearchIndexer(final String artifactName) {
        Objects.requireNonNull(artifactName);
        this.artifactName = artifactName;
    }

    @Override
    public long index() {
        final InetSocketTransportAddress localhost = new InetSocketTransportAddress("localhost", 9300);
        long successCount = 0;
        try (final Client client = new TransportClient().addTransportAddress(localhost);) {
            final Gson gson = new Gson();
            final Stream<MethodDef> flatten = sources.stream().flatMap(Function.identity());
            final Iterator<MethodDef> iterator = flatten.iterator();
            final List<MethodDef> buffer = new ArrayList<>(BUFFER_SIZE);
            while (iterator.hasNext()) {
                buffer.add(iterator.next());
                if (buffer.size() == BUFFER_SIZE) {
                    successCount += request(client, gson, buffer);
                    buffer.clear();
                }
            }
            successCount += request(client, gson, buffer);
        }
        return successCount;
    }

    @Override
    public void addSource(final Stream<MethodDef> source) {
        Objects.requireNonNull(source);
        sources.add(source);
    }

    private long request(final Client client, final Gson gson, final List<MethodDef> buffer) {
        long successCount = 0;
        final BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (final MethodDef methodDef : buffer) {
            final String json = gson.toJson(methodDef);
            final IndexRequestBuilder indexBuilder = client.prepareIndex("typefind", artifactName);
            bulkRequest.add(indexBuilder.setSource(json));
        }
        final BulkResponse response = bulkRequest.execute().actionGet();
        if (response.hasFailures()) {
            successCount += buffer.size() -
                            StreamSupport.stream(response.spliterator(), false).filter(res -> res.isFailed()).count();
        } else {
            successCount += buffer.size();
        }
        System.out.printf("source:%s,  success:%s  %n", buffer.size(), successCount);
        return successCount;
    }
}
