package org.isw2.weka;

import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.supervised.instance.ClassBalancer;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {

            // Load a CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("output/BOOKKEEPER.csv"));
            Instances allData = loader.getDataSet();

            // Set buggy as class index
            if (allData.classIndex() == -1)
                allData.setClassIndex(allData.numAttributes() - 1);

            // Remove identification columns
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndices("1-5");
            removeFilter.setInputFormat(allData);
            Instances dataCleaned = Filter.useFilter(allData, removeFilter);

            // Execute temporal split (66-33)
            int trainSize = (int) Math.round(dataCleaned.numInstances() * 0.66);
            int testSize = dataCleaned.numInstances() - trainSize;

            // Create the two set
            Instances trainData = new Instances(dataCleaned, 0, trainSize);
            Instances testData = new Instances(dataCleaned, trainSize, testSize);

            // Class balancing in the training set
            ClassBalancer balancer = new ClassBalancer();
            balancer.setInputFormat(trainData);
            Instances trainBalanced = Filter.useFilter(trainData, balancer);

            // Create CostSensitiveClassifier
            CostSensitiveClassifier csc = new CostSensitiveClassifier();
            csc.setClassifier(new RandomForest());

            // Create cost matrix and set weight
            CostMatrix costMatrix = new CostMatrix(2);
            // True negative
            costMatrix.setElement(0, 0, 0.0);
            // False positive
            costMatrix.setElement(0, 1, 1.0);
            // False negative
            costMatrix.setElement(1, 0, 3.0);
            // True positive
            costMatrix.setElement(1, 1, 0.0);
            csc.setCostMatrix(costMatrix);

            csc.setMinimizeExpectedCost(true);
            csc.buildClassifier(trainData);

            // Evaluate using test set
            Evaluation eval = new Evaluation(trainBalanced);
            eval.evaluateModel(csc, testData);

            // Print results
            System.out.println(eval.toSummaryString("\nRisultati Ordered Holdout:\n", false));
            System.out.println("Precision (Buggy=True): " + eval.precision(1));
            System.out.println("Recall (Buggy=True): " + eval.recall(1));
            System.out.println("Kappa: " + eval.kappa());
            System.out.println("AUC: " + eval.areaUnderROC(1));
            System.out.println(eval.toMatrixString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
