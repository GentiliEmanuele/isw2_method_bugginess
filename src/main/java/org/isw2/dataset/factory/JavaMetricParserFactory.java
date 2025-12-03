package org.isw2.dataset.factory;

import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.metrics.controller.JavaMetricParser;
import org.isw2.dataset.metrics.controller.context.ParserContext;

import java.util.List;

public class JavaMetricParserFactory extends AbstractControllerFactory<ParserContext, List<Method>> {
    @Override
    public Controller<ParserContext, List<Method>> createController() {
        return JavaMetricParser.getInstance();
    }
}
