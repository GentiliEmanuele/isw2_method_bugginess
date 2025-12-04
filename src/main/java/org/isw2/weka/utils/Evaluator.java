package org.isw2.weka.utils;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.utils.context.EvaluatorContext;

public class Evaluator implements Controller<EvaluatorContext, Statistics> {
    @Override
    public Statistics execute(EvaluatorContext input) throws ProcessingException {
        return null;
    }
}
