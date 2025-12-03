package org.isw2.dataset.core.boundary;

import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.factory.AbstractControllerFactory;
import org.isw2.dataset.factory.EntryPointControllerFactory;

public class EntryPointBoundary {

    private EntryPointBoundary() {
    }

    public static void startAnalysis(String projectName) throws ProcessingException {
        AbstractControllerFactory<String, Void> entryPointFactory = new EntryPointControllerFactory();
        entryPointFactory.process(projectName);
    }

}
