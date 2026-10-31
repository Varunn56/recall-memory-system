package com.recall.dto;

public record PredictionResponse(
    String predictedClass,
    double highProbability,
    double lowProbability,
    double modelConfidence,
    String recommendation,
    String confidenceExplanation
) {}
