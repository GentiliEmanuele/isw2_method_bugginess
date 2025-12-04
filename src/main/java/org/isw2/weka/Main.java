package org.isw2.weka;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.factory.OrderedHoldoutFactory;
import org.isw2.weka.factory.SplitterFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.procedure.OrderedHoldoutContext;
import org.isw2.weka.utils.context.SplitterContext;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ProcessingException {

        // Load a CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("output/BOOKKEEPER.csv"));
        Instances dataSet = loader.getDataSet();

        // Split data into training and testing set
        AbstractControllerFactory<SplitterContext, List<Instances>> splitterFactory = new SplitterFactory();
        List<Instances> datasets = splitterFactory.process(new SplitterContext(0.6,dataSet));
        Instances training = datasets.get(0);
        Instances testing = datasets.get(1);

        AbstractControllerFactory<OrderedHoldoutContext, Statistics> orderedHoldoutFactory = new OrderedHoldoutFactory();
        Statistics stats = orderedHoldoutFactory.process(new OrderedHoldoutContext(training, 0.8, new RandomForest()));

        System.out.println("Precision (Buggy=True): " + stats.precision());
        System.out.println("Recall (Buggy=True): " + stats.recall());
        System.out.println("Kappa: " + stats.kappa());
        System.out.println("AUC: " + stats.areaUnderROC());
    }
}
