package org.isw2.experiment;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.classifier.ClassifierType;
import org.isw2.weka.factory.SplitDataByVersionFactory;
import org.isw2.weka.factory.WalkForwardFactory;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.procedure.WalkForwardContext;
import org.isw2.weka.tuning.DummyTuner;
import org.isw2.weka.utils.StatsToCsv;
import org.isw2.weka.utils.context.SplitDataByVersionContext;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class Experiments {
    private static final String RELEASE_ID = "ReleaseID";
    private static final Logger LOGGER = Logger.getLogger(Experiments.class.getName());

    public static void main(String[] args) throws IOException, ProcessingException {
        // Load configuration
        List<String> projectNames = ConfigLoader.loadProjects();
        List<ClassifierType> classifiersToTest = ConfigLoader.loadClassifiers();

        if (projectNames.isEmpty() || classifiersToTest.isEmpty()) {
            LOGGER.severe("Projects or Classifiers list is empty in config.properties. Exiting.");
            return;
        }

        // Create the factory for Weka controller
        AbstractControllerFactory<SplitDataByVersionContext, List<Instances>> splitterFactory = new SplitDataByVersionFactory();
        AbstractControllerFactory<WalkForwardContext, Map<Integer, Statistics>> walkForwardFactory = new WalkForwardFactory();

        for (String projectName : projectNames) {
            // Load a CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("output/" + projectName + ".csv"));
            Instances dataSet = loader.getDataSet();

            // Split dataset by versions
            List<Instances> dataByVersion = splitterFactory.process(new SplitDataByVersionContext(dataSet, RELEASE_ID));

            // Call walk forward procedure foreach classifier
            Map<ClassifierType, Map<Integer, Statistics>> statsByClassifier = new EnumMap<>(ClassifierType.class);
            for (ClassifierType classifier : classifiersToTest) {
                Map<Integer, Statistics> statsByRun = walkForwardFactory.process(new WalkForwardContext(dataByVersion, 0.8, classifier, new DummyTuner()));
                statsByClassifier.put(classifier, statsByRun);
            }

            StatsToCsv.writeStatsToCsv(projectName, statsByClassifier);

        }

    }
}
