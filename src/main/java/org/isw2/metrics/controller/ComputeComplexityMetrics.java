package org.isw2.metrics.controller;

import org.isw2.core.model.FileClass;
import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ControllerFactory;
import org.isw2.factory.ControllerType;
import org.isw2.factory.ExecutionContext;
import org.isw2.jira.model.Version;
import org.isw2.metrics.controller.context.ComputeMetricsContext;
import org.isw2.metrics.controller.context.JavaMetricParserContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeComplexityMetrics implements Controller {

    private final Map<Version, List<Method>> methodsByVersion;

    public ComputeComplexityMetrics() {
        this.methodsByVersion = new HashMap<>();
    }

    public Map<Version, List<Method>> getMethodsByVersion() {
        return methodsByVersion;
    }

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof ComputeMetricsContext(Map<Version, List<FileClass>> fileClassByVersion))) {
            throw new ProcessingException("Context is not a ComputeMetricsContext");
        }

        Controller controller = ControllerFactory.createController(ControllerType.JAVA_METRIC_PARSER);

        fileClassByVersion.forEach((version, fileClasses) -> {
            List<Method> methods = new ArrayList<>();
            fileClasses.forEach(fileClass -> {
                methods.addAll(analyzeFile(fileClass, controller));
                fileClass.setMethods(methods);
            });
            methodsByVersion.put(version, methods);
        });
    }

    private List<Method> analyzeFile(FileClass fileClass, Controller controller)  {
        try {
            controller.execute(new JavaMetricParserContext(fileClass.getContent(), fileClass.getPath()));
            if (controller instanceof JavaMetricParser parser) {
                List<Method> methods = new ArrayList<>(parser.getMethods());
                parser.cleanMethodsList();
                return methods;
            }
            return new ArrayList<>();
        } catch (ProcessingException _) {
            return new ArrayList<>();
        }
    }
}
