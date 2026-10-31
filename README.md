# RECALL – Student Memory Retention & Revision Prediction System

An end-to-end Machine Learning web application built using **Java 21**, **Spring Boot 3**, and the **Weka ML Library**. The system predicts whether a student requires an immediate revision session (`HIGH RECALL` vs `LOW RECALL`) based on their study habits and historical test performance[cite: 5].

---

## Key Features

- **Embedded ML Pipeline:** Random Forest Classifier trained natively in Java using Weka (no Python runtime required)[cite: 3, 10].
- **Balanced Training Data:** 60 synthetic student retention samples structured using an Ebbinghaus retention index to eliminate class imbalance[cite: 1, 6].
- **Leakage-Free Validation:** Evaluated using Stratified 5-Fold Cross-Validation, reporting Precision, Recall, F1-Score, and Confusion Matrix per class[cite: 6, 10, 13].
- **Interactive UI Dashboard:** Responsive single-page frontend (HTML5/CSS3/Vanilla JS) connected via REST endpoints[cite: 5, 7, 8, 17].
- **Robust Input Validation:** Jakarta Bean Validation guards against out-of-range inputs[cite: 11, 16].

---

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.3.5[cite: 3]
- **ML Engine:** Weka 3.8.6 (`RandomForest`)[cite: 3, 10]
- **Frontend:** HTML5, CSS3, JavaScript (Fetch API)[cite: 5, 7, 8]
- **Build Tool:** Maven[cite: 3]

---

## Input Features

| Feature Name | Description | Accepted Range |
| :--- | :--- | :--- |
| `studyMinutes` | Total study duration in minutes | 1 to 720 minutes |
| `daysSinceRevision` | Days elapsed since the last revision session | 0 to 365 days |
| `prevRevisions` | Count of completed revision cycles | 0 to 50 |
| `practiceSessions` | Number of practice problem sets / sessions | 0 to 200 |
| `quizScore` | Previous quiz or assessment score | 0.0 to 100.0% |

---

## Project Structure

```text
recall-memory-system/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/recall/
    │   │   ├── RecallApplication.java
    │   │   ├── controller/RecallController.java
    │   │   ├── dto/
    │   │   ├── exception/
    │   │   └── service/MemoryPredictionService.java
    │   └── resources/
    │       ├── application.properties
    │       ├── data/student_retention_train.arff
    │       └── static/
    │           ├── index.html
    │           ├── css/style.css
    │           └── js/app.js
    └── test/java/com/recall/RecallApplicationTests.java
