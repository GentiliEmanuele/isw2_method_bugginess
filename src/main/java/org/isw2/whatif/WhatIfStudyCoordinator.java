package org.isw2.whatif;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.factory.JavaMetricParserFactory;
import org.isw2.dataset.factory.LinkMethodAndSmellFactory;
import org.isw2.dataset.factory.PmdFileAnalyzerFactory;
import org.isw2.dataset.factory.PmdFileCollectorFactory;
import org.isw2.dataset.jira.model.Version;
import org.isw2.dataset.metrics.controller.context.LinkMethodAndSmellContext;
import org.isw2.dataset.metrics.controller.context.ParserContext;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WhatIfStudyCoordinator implements Controller<CoordinatorContext, Map<Version, Map<MethodKey, Method>>> {
    @Override
    public Map<Version, Map<MethodKey, Method>> execute(CoordinatorContext context) throws ProcessingException {
        String noRefactoredCodePath = String.format("/refactoring/%s/before.java", context.projectName());
        String refactoredCodePath = String.format("/refactoring/%s/after.java", context.projectName());
        String noRefactoredCode = LoadClassCode.codeToString(noRefactoredCodePath);
        String refactoredCode = LoadClassCode.codeToString(refactoredCodePath);

        // Delete the first / to be compliant with parser keys
        noRefactoredCodePath = noRefactoredCodePath.replaceFirst("/", "");
        refactoredCodePath = refactoredCodePath.replaceFirst("/", "");

        // Compute the metrics before the refactoring activity
        AbstractControllerFactory<ParserContext, Map<MethodKey, Method>> parserFactory = new JavaMetricParserFactory();
        Map<MethodKey, Method> noRefactoredMethodMetrics = parserFactory.process(new ParserContext(noRefactoredCode, noRefactoredCodePath));

        // Compute the metrics after the refactoring activity
        Map<MethodKey, Method> refactoredMethodMetrics = parserFactory.process(new ParserContext(refactoredCode, refactoredCodePath));

        // Create two dummy version for the pmd analysis key
        Version before = new Version();
        before.setName("before");
        Version after = new Version();
        after.setName("after");

        // Adding the method for Pmd analysis
        AbstractControllerFactory<PmdFileCollectorContext, Void> collectorFactory = new PmdFileCollectorFactory();
        collectorFactory.process(new PmdFileCollectorContext(before, noRefactoredCodePath, noRefactoredCode));
        collectorFactory.process(new PmdFileCollectorContext(after, refactoredCodePath, refactoredCode));

        // Run the Pmd analysis
        AbstractControllerFactory<Void, Map<String, List<CodeSmell>>> analyzerFactory = new PmdFileAnalyzerFactory();
        Map<String, List<CodeSmell>> codeSmells = analyzerFactory.process(null);

        // Link methods and smells
        Map<Version, Map<MethodKey, Method>> methodsByVersion = new HashMap<>();
        methodsByVersion.put(before, noRefactoredMethodMetrics);
        methodsByVersion.put(after, refactoredMethodMetrics);
        AbstractControllerFactory<LinkMethodAndSmellContext, Void> linkMethodAndSmellFactory = new LinkMethodAndSmellFactory();
        linkMethodAndSmellFactory.process(new LinkMethodAndSmellContext(codeSmells, methodsByVersion));

        return methodsByVersion;
    }
}
