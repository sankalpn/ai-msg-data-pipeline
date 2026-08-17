package com.acuvity.pipeline.service.sync;

public class SyncFanOutException extends RuntimeException {
    public SyncFanOutException(Throwable cause) {
        super("At least one namespace sync failed", cause);
    }
}
