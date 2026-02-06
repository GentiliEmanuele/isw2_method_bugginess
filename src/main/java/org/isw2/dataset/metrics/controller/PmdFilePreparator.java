package org.isw2.dataset.metrics.controller;


import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;


public class PmdFilePreparator implements Controller<PmdFileCollectorContext, TextFile> {

    private final LanguageVersion languageVersion;

    public PmdFilePreparator() {
        this.languageVersion = LanguageRegistry.PMD.getLanguageVersionById("java", "1.8");
    }

    @Override
    public TextFile execute(PmdFileCollectorContext context) throws ProcessingException {
        String uniqueFileId = context.version().getName() + "_" + context.path();
        return TextFile.forCharSeq(context.content(), FileId.fromPathLikeString(uniqueFileId), this.languageVersion);
    }

}
