package org.isw2.factory;

import org.isw2.core.model.Method;
import org.isw2.metrics.controller.JavaMetricParser;
import org.isw2.metrics.controller.context.ParserContext;

import java.util.List;

public class JavaMetricParserFactory extends AbstractControllerFactory<ParserContext, List<Method>> {
    @Override
    public Controller<ParserContext, List<Method>> createController() {
        return JavaMetricParser.getInstance();
    }
}
