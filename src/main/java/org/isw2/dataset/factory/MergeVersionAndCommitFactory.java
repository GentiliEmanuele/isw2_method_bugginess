package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.LinkVersionAndCommit;
import org.isw2.dataset.core.controller.context.MergeVersionAndCommitContext;

public class MergeVersionAndCommitFactory extends AbstractControllerFactory<MergeVersionAndCommitContext, Void> {
    @Override
    public Controller<MergeVersionAndCommitContext, Void> createController() {
        return new LinkVersionAndCommit();
    }
}
