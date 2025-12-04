package org.isw2.weka.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.procedure.OrderedHoldout;
import org.isw2.weka.procedure.OrderedHoldoutContext;

public class OrderedHoldoutFactory extends AbstractControllerFactory<OrderedHoldoutContext, Statistics> {
    @Override
    public Controller<OrderedHoldoutContext, Statistics> createController() {
        return new OrderedHoldout();
    }
}
