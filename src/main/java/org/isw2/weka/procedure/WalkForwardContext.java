package org.isw2.weka.procedure;

import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.List;

public record WalkForwardContext(List<Instances> dataByVersion, double trainingPercentage, Classifier classifier) {
}
