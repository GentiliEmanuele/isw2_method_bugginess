package org.isw2.weka.utils.context;

import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;

public record CostSensitiveClassifierBuilderContext(CostMatrix costMatrix, Classifier classifier) {
}
