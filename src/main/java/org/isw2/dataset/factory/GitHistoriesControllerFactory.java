package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.git.controller.GitHistoriesController;
import org.isw2.dataset.git.controller.GitHistoriesControllerContext;

public class GitHistoriesControllerFactory extends AbstractControllerFactory<GitHistoriesControllerContext, Void> {

    @Override
    public Controller<GitHistoriesControllerContext, Void> createController() {
        return new GitHistoriesController();
    }

}
