package com.acuvity.pipeline.models;

import java.util.List;

public record SyncSummaryResponse(
        String status,
        String topic,
        int namespacesProcessed,
        int recordsPublished,
        List<NamespaceSyncResult> namespaces) {
}
