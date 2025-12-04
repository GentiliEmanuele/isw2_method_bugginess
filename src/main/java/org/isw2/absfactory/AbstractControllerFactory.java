package org.isw2.absfactory;

import org.isw2.dataset.exceptions.ProcessingException;

public abstract class AbstractControllerFactory<I, O> {

    public abstract Controller<I, O> createController();

    public O process(I input) throws ProcessingException {
        Controller<I, O> controller = createController();
        return controller.execute(input);
    }
}
