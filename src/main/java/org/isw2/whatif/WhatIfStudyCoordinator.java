package org.isw2.whatif;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.boundary.EntryPointBoundary;
import org.isw2.dataset.core.controller.context.EntryPointContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.jira.model.Version;
import org.isw2.whatif.context.ChooseClassifierContext;
import org.isw2.whatif.context.CoordinatorContext;
import org.isw2.whatif.factory.ChooseClassifierFactory;
import org.isw2.whatif.factory.PreliminaryWhatIfFactory;
import weka.classifiers.Classifier;

import java.util.Map;


public class WhatIfStudyCoordinator implements Controller<CoordinatorContext, Map<Version, Map<MethodKey, Method>>> {
    @Override
    public Map<Version, Map<MethodKey, Method>> execute(CoordinatorContext context) throws ProcessingException {
        // This allows to answer to the two preliminary questions
        AbstractControllerFactory<String, Map<Version, Map<MethodKey, Method>>> preliminaryFactory = new PreliminaryWhatIfFactory();
        Map<Version, Map<MethodKey, Method>> refactoredAndNot = preliminaryFactory.process(context.projectName());

        // Build the dataset
        EntryPointBoundary.startAnalysis(new EntryPointContext(context.projectName(), context.discardPercentage()));

        // Choose BClassifier
        AbstractControllerFactory<ChooseClassifierContext, Classifier> classifierFactory = new ChooseClassifierFactory();
        Classifier BClassifier = classifierFactory.process(new ChooseClassifierContext(context.projectName(), context.classifierTypes()));


        return refactoredAndNot;
    }
}
