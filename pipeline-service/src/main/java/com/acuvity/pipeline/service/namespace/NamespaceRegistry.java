package com.acuvity.pipeline.service.namespace;

import com.acuvity.pipeline.api.NamespaceNotEnabledException;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NamespaceRegistry {

    private final List<String> namespaces;
    private final Set<String> namespaceSet;

    public NamespaceRegistry(PipelineProperties properties) {
        this.namespaces = properties.enabledNamespaces();
        this.namespaceSet = Set.copyOf(namespaces);
    }

    public List<String> all() {
        return namespaces;
    }

    public void requireEnabled(String namespace) {
        if (!namespaceSet.contains(namespace)) {
            throw new NamespaceNotEnabledException(namespace);
        }
    }
}
