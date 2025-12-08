package org.isw2.weka.tuning;

import org.isw2.dataset.exceptions.ProcessingException;
import weka.classifiers.Classifier;
import weka.core.Instances;

public interface Tuner {
    Classifier tune(Classifier classifier, Instances training, double splittingPercentage) throws ProcessingException;
}
