package org.isw2.weka.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.weka.utils.CostSensitiveClassifierBuilder;
import org.isw2.weka.utils.context.CostSensitiveClassifierBuilderContext;
import weka.classifiers.meta.CostSensitiveClassifier;

public class CostSensitiveClassifierBuilderFactory extends AbstractControllerFactory<CostSensitiveClassifierBuilderContext, CostSensitiveClassifier> {
    @Override
    public Controller<CostSensitiveClassifierBuilderContext, CostSensitiveClassifier> createController() {
        return new CostSensitiveClassifierBuilder();
    }
}
