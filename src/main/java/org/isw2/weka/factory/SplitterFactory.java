package org.isw2.weka.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.weka.utils.Splitter;
import org.isw2.weka.utils.context.SplitterContext;
import weka.core.Instances;

import java.util.List;

public class SplitterFactory extends AbstractControllerFactory<SplitterContext, List<Instances>> {
    @Override
    public Controller<SplitterContext, List<Instances>> createController() {
        return new Splitter();
    }
}
