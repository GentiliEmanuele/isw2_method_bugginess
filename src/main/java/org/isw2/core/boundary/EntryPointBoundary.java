package org.isw2.core.boundary;

import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.AbstractControllerFactory;
import org.isw2.factory.EntryPointControllerFactory;

public class EntryPointBoundary {

    private EntryPointBoundary() {
    }

    public static void startAnalysis(String projectName) throws ProcessingException {
        AbstractControllerFactory<String, Void> entryPointFactory = new EntryPointControllerFactory();
        entryPointFactory.process(projectName);
    }

}
