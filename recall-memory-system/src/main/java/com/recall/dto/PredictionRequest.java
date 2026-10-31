package com.recall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PredictionRequest(
    @NotNull(message = "studyMinutes is required")
    @Min(value = 1, message = "studyMinutes must be at least 1 minute")
    @Max(value = 720, message = "studyMinutes cannot exceed 720 minutes (12 hours)")
    Integer studyMinutes,

    @NotNull(message = "daysSinceRevision is required")
    @Min(value = 0, message = "daysSinceRevision cannot be negative")
    @Max(value = 365, message = "daysSinceRevision cannot exceed 365 days")
    Integer daysSinceRevision,

    @NotNull(message = "prevRevisions is required")
    @Min(value = 0, message = "prevRevisions cannot be negative")
    @Max(value = 50, message = "prevRevisions cannot exceed 50")
    Integer prevRevisions,

    @NotNull(message = "practiceSessions is required")
    @Min(value = 0, message = "practiceSessions cannot be negative")
    @Max(value = 200, message = "practiceSessions cannot exceed 200")
    Integer practiceSessions,

    @NotNull(message = "quizScore is required")
    @Min(value = 0, message = "quizScore must be at least 0.0")
    @Max(value = 100, message = "quizScore cannot exceed 100.0")
    Double quizScore
) {}
