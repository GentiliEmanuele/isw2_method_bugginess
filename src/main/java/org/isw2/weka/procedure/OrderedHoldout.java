package org.isw2.weka.procedure;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.factory.CostSensitiveClassifierBuilderFactory;
import org.isw2.weka.utils.context.CostSensitiveClassifierBuilderContext;
import org.isw2.weka.utils.context.SplitterContext;
import org.isw2.weka.factory.SplitterFactory;
import org.isw2.weka.model.Statistics;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.List;

public class OrderedHoldout implements Controller<OrderedHoldoutContext, Statistics> {

    @Override
    public Statistics execute(OrderedHoldoutContext context) throws ProcessingException {
        try {
            // Set buggy as class index for training data
            if (context.trainData().classIndex() == -1)
                context.trainData().setClassIndex(context.trainData().numAttributes() - 1);

            // Set buggy as class index for test data
            if (context.testData().classIndex() == -1)
                context.testData().setClassIndex(context.testData().numAttributes() - 1);

            // Remove identification columns for training and test set
            Instances cleanedTraining = removeIdColumns(context.trainData());
            Instances cleanedTest = removeIdColumns(context.testData());

            // Splitting data in training and validation (assuming that test set is already removed)
            List<Instances> datasets = splitData(cleanedTraining, context.splittingPercentage());
            Instances training = datasets.get(0);
            Instances validation = datasets.get(1);

            double bestCfn = 1.0;
            double bestScore = -1.0;

            double[] costsToTry = {1.0, 2.0, 3.0, 4.0};

            for (double cost : costsToTry) {
                // Build classifier
                CostSensitiveClassifier csc = createCostSensitiveClassifier(context.classifier(), cost);
                csc.buildClassifier(training);

                // Evaluate classifier
                Statistics currentStats = evaluateClassifier(csc, training, validation, cost);

                // Get current f1 score
                double currentScore = currentStats.f1Score();

                if (currentScore > bestScore) {
                    bestCfn = cost;
                }
                System.out.println("Try " +  cost + " score = " +  currentScore);
            }

            return applyTestSet(bestCfn, cleanedTraining, cleanedTest, context.classifier());

        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private Instances removeIdColumns(Instances data) throws ProcessingException {
        try {
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndices("1-5");
            removeFilter.setInputFormat(data);
            return Filter.useFilter(data, removeFilter);
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

    private Statistics evaluateClassifier(CostSensitiveClassifier csc, Instances training, Instances validation, double cost) throws Exception {
        Evaluation eval = new Evaluation(training);
        eval.evaluateModel(csc, validation);
        return new Statistics(eval.precision(1), eval.recall(1), eval.kappa(), eval.areaUnderROC(1), eval.toMatrixString(), eval.fMeasure(1), cost);
    }

    private Statistics applyTestSet(double bestCost, Instances training, Instances test, Classifier classifier) throws Exception {
        CostSensitiveClassifier csc = createCostSensitiveClassifier(classifier, bestCost);
        csc.buildClassifier(training);
        return evaluateClassifier(csc, training, test, bestCost);
    }

}
