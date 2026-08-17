package com.acuvity.log.models;

import java.util.List;
import java.util.Map;

public record Extraction(
        Map<String, Double> secrets,
        String data,
        List<Detection> detections,
        boolean isFile,
        String kind,
        List<Modality> modalities,
        String role) {
}
