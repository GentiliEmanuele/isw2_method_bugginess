package org.isw2.weka.procedure;

import org.isw2.dataset.exceptions.ProcessingException;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WekaFeatureSelection {

    private static final Logger LOG = Logger.getLogger(WekaFeatureSelection.class.getName());

    public AttributeSelection selectFeatures(Instances training, boolean backward) throws ProcessingException {
        // Set the class to predict
        if (training.classIndex() == -1) {
            training.setClassIndex(training.numAttributes() - 1);
        }

        // Create a copy of training
        Instances trainingCopy = FeatureSelectionUtils.removeIds(training);

        // Set F1-score as metric for feature selection
        SelectedTag f1Tag = new SelectedTag(
                WrapperSubsetEval.EVAL_FMEASURE,
                WrapperSubsetEval.TAGS_EVALUATION
        );

        MyEvaluator evaluator = new MyEvaluator();
        MyEvaluator.setClassifier(new J48());
        evaluator.setF1Tag(f1Tag);

        // Configure the search method
        GreedyStepwise search = new GreedyStepwise();

        // Parallelize the search
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int safeSlots = Math.clamp(availableProcessors / 2, 1, 4);
        search.setNumExecutionSlots(safeSlots);

        // Choose forward (false) or backward (true)
        search.setSearchBackwards(backward);

        // Search the optimal number of feature
        search.setNumToSelect(-1);

        search.setDebuggingOutput(true);

        // Search the best parameters
        AttributeSelection attributeSelection = new AttributeSelection();
        attributeSelection.setEvaluator(evaluator);
        attributeSelection.setSearch(search);

        LOG.log(Level.INFO, "Starting feature selection (backward={0}) ...", backward);

        try {
            if (!backward) search.setStartSet("1");
            attributeSelection.SelectAttributes(trainingCopy);
            return attributeSelection;
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }
}
