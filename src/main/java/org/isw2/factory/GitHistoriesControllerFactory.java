package org.isw2.factory;

import org.isw2.git.controller.GitHistoriesController;
import org.isw2.git.controller.GitHistoriesControllerContext;

public class GitHistoriesControllerFactory extends AbstractControllerFactory<GitHistoriesControllerContext, Void> {

    @Override
    public Controller<GitHistoriesControllerContext, Void> createController() {
        return new GitHistoriesController();
    }

}
