package com.recall.controller;

import com.recall.dto.ModelMetricsResponse;
import com.recall.dto.PredictionRequest;
import com.recall.dto.PredictionResponse;
import com.recall.service.MemoryPredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class RecallController {

    private final MemoryPredictionService predictionService;

    public RecallController(MemoryPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RECALL - Student Memory Prediction System",
                "version", "1.0.0"
        ));
    }

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@Valid @RequestBody PredictionRequest request) {
        PredictionResponse response = predictionService.predict(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/model/metrics")
    public ResponseEntity<ModelMetricsResponse> getMetrics() {
        return ResponseEntity.ok(predictionService.getModelMetrics());
    }
}
