package org.isw2.dataset.core.model;

import net.sourceforge.pmd.lang.document.TextFile;
import org.isw2.dataset.git.model.Commit;

import java.util.Map;

public record AnalysisResult(Map<Commit, Map<MethodKey, Method>> methodsByCommit, Map<String, TextFile> pmdFiles) {
}
