package org.isw2.weka.utils;

import com.opencsv.CSVWriter;
import org.isw2.weka.classifier.ClassifierType;
import org.isw2.weka.model.Correlation;
import org.isw2.weka.model.Statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StatsToCsv {
    private static final String [] HEADER = {
            "Classifier",
            "runId",
            "Recall",
            "Precision",
            "F1Score",
            "areaUnderROC",
            "kappa"
    };

    private static final String [] CORRELATION_HEADER = {
            "Attribute",
            "Correlation-value"
    };

    private StatsToCsv() {}

    public static void writeStatsToCsv(String projectName, Map<ClassifierType, Map<Integer, Statistics>> statsByClassifier) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("output/weka_" + projectName + ".csv"))) {
            writer.writeNext(HEADER);
            statsByClassifier.forEach((classifierType, statsByRun) ->
                statsByRun.forEach((runId, statistics) -> {
                    String[] row = {
                            classifierType.toString(),
                            runId.toString(),
                            String.valueOf(statistics.recall()),
                            String.valueOf(statistics.precision()),
                            String.valueOf(statistics.f1Score()),
                            String.valueOf(statistics.areaUnderROC()),
                            String.valueOf(statistics.kappa())
                    };
                    writer.writeNext(row);
                })
            );
        }
    }

    public static void writeCorrelationToCsv(String projectName, List<Correlation> correlations) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("output/weka_correlation" + projectName + ".csv"))) {
            writer.writeNext(CORRELATION_HEADER);
            correlations.forEach(correlation -> {
                String[] row = {
                        correlation.attributeName(),
                        String.valueOf(correlation.correlation())
                };
                writer.writeNext(row);
            });
        }
    }
}
