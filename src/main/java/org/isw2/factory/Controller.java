package org.isw2.factory;

import org.isw2.exceptions.ProcessingException;

public interface Controller<I, O> {
    O execute(I input) throws ProcessingException;
}
