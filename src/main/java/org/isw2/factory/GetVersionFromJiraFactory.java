package org.isw2.factory;

import org.isw2.exceptions.ProcessingException;
import org.isw2.jira.controller.GetVersionsFromJira;
import org.isw2.jira.model.Version;

import java.util.List;

public class GetVersionFromJiraFactory extends AbstractControllerFactory<String, List<Version>> {

    @Override
    public Controller<String, List<Version>> createController() {
        return new GetVersionsFromJira();
    }

}
