package org.isw2.weka.procedure;

import org.isw2.dataset.exceptions.ProcessingException;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WekaBackWardSearchWrapper implements FeatureSelection{
    private static final Logger LOG = Logger.getLogger(WekaBackWardSearchWrapper.class.getName());
    @Override
    public Instances selectFeatures(Instances training) throws ProcessingException {
        // Set the class to predict
        if (training.classIndex() == -1) {
            training.setClassIndex(training.numAttributes() - 1);
        }

        // Create a copy of training
        Instances trainingCopy = FeatureSelection.removeIds(training);

        // Config the wrapper
        WrapperSubsetEval wrapperEval = new WrapperSubsetEval();
        wrapperEval.setClassifier(new J48());
        wrapperEval.setFolds(2);

        // Set F1-score as metric for feature selection
        SelectedTag f1Tag = new SelectedTag(
                WrapperSubsetEval.EVAL_FMEASURE,
                WrapperSubsetEval.TAGS_EVALUATION
        );
        wrapperEval.setEvaluationMeasure(f1Tag);

        // Configure the search method
        GreedyStepwise search = new GreedyStepwise();

        // Choose forward (false) or backward (true)
        search.setSearchBackwards(true);

        // Search the optimal number of feature
        search.setNumToSelect(-1);

        search.setDebuggingOutput(true);

        // Search the best parameters
        AttributeSelection attributeSelection = new AttributeSelection();
        attributeSelection.setEvaluator(wrapperEval);
        attributeSelection.setSearch(search);

        LOG.info("Starting feature selection...");
        try {
            attributeSelection.SelectAttributes(trainingCopy);
            int [] indices = attributeSelection.selectedAttributes();
            return clearDataset(training, indices);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private Instances clearDataset(Instances training, int [] indices) throws Exception {
        List<Integer> toBeRemoved = new ArrayList<>();
        for (int i = 0; i <= 20; i++) {
            if (!isSelected(i, indices)) {
                toBeRemoved.add(i);
            }
        }
        for (Integer i : toBeRemoved) {
            FeatureSelection.setIndexToBeRemoved(i + 5, training);
            LOG.log(Level.INFO, "Removing {0} from training data", training.attribute(i + 5).name());
        }
        return training;
    }

    private boolean isSelected(int i, int [] indices) {
        for (int index : indices) {
            if (i == index) return true;
        }
        return false;
    }
}
