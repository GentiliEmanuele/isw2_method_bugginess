package org.isw2.factory;

import org.isw2.exceptions.ProcessingException;

public interface Controller {
    void execute(ExecutionContext context) throws ProcessingException;
}
