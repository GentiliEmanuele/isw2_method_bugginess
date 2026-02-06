package org.isw2.whatif;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.jira.model.Version;
import org.isw2.weka.factory.SplitDataByVersionFactory;
import org.isw2.weka.procedure.FeatureSelectionUtils;
import org.isw2.weka.utils.context.SplitDataByVersionContext;
import org.isw2.whatif.context.ChooseClassifierContext;
import org.isw2.whatif.context.CoordinatorContext;
import org.isw2.whatif.factory.ChooseClassifierFactory;
import org.isw2.whatif.factory.PreliminaryWhatIfFactory;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class WhatIfStudyCoordinator implements Controller<CoordinatorContext, WhatIfStats> {

    private static final String CODE_SMELL = "codeSmell";

    @Override
    public WhatIfStats execute(CoordinatorContext context) throws ProcessingException {
        // This allows to answer to the two preliminary questions
        AbstractControllerFactory<String, Map<Version, Map<MethodKey, Method>>> preliminaryFactory = new PreliminaryWhatIfFactory();
        Map<Version, Map<MethodKey, Method>> refactoredAndNot = preliminaryFactory.process(context.projectName());

        // Load the dataset and set the class
        Instances dataset = getDataSet(context.projectName());
        if (dataset.classIndex() == -1) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
        }

        FeatureSelectionUtils.removeIds(dataset);

        // Choose BClassifier
        AbstractControllerFactory<ChooseClassifierContext, Classifier> classifierFactory = new ChooseClassifierFactory();
        Classifier bClassifier = classifierFactory.process(new ChooseClassifierContext(context.projectName(), context.classifierTypes(), dataset));

        AbstractControllerFactory<SplitDataByVersionContext, List<Instances>> splitterFactory =  new SplitDataByVersionFactory();
        List<Instances> dataByVersion = splitterFactory.process(new SplitDataByVersionContext(dataset, "ReleaseID"));

        Instances train = dataByVersion.getFirst();
        Instances test = dataByVersion.getLast();

        trainBestClassifier(bClassifier, train);

        int expectedBugComplete = countExpectedBuggy(test);
        int actualBugComplete = countActualBuggy(test, bClassifier);

        // Create B+ dataset
        Instances positiveSmell = filterMethodWithSmells(test);
        int expectedBugWithSmell = countExpectedBuggy(positiveSmell);
        int actualBugWithSmell = countActualBuggy(positiveSmell, bClassifier);

        // Create C dataset
        Instances noSmell = filterMethodWithoutSmells(test);
        int expectedBugWithoutSmell = countExpectedBuggy(noSmell);
        int actualBugWithoutSmell = countActualBuggy(noSmell, bClassifier);

        // Create B dataset manipulating B+
        Instances resetSmell = resetSmell(positiveSmell);
        int actualAfterSmellReset = countActualBuggy(resetSmell, bClassifier);

        return new WhatIfStats(expectedBugComplete, actualBugComplete, expectedBugWithSmell, actualBugWithSmell, expectedBugWithoutSmell,  actualBugWithoutSmell, actualAfterSmellReset, refactoredAndNot);

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

    private Instances filterMethodWithSmells(Instances dataset) throws ProcessingException {
        int indexCodeSmellCol = dataset.attribute(CODE_SMELL).index();

        RemoveWithValues filter = new RemoveWithValues();
        // Parse the index from 0-based to 1-based
        filter.setAttributeIndex(String.valueOf(indexCodeSmellCol + 1));
        filter.setSplitPoint(0.5);

        // Apply the filter
        try {
            filter.setInputFormat(dataset);
            return Filter.useFilter(dataset, filter);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private Instances filterMethodWithoutSmells(Instances dataset) throws ProcessingException {
        int index0Based = dataset.attribute(CODE_SMELL).index();

        RemoveWithValues filter = new RemoveWithValues();
        filter.setAttributeIndex(String.valueOf(index0Based + 1));

        filter.setSplitPoint(0.5);

        filter.setInvertSelection(true);

        try {
            filter.setInputFormat(dataset);
            return Filter.useFilter(dataset, filter);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private Instances resetSmell(Instances dataset) {
        Instances copyDataset = new Instances(dataset);

        int colIndex = copyDataset.attribute(CODE_SMELL).index();

        for (int i = 0; i < copyDataset.numInstances(); i++) {
            copyDataset.instance(i).setValue(colIndex, 0.0);
        }

        return copyDataset;
    }

    private int countActualBuggy(Instances dataset, Classifier classifier) throws ProcessingException {
        int countBuggy = 0;
        try {
            for (int i = 0; i < dataset.numInstances(); i++) {
                double buggyValueIndex = dataset.classAttribute().indexOfValue("true");
                double actual = classifier.classifyInstance(dataset.instance(i));
                if (actual == buggyValueIndex) countBuggy++;
            }
            return countBuggy;
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private int countExpectedBuggy(Instances dataset) {
        double buggyValueIndex = dataset.classAttribute().indexOfValue("true");
        int count = 0;
        for (int i = 0; i < dataset.numInstances(); i++) {
            if (dataset.instance(i).classValue() == buggyValueIndex) {
                count++;
            }
        }
        return count;
    }

}
