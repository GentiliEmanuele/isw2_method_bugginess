package org.isw2.whatif.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.whatif.ChooseBClassifier;
import org.isw2.whatif.context.ChooseClassifierContext;
import weka.classifiers.Classifier;


public class ChooseClassifierFactory extends AbstractControllerFactory<ChooseClassifierContext, Classifier> {
    @Override
    public Controller<ChooseClassifierContext, Classifier> createController() {
        return new ChooseBClassifier();
    }
}
