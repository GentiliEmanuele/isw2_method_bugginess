package org.isw2.weka.procedure;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.classifier.ClassifierFactory;
import org.isw2.weka.factory.OrderedHoldoutFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.tuning.Tuner;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WalkForward implements Controller<WalkForwardContext, Map<Integer, Statistics>> {
    private static final Logger LOGGER = Logger.getLogger(WalkForward.class.getName());

    @Override
    public Map<Integer, Statistics> execute(WalkForwardContext context) throws ProcessingException {
        int currentTrainingIndex = 0;
        Map<Integer, Statistics> statsByRun = new HashMap<>();

        LOGGER.log(Level.INFO, "Using {0}", context.classifierType().name());
        for (int currentTestingIndex = 1; currentTestingIndex < context.dataByVersion().size(); currentTestingIndex++) {
            // Build training data
            Instances currentTrainingData = new Instances(context.dataByVersion().get(currentTrainingIndex), 0);
            mergeVersionInTraining(currentTrainingData, context.dataByVersion(), currentTrainingIndex, currentTestingIndex);

            // Build test data
            Instances currentTestingData = new Instances(context.dataByVersion().get(currentTestingIndex));

            // Apply ordered holdout for walk forward iteration
            LOGGER.log(Level.INFO, "Use as testing the version {0}", currentTestingIndex);
            Statistics currentStats;
            if (context.classifierType().name().equals("RANDOM_FOREST")) {
                RandomForest rf = new RandomForest();
                rf.setNumIterations(50);

                rf.setMaxDepth(10);

                rf.setNumFeatures(0);

                rf.setNumExecutionSlots(Runtime.getRuntime().availableProcessors());
                currentStats= callOrderedHoldout(currentTrainingData, currentTestingData, context.trainingPercentage(), rf, context.tuner());
            } else {
                currentStats = callOrderedHoldout(currentTrainingData, currentTestingData, context.trainingPercentage(), ClassifierFactory.createClassifier(context.classifierType()), context.tuner());
            }
            statsByRun.put(currentTestingIndex, currentStats);
        }
        return statsByRun;
    }

    private void mergeVersionInTraining(Instances currentTrainingData, List<Instances> dataByVersion, int currentTrainingIndex, int currentTestingIndex) {
        for (int i = currentTrainingIndex; i < currentTestingIndex; i++) {
            currentTrainingData.addAll(dataByVersion.get(i));
        }
    }

    private Statistics callOrderedHoldout(Instances training, Instances testing, double trainingPercentage, Classifier classifier, Tuner tuner) throws ProcessingException {
        AbstractControllerFactory<OrderedHoldoutContext, Statistics> orderedHoldoutFactory = new OrderedHoldoutFactory();
        return orderedHoldoutFactory.process(new OrderedHoldoutContext(training, trainingPercentage, classifier, testing, tuner));
    }
}
