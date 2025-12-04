package org.isw2.weka.model;

public record Statistics(double precision, double recall, double kappa, double areaUnderROC, String confusionMatrix) {
}
