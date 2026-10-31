document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("predictionForm");
    const predictBtn = document.getElementById("predictBtn");
    const formError = document.getElementById("formError");
    
    const resultPlaceholder = document.getElementById("resultPlaceholder");
    const resultContent = document.getElementById("resultContent");
    const badgeContainer = document.getElementById("badgeContainer");
    const recommendationText = document.getElementById("recommendationText");
    const highProbText = document.getElementById("highProbText");
    const highProbBar = document.getElementById("highProbBar");
    const lowProbText = document.getElementById("lowProbText");
    const lowProbBar = document.getElementById("lowProbBar");
    const confidenceText = document.getElementById("confidenceText");
    const explanationText = document.getElementById("explanationText");
    const refreshMetricsBtn = document.getElementById("refreshMetricsBtn");

    loadModelMetrics();

    refreshMetricsBtn.addEventListener("click", () => {
        loadModelMetrics();
    });

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        hideError();

        const payload = {
            studyMinutes: parseInt(document.getElementById("studyMinutes").value, 10),
            daysSinceRevision: parseInt(document.getElementById("daysSinceRevision").value, 10),
            prevRevisions: parseInt(document.getElementById("prevRevisions").value, 10),
            practiceSessions: parseInt(document.getElementById("practiceSessions").value, 10),
            quizScore: parseFloat(document.getElementById("quizScore").value)
        };

        predictBtn.disabled = true;
        predictBtn.textContent = "Classifying...";

        try {
            const response = await fetch("/api/v1/predict", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errData = await response.json();
                const errMsg = errData.validationErrors ? errData.validationErrors.join("<br>") : errData.error;
                showError(errMsg || "An error occurred during prediction.");
                return;
            }

            const data = await response.json();
            renderPredictionResult(data);
        } catch (err) {
            showError("Network error: Could not reach Spring Boot backend.");
        } finally {
            predictBtn.disabled = false;
            predictBtn.textContent = "Predict Revision Need";
        }
    });

    function renderPredictionResult(data) {
        resultPlaceholder.classList.add("hidden");
        resultContent.classList.remove("hidden");

        const isHigh = data.predictedClass === "HIGH RECALL";
        badgeContainer.className = "badge " + (isHigh ? "badge-high" : "badge-low");
        badgeContainer.textContent = data.predictedClass;

        recommendationText.textContent = data.recommendation;

        const highPct = (data.highProbability * 100).toFixed(1);
        const lowPct = (data.lowProbability * 100).toFixed(1);

        highProbText.textContent = `${highPct}%`;
        highProbBar.style.width = `${highPct}%`;

        lowProbText.textContent = `${lowPct}%`;
        lowProbBar.style.width = `${lowPct}%`;

        confidenceText.textContent = `${(data.modelConfidence * 100).toFixed(1)}% statistical classification probability`;
        explanationText.textContent = data.confidenceExplanation;
    }

    async function loadModelMetrics() {
        try {
            const response = await fetch("/api/v1/model/metrics");
            if (!response.ok) return;
            const metrics = await response.json();

            document.getElementById("datasetNotice").textContent = metrics.datasetNotice;
            document.getElementById("metricAccuracy").textContent = `${metrics.overallAccuracy}%`;
            document.getElementById("metricError").textContent = `${metrics.errorRate}%`;
            document.getElementById("metricValidation").textContent = `${metrics.crossValidationFolds}-Fold Stratified CV`;

            const tbody = document.querySelector("#classMetricsTable tbody");
            tbody.innerHTML = "";
            metrics.classMetrics.forEach(cm => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td><strong>${escapeHtml(cm.className)}</strong></td>
                    <td>${(cm.precision * 100).toFixed(1)}%</td>
                    <td>${(cm.recall * 100).toFixed(1)}%</td>
                    <td>${(cm.f1Score * 100).toFixed(1)}%</td>
                `;
                tbody.appendChild(row);
            });

            renderConfusionMatrix(metrics.classLabels, metrics.confusionMatrix);
        } catch (e) {
            console.error("Failed to load model metrics", e);
        }
    }

    function renderConfusionMatrix(labels, matrix) {
        const container = document.getElementById("confusionMatrixContainer");
        let html = '<table class="data-table"><thead><tr><th>Actual \ Predicted</th>';
        labels.forEach(lbl => {
            html += `<th>Pred ${escapeHtml(lbl)}</th>`;
        });
        html += '</tr></thead><tbody>';

        for (let i = 0; i < labels.length; i++) {
            html += `<tr><td><strong>Actual ${escapeHtml(labels[i])}</strong></td>`;
            for (let j = 0; j < labels.length; j++) {
                html += `<td>${Math.round(matrix[i][j])}</td>`;
            }
            html += '</tr>';
        }
        html += '</tbody></table>';
        container.innerHTML = html;
    }

    function showError(msg) {
        formError.innerHTML = msg;
        formError.classList.remove("hidden");
    }

    function hideError() {
        formError.innerHTML = "";
        formError.classList.add("hidden");
    }

    function escapeHtml(str) {
        return str.replace(/[&<>'"]/g, 
            tag => ({
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                "'": '&#39;',
                '"': '&quot;'
            }[tag] || tag)
        );
    }
});
