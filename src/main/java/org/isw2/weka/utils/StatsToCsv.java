package org.isw2.weka.utils;

import com.opencsv.CSVWriter;
import org.isw2.weka.model.Statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class StatsToCsv {
    private static final String [] HEADER = {
            "runId",
            "Recall",
            "Precision",
            "F1Score",
            "areaUnderROC",
    };

    public static void writeStatsToCsv(String projectName, Map<Integer, Statistics> statsByRun) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("output/weka_" + projectName + ".csv"))) {
            writer.writeNext(HEADER);
            statsByRun.forEach((runId, statistics) -> {
                String[] row = {
                        runId.toString(),
                        String.valueOf(statistics.recall()),
                        String.valueOf(statistics.precision()),
                        String.valueOf(statistics.f1Score()),
                        String.valueOf(statistics.areaUnderROC()),
                };
                writer.writeNext(row);
            });
        }
    }
}
