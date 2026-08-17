package com.acuvity.pipeline.service;

import com.acuvity.alert.models.Alert;
import com.acuvity.log.models.LogRecord;
import com.acuvity.log.models.LogRecords;
import com.acuvity.pipeline.service.log.LogServiceApi;
import com.acuvity.pipeline.service.model.EnrichedAlert;
import com.acuvity.pipeline.service.redpanda.PipelinePublisher;
import com.acuvity.pipeline.service.namespace.NamespaceRegistry;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PipelineService {

    private final LogServiceApi logServiceApi;
    private final PipelinePublisher publisher;
    private final PipelineProperties properties;
    private final NamespaceRegistry namespaceRegistry;
    private final MeterRegistry meterRegistry;

    public PipelineService(
            LogServiceApi logServiceApi,
            PipelinePublisher publisher,
            PipelineProperties properties,
            NamespaceRegistry namespaceRegistry,
            MeterRegistry meterRegistry) {
        this.logServiceApi = logServiceApi;
        this.publisher = publisher;
        this.properties = properties;
        this.namespaceRegistry = namespaceRegistry;
        this.meterRegistry = meterRegistry;
    }

    public Mono<Integer> admit(Alert alert) {
        return Mono.defer(() -> {
            namespaceRegistry.requireEnabled(alert.namespace());
            Timer.Sample sample = Timer.start(meterRegistry);
            Instant from = alert.start().minus(properties.logService().correlationTolerance());
            Instant to = alert.end().plus(properties.logService().correlationTolerance());

            return Flux.fromIterable(alert.alertEvents())
                    .distinct()
                    .flatMap(event -> logServiceApi.findLogs(
                                    alert.namespace(), event.provider(), event.principal().user().name(),
                                    event.alertDefinition(), from, to)
                            .defaultIfEmpty(new LogRecords(null)))
                    .flatMapIterable(LogRecords::logs)
                    .collect(LinkedHashSet<LogRecord>::new, LinkedHashSet::add)
                    .flatMap(correlatedLogs -> publisher.publish(
                                    properties.topics().enrichedAlerts(), alert.namespace(),
                                    new EnrichedAlert(alert, correlatedLogs.stream().toList()))
                            .thenReturn(correlatedLogs.size()))
                    .doFinally(signal -> sample.stop(meterRegistry.timer(
                            "pipeline.alert.admit.duration")));
        });
    }

    public Mono<Integer> syncLogs(String namespace, Instant from, Instant to) {
        return Mono.defer(() -> {
            namespaceRegistry.requireEnabled(namespace);
            Timer.Sample sample = Timer.start(meterRegistry);
            return logServiceApi.findLogs(namespace, null, null, null, from, to)
                    .defaultIfEmpty(new LogRecords(null))
                    .flatMapMany(response -> Flux.fromIterable(response.logs()))
                    .concatMap(log -> publisher.publish(properties.topics().syncedLogs(), namespace, log)
                            .thenReturn(log))
                    .count()
                    .map(Math::toIntExact)
                    .doOnNext(count -> meterRegistry.counter(
                                    "pipeline.logs.exported", "namespace", namespace)
                            .increment(count))
                    .doFinally(signal -> sample.stop(meterRegistry.timer(
                            "pipeline.logs.export.duration", "namespace", namespace)));
        });
    }
}
