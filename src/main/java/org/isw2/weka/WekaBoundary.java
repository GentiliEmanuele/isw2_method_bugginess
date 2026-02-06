package org.isw2.weka;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.classifier.ClassifierType;
import org.isw2.weka.factory.SplitDataByVersionFactory;
import org.isw2.weka.factory.WalkForwardFactory;
import org.isw2.weka.factory.WekaCorrelationFactory;
import org.isw2.weka.model.Correlation;
import org.isw2.weka.model.Statistics;
import org.isw2.weka.procedure.FeatureSelectionUtils;
import org.isw2.weka.procedure.WalkForwardContext;
import org.isw2.weka.procedure.WekaFeatureSelection;
import org.isw2.weka.tuning.DummyTuner;
import org.isw2.weka.utils.StatsToCsv;
import org.isw2.weka.utils.context.SplitDataByVersionContext;
import weka.attributeSelection.AttributeSelection;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WekaBoundary {

    private static final String RELEASE_ID = "ReleaseID";
    private static final Logger logger = Logger.getLogger(WekaBoundary.class.getName());

    private WekaBoundary() {}

    public static void wekaBoundaryWork(String projectName, List<ClassifierType> classifiersToTest, boolean activeFeatureSelection, boolean backward) throws IOException, ProcessingException {
        // Load a CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("output/" + projectName + ".csv"));
        Instances dataSet = loader.getDataSet();

        AbstractControllerFactory<SplitDataByVersionContext, List<Instances>> splitterFactory = new SplitDataByVersionFactory();
        AbstractControllerFactory<WalkForwardContext, Map<Integer, Statistics>> walkForwardFactory = new WalkForwardFactory();

        // Split dataset by versions
        List<Instances> dataByVersion = splitterFactory.process(new SplitDataByVersionContext(dataSet, RELEASE_ID));

        WekaFeatureSelection wekaFeatureSelection = new WekaFeatureSelection();
        AttributeSelection attributeSelection = null;
        if (activeFeatureSelection) attributeSelection = wekaFeatureSelection.selectFeatures(dataSet, backward);
        List<Instances> selectedByVersion = new ArrayList<>();
        for (Instances data : dataByVersion) {
            try {
                data = FeatureSelectionUtils.removeIds(data);
                if (activeFeatureSelection) data = attributeSelection.reduceDimensionality(data);
                selectedByVersion.add(data);
            } catch (Exception e) {
                throw new ProcessingException(e.getMessage());
            }
        }

        // Call walk forward procedure foreach classifier
        Map<ClassifierType, Map<Integer, Statistics>> statsByClassifier = new EnumMap<>(ClassifierType.class);
        for (ClassifierType classifier : classifiersToTest) {
            Map<Integer, Statistics> statsByRun = walkForwardFactory.process(new WalkForwardContext(selectedByVersion, 0.8, classifier, new DummyTuner()));
            statsByClassifier.put(classifier, statsByRun);
        }

        StatsToCsv.writeStatsToCsv(projectName, statsByClassifier);

        logger.log(Level.INFO, "Start to compute correlation");
        AbstractControllerFactory<String, List<Correlation>> computeCorrelationFactory = new WekaCorrelationFactory();
        List<Correlation> correlations = computeCorrelationFactory.process(projectName);

        StatsToCsv.writeCorrelationToCsv(projectName, correlations);
    }
}
