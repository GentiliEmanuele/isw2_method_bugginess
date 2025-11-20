package org.isw2.factory;

import org.isw2.core.controller.*;
import org.isw2.git.controller.GetCommitFromGit;
import org.isw2.git.controller.GitController;
import org.isw2.jira.controller.GetTicketFromJira;
import org.isw2.jira.controller.GetVersionsFromJira;
import org.isw2.metrics.complexity.controller.CodeSmellAnalyzer;

public class ControllerFactory {

    private ControllerFactory() {
    }

    public static Controller createController(ControllerType type) {
        return switch (type) {
            case ENTRY_POINT_CONTROLLER -> new EntryPointController();
            case GET_VERSION_FROM_JIRA -> new GetVersionsFromJira();
            case GET_TICKET_FROM_JIRA -> new GetTicketFromJira();
            case GIT_CONTROLLER -> new GitController();
            case GET_COMMIT_FROM_GIT -> new GetCommitFromGit();
            case MERGE_VERSION_AND_COMMIT -> new MergeVersionAndCommit();
            case MAP_COMMITS_AND_METHODS -> new MapCommitsAndMethods();
            case PROPORTION ->  new Proportion();
            case LABELING ->  new Labeling();
            case CODE_SMELL_ANALYZER ->   new CodeSmellAnalyzer();
            default -> throw new IllegalArgumentException("Controller not supported");
        };
    }
}
