package org.isw2.weka.tuning;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.factory.CostSensitiveClassifierBuilderFactory;
import org.isw2.weka.factory.SplitterFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.utils.context.CostSensitiveClassifierBuilderContext;
import org.isw2.weka.utils.context.SplitterContext;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

import java.util.List;

public class CostSensitiveTuner implements Tuner{

    @Override
    public Classifier tune(Classifier classifier, Instances training, double splittingPercentage) throws ProcessingException {
        // Splitting data in training and validation (assuming that test set is already removed)
        List<Instances> datasets = splitData(training, splittingPercentage);
        Instances trainingSplit = datasets.get(0);
        Instances validationSplit = datasets.get(1);

        double bestCfn = 1.0;
        double bestScore = -1.0;

        double[] costsToTry = {1.0, 2.0, 3.0, 4.0};

        try {
            for (double cost : costsToTry) {
                // Build classifier
                CostSensitiveClassifier csc = createCostSensitiveClassifier(classifier, cost);
                csc.buildClassifier(training);

                // Evaluate classifier
                Statistics currentStats = evaluateClassifier(csc, trainingSplit, validationSplit);

                // Get current f1 score
                double currentScore = currentStats.f1Score();

                if (currentScore > bestScore) {
                    bestCfn = cost;
                }
            }
            return createCostSensitiveClassifier(classifier, bestCfn);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private List<Instances> splitData(Instances data, double splittingPercentage) throws ProcessingException {
        AbstractControllerFactory<SplitterContext, List<Instances>> splitterFactory = new SplitterFactory();
        return splitterFactory.process(new SplitterContext(splittingPercentage, data));
    }

    private CostSensitiveClassifier createCostSensitiveClassifier(Classifier classifier, double falseNegativeCost) throws ProcessingException {
        CostMatrix costMatrix = createCostMatrix(falseNegativeCost);
        AbstractControllerFactory<CostSensitiveClassifierBuilderContext, CostSensitiveClassifier> costSensitiveClassifierBuilderFactory = new CostSensitiveClassifierBuilderFactory();
        return costSensitiveClassifierBuilderFactory.process(new CostSensitiveClassifierBuilderContext(costMatrix, classifier));
    }

    private CostMatrix createCostMatrix(double falseNegativeCost) {
        // Create cost matrix and set weight
        CostMatrix costMatrix = new CostMatrix(2);
        // True negative
        costMatrix.setElement(0, 0, 0.0);
        // False positive
        costMatrix.setElement(0, 1, 1.0);
        // False negative
        costMatrix.setElement(1, 0, falseNegativeCost);
        // True positive
        costMatrix.setElement(1, 1, 0.0);
        return costMatrix;
    }

    private Statistics evaluateClassifier(CostSensitiveClassifier csc, Instances training, Instances validation) throws Exception {
        Evaluation eval = new Evaluation(training);
        eval.evaluateModel(csc, validation);
        return new Statistics(eval.precision(1), eval.recall(1), eval.kappa(), eval.areaUnderROC(1), eval.toMatrixString(), eval.fMeasure(1));
    }
}
