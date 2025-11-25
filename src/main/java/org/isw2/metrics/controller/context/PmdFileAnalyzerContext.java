package org.isw2.metrics.controller.context;

import net.sourceforge.pmd.PmdAnalysis;
import org.isw2.core.model.Method;

import java.util.List;
import java.util.Map;

public record PmdFileAnalyzerContext(Map<String, List<Method>> methodsByFileAndVersion, PmdAnalysis pmdAnalysis) {
}
