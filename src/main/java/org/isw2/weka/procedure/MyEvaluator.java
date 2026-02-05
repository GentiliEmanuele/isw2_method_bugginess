package org.isw2.weka.procedure;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.factory.OrderedHoldoutFactory;
import org.isw2.weka.factory.SplitterFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.tuning.DummyTuner;
import weka.attributeSelection.WrapperSubsetEval;
import org.isw2.weka.utils.context.SplitterContext;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.SubsetEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.util.BitSet;
import java.util.List;

public class MyEvaluator extends ASEvaluation implements SubsetEvaluator {
    private static Classifier classifier;
    private SelectedTag f1Tag;

    private Instances training;
    private Instances test;

    public static void setClassifier(Classifier classifier) {
        MyEvaluator.classifier = classifier;
    }

    public void setF1Tag(SelectedTag f1Tag) {
        this.f1Tag = f1Tag;
    }

    @Override
    public void buildEvaluator(Instances instances) throws Exception {
        List<Instances> splitInstances = splitterUtils(instances);
        this.training = splitInstances.get(0);
        this.test = splitInstances.get(1);
    }

    @Override
    public double evaluateSubset(BitSet bitSet) throws Exception {

        // Parse the bit set in an array int[]
        int[] featuresToKeep = bitSetToIntArray(bitSet, this.training.classIndex(), this.training.numAttributes());

        // Filter datasets getting only feature indicated by bit set
        Instances reducedTraining = FeatureSelectionUtils.keepAttributes(this.training, featuresToKeep);
        Instances reducedTest = FeatureSelectionUtils.keepAttributes(this.test, featuresToKeep);

        // Compute the current stats by ordered holdout
        Statistics currentStats = applyOrderedHoldout(reducedTraining, reducedTest);

        // Return the metric
        return getMetricValue(currentStats);
    }

    private int[] bitSetToIntArray(BitSet subset, int classIndex, int numAttributes) {
        // Count how many attribute keep
        int count = 0;
        for (int i = 0; i < numAttributes; i++) {
            if (subset.get(i) || i == classIndex) {
                count++;
            }
        }

        int[] indices = new int[count];
        int k = 0;
        for (int i = 0; i < numAttributes; i++) {
            if (subset.get(i) || i == classIndex) {
                indices[k++] = i;
            }
        }
        return indices;
    }

    private List<Instances> splitterUtils(Instances instances) throws ProcessingException {
        AbstractControllerFactory<SplitterContext, List<Instances>> splitterFactory = new SplitterFactory();
        return splitterFactory.process(new SplitterContext(0.8, instances));
    }

    private Statistics applyOrderedHoldout(Instances training, Instances testing) throws ProcessingException {
        AbstractControllerFactory<OrderedHoldoutContext, Statistics> orderedHoldoutFactory = new OrderedHoldoutFactory();
        return orderedHoldoutFactory.process(new OrderedHoldoutContext(training, 0.8, classifier, testing, new DummyTuner()));
    }


    private double getMetricValue(Statistics stats) {
        // If tag not set return f1 score
        if (this.f1Tag == null) {
            return stats.f1Score();
        }

        // Get the numeric id
        int metricID = this.f1Tag.getSelectedTag().getID();

        // Map the numeric id with the Statistic class
        return switch (metricID) {
            case WrapperSubsetEval.EVAL_FMEASURE -> stats.f1Score();
            case WrapperSubsetEval.EVAL_AUC -> stats.areaUnderROC();
            default -> stats.recall();
        };
    }

}
