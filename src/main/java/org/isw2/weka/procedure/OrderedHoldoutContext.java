package org.isw2.weka.procedure;

import org.isw2.weka.tuning.Tuner;
import weka.classifiers.Classifier;
import weka.core.Instances;

public record OrderedHoldoutContext(Instances trainData, double splittingPercentage, Classifier classifier, Instances testData, Tuner tuner) {
}
