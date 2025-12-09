package org.isw2.weka.procedure;

import org.isw2.weka.classifier.ClassifierType;
import org.isw2.weka.tuning.Tuner;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.List;

public record WalkForwardContext(List<Instances> dataByVersion, double trainingPercentage, ClassifierType classifierType, Tuner tuner) {
}
