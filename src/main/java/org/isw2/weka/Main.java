package org.isw2.weka;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.factory.SplitDataByVersionFactory;
import org.isw2.weka.factory.WalkForwardFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.procedure.WalkForwardContext;
import org.isw2.weka.tuning.CostSensitiveTuner;
import org.isw2.weka.tuning.DummyTuner;
import org.isw2.weka.tuning.Tuner;
import org.isw2.weka.utils.StatsToCsv;
import org.isw2.weka.utils.context.SplitDataByVersionContext;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    private static final String RELEASE_ID = "ReleaseID";

    public static void main(String[] args) throws IOException, ProcessingException {

        String projectName = "BOOKKEEPER";
        // Load a CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("output/" + projectName + ".csv"));
        Instances dataSet = loader.getDataSet();

        // Split dataset by versions
        AbstractControllerFactory<SplitDataByVersionContext, List<Instances>> splitterFactory = new SplitDataByVersionFactory();
        List<Instances> dataByVersion = splitterFactory.process(new SplitDataByVersionContext(dataSet, RELEASE_ID));

        // Call walk forward procedure
        AbstractControllerFactory<WalkForwardContext, Map<Integer, Statistics>> walkForwardFactory = new WalkForwardFactory();
        Map<Integer, Statistics> statsByRun = walkForwardFactory.process(new WalkForwardContext(dataByVersion, 0.8, new RandomForest(), new DummyTuner()));

        StatsToCsv.writeStatsToCsv(projectName, statsByRun);

    }
}
