package org.isw2.weka.utils;

import com.opencsv.CSVWriter;
import org.isw2.weka.classifier.ClassifierType;
import org.isw2.weka.model.Statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class StatsToCsv {
    private static final String [] HEADER = {
            "Classifier",
            "runId",
            "Recall",
            "Precision",
            "F1Score",
            "areaUnderROC",
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
                            String.valueOf(statistics.areaUnderROC())
                    };
                    writer.writeNext(row);
                })
            );
        }
    }
}
