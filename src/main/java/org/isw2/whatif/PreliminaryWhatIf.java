package org.isw2.whatif;

import net.sourceforge.pmd.lang.document.TextFile;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.factory.JavaMetricParserFactory;
import org.isw2.dataset.factory.LinkMethodAndSmellFactory;
import org.isw2.dataset.factory.PmdFileAnalyzerFactory;
import org.isw2.dataset.factory.PmdFilePreparatorFactory;
import org.isw2.dataset.jira.model.Version;
import org.isw2.dataset.metrics.controller.context.LinkMethodAndSmellContext;
import org.isw2.dataset.metrics.controller.context.ParserContext;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreliminaryWhatIf implements Controller<String, Map<Version, Map<MethodKey, Method>>> {
    @Override
    public Map<Version, Map<MethodKey, Method>> execute(String projectName) throws ProcessingException {
        String noRefactoredCodePath = String.format("/refactoring/%s/before.java", projectName);
        String refactoredCodePath = String.format("/refactoring/%s/after.java", projectName);
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
        AbstractControllerFactory<PmdFileCollectorContext, TextFile> collectorFactory = new PmdFilePreparatorFactory();
        TextFile beforeRefactoring = collectorFactory.process(new PmdFileCollectorContext(before, noRefactoredCodePath, noRefactoredCode));
        TextFile afterRefactoring = collectorFactory.process(new PmdFileCollectorContext(after, refactoredCodePath, refactoredCode));
        Map<String, TextFile> contentByPathAndVersion = new HashMap<>();
        contentByPathAndVersion.put(before.getName() + "_" + noRefactoredCodePath, beforeRefactoring);
        contentByPathAndVersion.put(after.getName() + "_" + refactoredCodePath, afterRefactoring);

        // Run the Pmd analysis
        AbstractControllerFactory<Map<String, TextFile>, Map<String, List<CodeSmell>>> analyzerFactory = new PmdFileAnalyzerFactory();
        Map<String, List<CodeSmell>> codeSmells = analyzerFactory.process(contentByPathAndVersion);

        // Link methods and smells
        Map<Version, Map<MethodKey, Method>> methodsByVersion = new HashMap<>();
        methodsByVersion.put(before, noRefactoredMethodMetrics);
        methodsByVersion.put(after, refactoredMethodMetrics);
        AbstractControllerFactory<LinkMethodAndSmellContext, Void> linkMethodAndSmellFactory = new LinkMethodAndSmellFactory();
        linkMethodAndSmellFactory.process(new LinkMethodAndSmellContext(codeSmells, methodsByVersion));

        return methodsByVersion;
    }
}
