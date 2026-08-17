package com.acuvity.pipeline.service.sync;

import com.acuvity.pipeline.models.NamespaceSyncResult;
import com.acuvity.pipeline.models.SyncSummaryResponse;
import com.acuvity.pipeline.service.namespace.NamespaceRegistry;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SyncFanOutService {

    private final PipelineSyncApi pipelineSyncApi;
    private final PipelineProperties properties;
    private final NamespaceRegistry namespaceRegistry;

    public SyncFanOutService(
            PipelineSyncApi pipelineSyncApi,
            PipelineProperties properties,
            NamespaceRegistry namespaceRegistry) {
        this.pipelineSyncApi = pipelineSyncApi;
        this.properties = properties;
        this.namespaceRegistry = namespaceRegistry;
    }

    public Mono<SyncSummaryResponse> syncAll(Instant from, Instant to) {
        var namespaces = namespaceRegistry.all();
        return Flux.fromIterable(namespaces)
                .flatMapDelayError(namespace -> pipelineSyncApi.syncLogs(namespace, from, to)
                                .map(response -> new NamespaceSyncResult(
                                        namespace, response.recordsPublished())),
                        Math.max(1, namespaces.size()), 1)
                .collectList()
                .map(results -> new SyncSummaryResponse(
                        "published",
                        properties.topics().syncedLogs(),
                        results.size(),
                        results.stream().mapToInt(NamespaceSyncResult::recordsPublished).sum(),
                        results))
                .onErrorMap(exception -> new SyncFanOutException(exception));
    }
}
