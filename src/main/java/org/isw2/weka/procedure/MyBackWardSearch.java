package org.isw2.weka.procedure;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.factory.OrderedHoldoutFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.tuning.DummyTuner;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.util.List;
import java.util.logging.Logger;

public class MyBackWardSearch {

    private static final Logger LOGGER = Logger.getLogger(MyBackWardSearch.class.getName());
    private static final int FIRST_VALID_FEATURE_INDEX = 5;

    public Instances selectFeatures(Instances training, boolean backward) throws ProcessingException {

        if (training.classIndex() == -1)
            training.setClassIndex(training.numAttributes() - 1);

        // Start with complete dataset
        Instances currentData = new Instances(training);

        // Compute the baseline with complete dataset
        double bestGlobalF1Score = evaluate(currentData).f1Score();

        boolean improved = true;

        while (improved) {
            // Stop if remain only ID + 1 Feature + Class
            if (currentData.numAttributes() <= FIRST_VALID_FEATURE_INDEX + 2) break;

            improved = false;
            int bestAttributeToRemove = -1;
            double bestF1InThisRound = bestGlobalF1Score;

            // The num of attribute change
            int numAttributes = currentData.numAttributes();
            int classIndex = currentData.classIndex();

            for (int i = FIRST_VALID_FEATURE_INDEX; i < numAttributes; i++) {

                if (i == classIndex) continue;

                // Create a temp dataset without the i-th feature
                Instances tempTraining = wrapperSetIndexToBeRemoved(i, currentData);

                // Evaluate this temporary dataset
                Statistics stats = evaluate(tempTraining);
                double currentF1 = stats.f1Score();


                // Search the remotion that improve f1-score
                if (currentF1 >= bestF1InThisRound) {
                    bestF1InThisRound = currentF1;
                    bestAttributeToRemove = i;
                }
            }

            if (bestAttributeToRemove != -1) {
                String msg = String.format("Removing %s from the training data", currentData.attribute(bestAttributeToRemove).name());
                LOGGER.info(msg);

                // Apply the remotion to the effective dataset
                currentData = wrapperSetIndexToBeRemoved(bestAttributeToRemove, currentData);

                // Update the best global score
                bestGlobalF1Score = bestF1InThisRound;

                // Search other attribute to remove
                improved = true;
            } else {
                LOGGER.info("No improvement found in this round. Stopping.");
            }
        }
        return currentData;
    }


    private Instances wrapperSetIndexToBeRemoved(int x, Instances training) throws ProcessingException {
        try {
            return FeatureSelectionUtils.setIndexToBeRemoved(x, training);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private Statistics evaluate(Instances training) throws ProcessingException {
        // Split training and testing set
        List<Instances> splitTraining = FeatureSelectionUtils.splitData(training, 0.8);
        Instances trainSet = splitTraining.getFirst();
        Instances testSet = splitTraining.getLast();

        // Remove ids cols (only for the evaluation of the model)
        Instances cleanTrain = FeatureSelectionUtils.removeIds(trainSet);
        Instances cleanTest = FeatureSelectionUtils.removeIds(testSet);

        // Create a RandomForest classifier doing only 10 iteration (speeder then default configuration but sufficient for feature selection)
        RandomForest randomForest = new RandomForest();
        randomForest.setNumIterations(10);

        // Evaluate the current parameters set
        AbstractControllerFactory<OrderedHoldoutContext, Statistics> orderedHoldoutFactory = new OrderedHoldoutFactory();
        return orderedHoldoutFactory.process(new OrderedHoldoutContext(cleanTrain, 0.8, randomForest, cleanTest, new DummyTuner()));
    }
}
