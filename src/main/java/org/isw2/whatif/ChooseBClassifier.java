package org.isw2.whatif;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.classifier.ClassifierFactory;
import org.isw2.weka.classifier.ClassifierType;
import org.isw2.weka.factory.SplitDataByVersionFactory;
import org.isw2.weka.factory.WalkForwardFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.procedure.FeatureSelectionUtils;
import org.isw2.weka.procedure.WalkForwardContext;
import org.isw2.weka.tuning.DummyTuner;
import org.isw2.weka.utils.context.SplitDataByVersionContext;
import org.isw2.whatif.context.ChooseClassifierContext;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class ChooseBClassifier implements Controller<ChooseClassifierContext, Classifier> {

    private static final Logger LOGGER = Logger.getLogger(ChooseBClassifier.class.getName());

    @Override
    public Classifier execute(ChooseClassifierContext context) throws ProcessingException {
        // Read the dateset
        Instances dataSet = context.dataset();

        AbstractControllerFactory<SplitDataByVersionContext, List<Instances>> splitterFactory = new SplitDataByVersionFactory();
        AbstractControllerFactory<WalkForwardContext, Map<Integer, Statistics>> walkForwardFactory = new WalkForwardFactory();

        // Split dataset by versions
        List<Instances> dataByVersion = splitterFactory.process(new SplitDataByVersionContext(dataSet, "ReleaseID"));
        List<Instances> clearedByVersion = new ArrayList<>();
        for (Instances data : dataByVersion) {
            try {
                data = FeatureSelectionUtils.removeIds(data);
                clearedByVersion.add(data);
            } catch (Exception e) {
                throw new ProcessingException(e.getMessage());
            }
        }
        Map<ClassifierType, Map<Integer, Statistics>> statsByClassifier = new EnumMap<>(ClassifierType.class);
        for (ClassifierType classifier : context.classifiers()) {
            Map<Integer, Statistics> statsByRun = walkForwardFactory.process(new WalkForwardContext(clearedByVersion, 0.8, classifier, new DummyTuner()));
            statsByClassifier.put(classifier, statsByRun);
        }

        ClassifierType bClassifierType = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<ClassifierType, Map<Integer, Statistics>> entry : statsByClassifier.entrySet()) {
            double currMean = computeMeanF1Score(entry.getValue());
            double currVar = computeVarF1Score(entry.getValue());
            double currentScore = currMean - currVar;

            if (currentScore > bestScore) {
                bClassifierType = entry.getKey();
                bestScore = currentScore;
            }
        }

        LOGGER.log(INFO, "Best classifier found is {0} ", bClassifierType);

        return bClassifierType != null ? ClassifierFactory.createClassifier(bClassifierType) : null;
    }


    private double computeMeanF1Score(Map<Integer, Statistics> statsByRun) {
        double weightedSum = 0.0;
        double totalWeights = 0.0;

        for (Map.Entry<Integer, Statistics> entry : statsByRun.entrySet()) {
            int runIndex = entry.getKey();
            double f1Score = entry.getValue().f1Score();

            weightedSum += f1Score * runIndex;
            totalWeights += runIndex;
        }

        return (totalWeights == 0) ? 0.0 : weightedSum / totalWeights;
    }

    private double computeVarF1Score(Map<Integer, Statistics> statsByRun) {
        if (statsByRun.size() < 2) return 0.0;

        double mean = computeMeanF1Score(statsByRun);
        double sumSquaredDiff = 0.0;

        for (Map.Entry<Integer, Statistics> entry : statsByRun.entrySet()) {
            sumSquaredDiff += Math.pow(entry.getValue().f1Score() - mean, 2);
        }

        return sumSquaredDiff / (statsByRun.size() - 1);
    }

}
