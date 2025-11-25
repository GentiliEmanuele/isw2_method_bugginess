package org.isw2.factory;

import org.isw2.git.controller.GetCommitFromGit;
import org.isw2.git.model.Commit;

import java.util.List;

public class GetCommitFactory extends AbstractControllerFactory<String, List<Commit>>{
    @Override
    public Controller<String, List<Commit>> createController() {
        return new GetCommitFromGit();
    }
}
