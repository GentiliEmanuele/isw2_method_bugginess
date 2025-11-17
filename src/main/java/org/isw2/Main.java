package org.isw2;

import org.isw2.core.boundary.EntryPointBoundary;
import org.isw2.exceptions.ProcessingException;

public class Main {

    public static void main(String[] args) throws  ProcessingException {

        EntryPointBoundary.startAnalysis("BOOKKEEPER");

    }
}