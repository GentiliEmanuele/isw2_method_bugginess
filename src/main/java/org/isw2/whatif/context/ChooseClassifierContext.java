package org.isw2.whatif.context;

import org.isw2.weka.classifier.ClassifierType;
import weka.core.Instances;

import java.util.List;

public record ChooseClassifierContext(String projectName, List<ClassifierType> classifiers, Instances dataset) {
}
