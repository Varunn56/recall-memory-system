package com.recall.dto;

import java.util.List;

public record ModelMetricsResponse(
    String algorithm,
    int totalInstances,
    int crossValidationFolds,
    double overallAccuracy,
    double errorRate,
    List<ClassMetricDTO> classMetrics,
    double[][] confusionMatrix,
    String[] classLabels,
    String datasetNotice
) {}
