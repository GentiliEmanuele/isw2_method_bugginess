package org.isw2.dataset.core.boundary;

import org.isw2.dataset.core.controller.context.EntryPointContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.factory.EntryPointControllerFactory;

public class EntryPointBoundary {

    private EntryPointBoundary() {
    }

    public static void startAnalysis(EntryPointContext context) throws ProcessingException {
        AbstractControllerFactory<EntryPointContext, Void> entryPointFactory = new EntryPointControllerFactory();
        entryPointFactory.process(context);
    }

}
