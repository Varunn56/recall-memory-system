package com.recall.service;

import com.recall.dto.ClassMetricDTO;
import com.recall.dto.ModelMetricsResponse;
import com.recall.dto.PredictionRequest;
import com.recall.dto.PredictionResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MemoryPredictionService {

    private static final Logger log = LoggerFactory.getLogger(MemoryPredictionService.class);

    @Value("${ml.dataset.path}")
    private Resource datasetResource;

    @Value("${ml.model.seed:42}")
    private int seed;

    @Value("${ml.model.num-trees:50}")
    private int numTrees;

    @Value("${ml.model.folds:5}")
    private int folds;

    private RandomForest classifier;
    private Instances dataStructure;
    private ModelMetricsResponse cachedMetrics;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Weka Random Forest Model with seed={}", seed);
            loadAndTrainModel();
            log.info("Model training and stratified cross-validation completed successfully.");
        } catch (Exception e) {
            log.error("Fatal error during model training: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Weka ML model", e);
        }
    }

    private synchronized void loadAndTrainModel() throws Exception {
        try (InputStream is = datasetResource.getInputStream()) {
            DataSource source = new DataSource(is);
            Instances data = source.getDataSet();

            if (data == null) {
                throw new IllegalStateException("Failed to read ARFF dataset from: " + datasetResource.getDescription());
            }

            data.setClassIndex(data.numAttributes() - 1);
            this.dataStructure = new Instances(data, 0);

            this.classifier = new RandomForest();
            this.classifier.setNumIterations(this.numTrees);
            this.classifier.setSeed(this.seed);

            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(this.classifier, data, this.folds, new Random(this.seed));

            this.classifier.buildClassifier(data);

            List<ClassMetricDTO> classMetrics = new ArrayList<>();
            for (int i = 0; i < data.numClasses(); i++) {
                String className = data.classAttribute().value(i);
                double precision = round(eval.precision(i), 4);
                double recallVal = round(eval.recall(i), 4);
                double f1 = round(eval.fMeasure(i), 4);
                classMetrics.add(new ClassMetricDTO(className, precision, recallVal, f1));
            }

            double[][] confMatrix = eval.confusionMatrix();
            String[] classLabels = new String[data.numClasses()];
            for (int i = 0; i < data.numClasses(); i++) {
                classLabels[i] = data.classAttribute().value(i);
            }

            this.cachedMetrics = new ModelMetricsResponse(
                    "Weka RandomForest (Iterations=" + numTrees + ", Seed=" + seed + ")",
                    data.numInstances(),
                    this.folds,
                    round(eval.pctCorrect(), 2),
                    round(eval.pctIncorrect(), 2),
                    classMetrics,
                    confMatrix,
                    classLabels,
                    "Trained on a synthetic balanced dataset using an Ebbinghaus retention proxy."
            );
        }
    }

    public PredictionResponse predict(PredictionRequest request) {
        if (classifier == null || dataStructure == null) {
            throw new IllegalStateException("Model is not initialized.");
        }

        try {
            Instance instance = new DenseInstance(dataStructure.numAttributes());
            instance.setDataset(dataStructure);

            instance.setValue(0, request.studyMinutes());
            instance.setValue(1, request.daysSinceRevision());
            instance.setValue(2, request.prevRevisions());
            instance.setValue(3, request.practiceSessions());
            instance.setValue(4, request.quizScore());

            double[] distribution = classifier.distributionForInstance(instance);

            int lowIndex = dataStructure.classAttribute().indexOfValue("LOW");
            int highIndex = dataStructure.classAttribute().indexOfValue("HIGH");

            double lowProb = round(distribution[lowIndex], 4);
            double highProb = round(distribution[highIndex], 4);

            String predictedClass = (highProb >= lowProb) ? "HIGH RECALL" : "LOW RECALL";
            double modelConfidence = round(Math.max(highProb, lowProb), 4);

            String recommendation;
            if ("HIGH RECALL".equals(predictedClass)) {
                recommendation = "Revision is not urgently needed. Memory trace is robust; maintain periodic self-testing.";
            } else {
                recommendation = "Revision is recommended. High probability of memory decay detected. Review core flashcards and solve practice questions.";
            }

            String explanation = String.format(
                    "The Random Forest model assigns a %.1f%% statistical probability to the %s class based on your inputs. " +
                    "This metric represents the statistical certainty of the model's classification, NOT the percentage of subject matter retained.",
                    modelConfidence * 100.0, predictedClass
            );

            return new PredictionResponse(
                    predictedClass,
                    highProb,
                    lowProb,
                    modelConfidence,
                    recommendation,
                    explanation
            );
        } catch (Exception e) {
            log.error("Failed to classify instance: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing model inference", e);
        }
    }

    public ModelMetricsResponse getModelMetrics() {
        return this.cachedMetrics;
    }

    private double round(double val, int decimals) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return 0.0;
        }
        return BigDecimal.valueOf(val).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }
}
