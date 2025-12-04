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
            // Set buggy as class index
            if (context.trainData().classIndex() == -1)
                context.trainData().setClassIndex(context.trainData().numAttributes() - 1);

            // Remove identification columns
            Instances cleaned = removeIdColumns(context.trainData());

            // Splitting data in training and validation (assuming that test set is already removed)
            List<Instances> datasets = splitData(cleaned, context.splittingPercentage());
            Instances training = datasets.get(0);
            Instances validation = datasets.get(1);

            // Build classifier
            CostSensitiveClassifier csc = createCostSensitiveClassifier(context.classifier());
            csc.buildClassifier(training);

            // Evaluate classifier
            return evaluateClassifier(csc, training, validation);

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

    private CostSensitiveClassifier createCostSensitiveClassifier(Classifier classifier) throws ProcessingException {
        CostMatrix costMatrix = createCostMatrix();
        AbstractControllerFactory<CostSensitiveClassifierBuilderContext, CostSensitiveClassifier> costSensitiveClassifierBuilderFactory = new CostSensitiveClassifierBuilderFactory();
        return costSensitiveClassifierBuilderFactory.process(new CostSensitiveClassifierBuilderContext(costMatrix, classifier));
    }

    private CostMatrix createCostMatrix() {
        // Create cost matrix and set weight
        CostMatrix costMatrix = new CostMatrix(2);
        // True negative
        costMatrix.setElement(0, 0, 0.0);
        // False positive
        costMatrix.setElement(0, 1, 1.0);
        // False negative
        costMatrix.setElement(1, 0, 5.0);
        // True positive
        costMatrix.setElement(1, 1, 0.0);
        return costMatrix;
    }

    private Statistics evaluateClassifier(CostSensitiveClassifier csc, Instances training, Instances validation) throws Exception {
        Evaluation eval = new Evaluation(training);
        eval.evaluateModel(csc, validation);
        return new Statistics(eval.precision(1), eval.recall(1), eval.kappa(), eval.areaUnderROC(1), eval.toMatrixString());
    }

}
