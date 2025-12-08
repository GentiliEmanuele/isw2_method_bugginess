package org.isw2.weka.tuning;

import org.isw2.dataset.exceptions.ProcessingException;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class DummyTuner implements  Tuner {
    @Override
    public Classifier tune(Classifier classifier, Instances training, double splittingPercentage) throws ProcessingException {
        return classifier;
    }
}
