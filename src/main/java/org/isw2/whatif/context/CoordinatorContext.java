package org.isw2.whatif.context;

import org.isw2.weka.classifier.ClassifierType;

import java.util.List;

public record CoordinatorContext(String projectName, double discardPercentage, List<ClassifierType> classifierTypes) {}
