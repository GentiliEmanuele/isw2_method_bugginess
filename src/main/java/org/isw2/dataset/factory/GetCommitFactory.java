package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.git.controller.GetCommitFromGit;
import org.isw2.dataset.git.model.Commit;

import java.util.List;

public class GetCommitFactory extends AbstractControllerFactory<String, List<Commit>> {
    @Override
    public Controller<String, List<Commit>> createController() {
        return new GetCommitFromGit();
    }
}
