package org.isw2.dataset.metrics.controller;


import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class PmdFileCollector implements Controller<PmdFileCollectorContext, Void> {

    private final Map<String, TextFile> contentByVersionAndPath;
    private final LanguageVersion languageVersion;

    public Map<String, TextFile> getContentByVersionAndPath() {
        return contentByVersionAndPath;
    }

    private PmdFileCollector() {
        contentByVersionAndPath = new HashMap<>();
        this.languageVersion = LanguageRegistry.PMD.getLanguageVersionById("java", "1.8");
    }

    @Override
    public Void execute(PmdFileCollectorContext context) throws ProcessingException {
        String uniqueFileId = context.version().getName() + "_" + context.path();
        try (TextFile textFile = TextFile.forCharSeq(context.content(), FileId.fromPathLikeString(uniqueFileId), this.languageVersion)) {
            contentByVersionAndPath.remove(uniqueFileId);
            contentByVersionAndPath.put(uniqueFileId, textFile);
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
        return null;
    }

    private static class SingletonHelper {
        private static final PmdFileCollector INSTANCE = new PmdFileCollector();
    }

    public static PmdFileCollector getInstance() {
        return PmdFileCollector.SingletonHelper.INSTANCE;
    }
}
