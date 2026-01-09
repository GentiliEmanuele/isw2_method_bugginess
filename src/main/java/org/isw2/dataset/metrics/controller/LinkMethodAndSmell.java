package org.isw2.dataset.metrics.controller;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.metrics.controller.context.LinkMethodAndSmellContext;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.util.List;

public class LinkMethodAndSmell implements Controller<LinkMethodAndSmellContext, Void> {

    @Override
    public Void execute(LinkMethodAndSmellContext context) throws ProcessingException {
        context.methodsByVersion().forEach((version, methodsByVersion) -> {
            methodsByVersion.keySet().forEach(key -> {
                // 1. Get the smells for the specified files
                String smellKey = version.getName() + "_" + key.path();
                List<CodeSmell> smellsOfThisFile = context.smellsByPathAndVersion().get(smellKey);

                // 2. Get the methods only of this files
                Method method = methodsByVersion.get(key);

                // If both are available map method and smell
                if (smellsOfThisFile != null && method != null) {
                    mapMethodsAndSmells(smellsOfThisFile, method);
                }
            });
        });
        return null;
    }

    private void mapMethodsAndSmells(List<CodeSmell> smells, Method method) {
        for (CodeSmell smell : smells) {
            if (smell.getStartLine() >= method.getStartLine() && smell.getStartLine() <= method.getEndLine() &&  smell.getEndLine() >= method.getStartLine() && smell.getEndLine() <= method.getEndLine()) {
                method.getMetrics().setCodeSmellCounter(method.getMetrics().getCodeSmellCounter() + 1);
            }
        }
    }
}
