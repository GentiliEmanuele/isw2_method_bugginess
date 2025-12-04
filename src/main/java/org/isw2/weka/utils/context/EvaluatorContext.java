package org.isw2.weka.utils.context;

import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

public record EvaluatorContext(CostSensitiveClassifier classifier, Instances validation) {
}
