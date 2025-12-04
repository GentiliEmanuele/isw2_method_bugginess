package org.isw2.absfactory;

import org.isw2.dataset.exceptions.ProcessingException;

public interface Controller<I, O> {
    O execute(I input) throws ProcessingException;
}
