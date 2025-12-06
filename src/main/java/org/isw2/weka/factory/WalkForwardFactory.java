package org.isw2.weka.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.procedure.OrderedHoldoutContext;
import org.isw2.weka.procedure.WalkForward;
import org.isw2.weka.procedure.WalkForwardContext;

import java.util.Map;

public class WalkForwardFactory extends AbstractControllerFactory<WalkForwardContext, Map<Integer, Statistics>> {
    @Override
    public Controller<WalkForwardContext, Map<Integer, Statistics>> createController() {
        return new WalkForward();
    }
}
