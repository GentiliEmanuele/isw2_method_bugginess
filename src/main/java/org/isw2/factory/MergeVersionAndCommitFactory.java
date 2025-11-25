package org.isw2.factory;

import org.isw2.core.controller.MergeVersionAndCommit;
import org.isw2.core.controller.context.MergeVersionAndCommitContext;

public class MergeVersionAndCommitFactory extends AbstractControllerFactory<MergeVersionAndCommitContext, Void> {
    @Override
    public Controller<MergeVersionAndCommitContext, Void> createController() {
        return new MergeVersionAndCommit();
    }
}
