package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.metrics.controller.JavaMetricParser;
import org.isw2.dataset.metrics.controller.context.ParserContext;

import java.util.Map;

public class JavaMetricParserFactory extends AbstractControllerFactory<ParserContext, Map<MethodKey, Method>> {
    @Override
    public Controller<ParserContext, Map<MethodKey, Method>> createController() {
        return JavaMetricParser.getInstance();
    }
}
