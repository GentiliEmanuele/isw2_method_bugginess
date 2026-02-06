package org.isw2.whatif;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.jira.model.Version;
import org.isw2.weka.factory.SplitterFactory;
import org.isw2.weka.procedure.FeatureSelectionUtils;
import org.isw2.weka.utils.context.SplitterContext;
import org.isw2.whatif.context.ChooseClassifierContext;
import org.isw2.whatif.context.CoordinatorContext;
import org.isw2.whatif.factory.ChooseClassifierFactory;
import org.isw2.whatif.factory.PreliminaryWhatIfFactory;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class WhatIfStudyCoordinator implements Controller<CoordinatorContext, Map<Version, Map<MethodKey, Method>>> {

    @Override
    public Map<Version, Map<MethodKey, Method>> execute(CoordinatorContext context) throws ProcessingException {
        // This allows to answer to the two preliminary questions
        AbstractControllerFactory<String, Map<Version, Map<MethodKey, Method>>> preliminaryFactory = new PreliminaryWhatIfFactory();
        Map<Version, Map<MethodKey, Method>> refactoredAndNot = preliminaryFactory.process(context.projectName());

        // Load the dataset and set the class
        Instances dataset = getDataSet(context.projectName());
        if (dataset.classIndex() == -1) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
        }

        FeatureSelectionUtils.removeIds(dataset);

        // Split dataset into train and test set
        AbstractControllerFactory<SplitterContext, List<Instances>> splitterFactory = new SplitterFactory();
        // Use only 50 % of the data
        List<Instances> discarded = splitterFactory.process(new SplitterContext(0.5, dataset));

        List<Instances> trainAndTest = splitterFactory.process(new SplitterContext(0.8, discarded.getFirst()));
        // Choose BClassifier
        AbstractControllerFactory<ChooseClassifierContext, Classifier> classifierFactory = new ChooseClassifierFactory();
        Classifier bClassifier = classifierFactory.process(new ChooseClassifierContext(context.projectName(), context.classifierTypes(), dataset));

        Instances train = trainAndTest.get(0);

        // Train BClassifier on A (original dataset)
        trainBestClassifier(bClassifier, train);

        return refactoredAndNot;
    }

    private Instances getDataSet(String projectName) throws ProcessingException {
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("output/" + projectName + ".csv"));
            return loader.getDataSet();
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private void trainBestClassifier(Classifier bClassifier, Instances dataset) throws ProcessingException {
        try {
            bClassifier.buildClassifier(dataset);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

}
