package org.isw2.metrics.controller;


import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.metrics.controller.context.PmdFileCollectorContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;



public class PmdFileCollector implements Controller<PmdFileCollectorContext, PmdAnalysis>, AutoCloseable {

    private final LanguageVersion languageVersion;
    private final PmdAnalysis pmdAnalysis;


    private PmdFileCollector() {
        PMDConfiguration config = new PMDConfiguration();
        config.addRuleSet("rulesets/java/quickstart.xml");
        config.setSourceEncoding(StandardCharsets.UTF_8);
        this.languageVersion = LanguageRegistry.PMD.getLanguageVersionById("java", "1.8");
        config.setDefaultLanguageVersion(languageVersion);
        config.setFailOnViolation(false);
        config.setFailOnError(false);
        this.pmdAnalysis = PmdAnalysis.create(config);
    }

    @Override
    public PmdAnalysis execute(PmdFileCollectorContext context) throws ProcessingException {
        String uniqueFileId = context.version().getName() + "_" + context.path();
        try (TextFile textFile = TextFile.forCharSeq(context.content(), FileId.fromPathLikeString(uniqueFileId), this.languageVersion)) {
            pmdAnalysis.files().addFile(textFile);
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
        return pmdAnalysis;
    }

    private static class SingletonHelper {
        private static final PmdFileCollector INSTANCE = new PmdFileCollector();
    }

    public static PmdFileCollector getInstance() {
        return PmdFileCollector.SingletonHelper.INSTANCE;
    }

    @Override
    public void close() {
        this.pmdAnalysis.close();
    }
}
