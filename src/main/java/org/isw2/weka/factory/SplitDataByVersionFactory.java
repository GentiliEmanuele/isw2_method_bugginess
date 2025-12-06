package org.isw2.weka.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.weka.utils.SplitDataByVersion;
import org.isw2.weka.utils.context.SplitDataByVersionContext;
import weka.core.Instances;

import java.util.List;

public class SplitDataByVersionFactory extends AbstractControllerFactory<SplitDataByVersionContext, List<Instances>> {
    @Override
    public Controller<SplitDataByVersionContext, List<Instances>> createController() {
        return new SplitDataByVersion();
    }
}
