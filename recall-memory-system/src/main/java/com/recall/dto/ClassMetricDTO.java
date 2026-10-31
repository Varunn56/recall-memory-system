package com.recall.dto;

public record ClassMetricDTO(
    String className,
    double precision,
    double recall,
    double f1Score
) {}
