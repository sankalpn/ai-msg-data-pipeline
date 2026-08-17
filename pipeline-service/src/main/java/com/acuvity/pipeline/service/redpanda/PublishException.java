package com.acuvity.pipeline.service.redpanda;

public class PublishException extends RuntimeException {
    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
