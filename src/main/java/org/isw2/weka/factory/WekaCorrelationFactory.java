package org.isw2.weka.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.weka.WekaCorrelation;
import org.isw2.weka.model.Correlation;

import java.util.List;

public class WekaCorrelationFactory extends AbstractControllerFactory<String, List<Correlation>> {
    @Override
    public Controller<String, List<Correlation>> createController() {
        return new WekaCorrelation();
    }
}
