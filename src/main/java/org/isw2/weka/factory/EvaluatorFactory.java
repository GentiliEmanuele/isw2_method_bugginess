package org.isw2.weka.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.utils.Evaluator;
import org.isw2.weka.utils.context.EvaluatorContext;

public class EvaluatorFactory extends AbstractControllerFactory<EvaluatorContext, Statistics> {
    @Override
    public Controller<EvaluatorContext, Statistics> createController() {
        return new Evaluator();
    }
}
