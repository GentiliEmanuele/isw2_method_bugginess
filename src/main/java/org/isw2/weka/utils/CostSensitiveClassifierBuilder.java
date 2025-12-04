package org.isw2.weka.utils;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.utils.context.CostSensitiveClassifierBuilderContext;
import weka.classifiers.meta.CostSensitiveClassifier;

public class CostSensitiveClassifierBuilder implements Controller<CostSensitiveClassifierBuilderContext, CostSensitiveClassifier> {
    @Override
    public CostSensitiveClassifier execute(CostSensitiveClassifierBuilderContext context) throws ProcessingException {
        CostSensitiveClassifier csc = new CostSensitiveClassifier();
        csc.setClassifier(context.classifier());
        csc.setCostMatrix(context.costMatrix());
        csc.setMinimizeExpectedCost(true);
        return csc;
    }
}
