package com.acuvity.pipeline.models;

public record AcceptedResponse(String status, String topic, int recordsPublished) {

    public static AcceptedResponse published(String topic, int count) {
        return new AcceptedResponse("published", topic, count);
    }
}
