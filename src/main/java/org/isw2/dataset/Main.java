package org.isw2.dataset;

import org.isw2.dataset.core.boundary.EntryPointBoundary;
import org.isw2.dataset.exceptions.ProcessingException;

public class Main {

    public static void main(String[] args) throws  ProcessingException {

        EntryPointBoundary.startAnalysis("BOOKKEEPER");

    }
}