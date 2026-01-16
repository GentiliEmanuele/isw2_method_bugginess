package org.isw2.experiment;


import org.isw2.dataset.core.boundary.EntryPointBoundary;
import org.isw2.dataset.core.controller.context.EntryPointContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.WekaBoundary;
import org.isw2.weka.classifier.ClassifierType;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;


public class Experiments {
    private static final Logger LOGGER = Logger.getLogger(Experiments.class.getName());

    public static void main(String[] args) throws IOException, ProcessingException {
        // Load configuration
        List<String> projectNames = ConfigLoader.loadProjects();
        List<ClassifierType> classifiersToTest = ConfigLoader.loadClassifiers();
        boolean rebuildDataset = ConfigLoader.loadRebuildDataset();

        boolean activeFeatureSelection = ConfigLoader.loadFeatureSelection();
        boolean featureSelectionType = ConfigLoader.loadFeatureSelectionType();

        if (projectNames.isEmpty() || classifiersToTest.isEmpty()) {
            LOGGER.severe("Projects or Classifiers list is empty in config.properties. Exiting.");
            return;
        }

        for (String projectName : projectNames) {
            // Foreach project build the dataset only if rebuild dataset is true
            if (rebuildDataset) {
                EntryPointBoundary.startAnalysis(new EntryPointContext(projectName, ConfigLoader.getVersionDiscardPercentage(projectName)));
            }
            // Build a model with Weka using the dataset
            WekaBoundary.wekaBoundaryWork(projectName, classifiersToTest, activeFeatureSelection, featureSelectionType);
        }
    }
}
