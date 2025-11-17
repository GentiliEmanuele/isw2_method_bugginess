package org.isw2.core.boundary;

import org.isw2.core.controller.context.EntryPointContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ControllerFactory;
import org.isw2.factory.ControllerType;

public class EntryPointBoundary {

    private EntryPointBoundary() {
    }

    public static void startAnalysis(String projectName) throws ProcessingException {
        Controller entryPointController = ControllerFactory.createController(ControllerType.ENTRY_POINT_CONTROLLER);
        entryPointController.execute(new EntryPointContext(projectName));
    }

}
