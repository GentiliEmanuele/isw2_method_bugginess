package org.isw2.weka.procedure;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.model.Statistics;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;


public class OrderedHoldout implements Controller<OrderedHoldoutContext, Statistics> {

    @Override
    public Statistics execute(OrderedHoldoutContext context) throws ProcessingException {
        try {
            // Set buggy as class index for training data
            context.trainData().setClassIndex(context.trainData().numAttributes() - 1);

            // Set buggy as class index for test data
            context.testData().setClassIndex(context.testData().numAttributes() - 1);

            Classifier classifier = context.tuner().tune(context.classifier(), context.trainData(), context.splittingPercentage());

            return applyTestSet(context.trainData(), context.testData(), classifier);

        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private Statistics evaluateClassifier(Classifier classifier, Instances training, Instances validation) throws Exception {
        Evaluation eval = new Evaluation(training);
        eval.evaluateModel(classifier, validation);
        return new Statistics(eval.precision(1), eval.recall(1), eval.kappa(), eval.areaUnderROC(1), eval.toMatrixString(), eval.fMeasure(1));
    }

    private Statistics applyTestSet(Instances training, Instances test, Classifier classifier) throws Exception {
        classifier.buildClassifier(training);
        return evaluateClassifier(classifier, training, test);
    }

}
