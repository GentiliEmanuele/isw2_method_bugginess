package org.isw2.dataset.metrics.controller.context;

import net.sourceforge.pmd.PmdAnalysis;
import org.isw2.dataset.core.model.Method;

import java.util.List;
import java.util.Map;

public record PmdFileAnalyzerContext(Map<String, List<Method>> methodsByFileAndVersion, PmdAnalysis pmdAnalysis) {
}
