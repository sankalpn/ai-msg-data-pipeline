package com.acuvity.pipeline.api;

public class NamespaceNotEnabledException extends RuntimeException {
    public NamespaceNotEnabledException(String namespace) {
        super("Namespace is not enabled: " + namespace);
    }
}
