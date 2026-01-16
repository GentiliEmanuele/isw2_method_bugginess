package org.isw2.experiment;

import org.isw2.weka.classifier.ClassifierType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final String CONFIG_FILE = "config.properties";

    private ConfigLoader() {}

    private static Properties loadProperties() {
        Properties prop = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            prop.load(input);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error reading config file", ex);
        }
        return prop;
    }

    public static boolean loadRebuildDataset() {
        Properties prop = loadProperties();
        return Boolean.parseBoolean(prop.getProperty("rebuildDataset"));
    }

    public static boolean loadFeatureSelection() {
        Properties prop = loadProperties();
        return Boolean.parseBoolean(prop.getProperty("featureSelection.active"));
    }

    public static boolean loadFeatureSelectionType() {
        Properties prop = loadProperties();
        return Boolean.parseBoolean(prop.getProperty("featureSelection.backward"));
    }

    public static List<ClassifierType> loadClassifiers() {
        List<ClassifierType> classifierList = new ArrayList<>();
        Properties prop = loadProperties();

        // Read classifiers properties
        String classifiersString = prop.getProperty("classifiers");

        if (classifiersString != null && !classifiersString.isEmpty()) {
            // Separate string by comma
            String[] classifierNames = classifiersString.split(",");
            for (String name : classifierNames) {
                cleanAndCast(classifierList, name);
            }
        }
        return classifierList;
    }

    private static void cleanAndCast(List<ClassifierType> classifierList, String name) {
        try {
            // Clean the string and cast to enum
            ClassifierType type = ClassifierType.valueOf(name.trim().toUpperCase());
            classifierList.add(type);
        } catch (IllegalArgumentException _) {
            LOGGER.log(Level.WARNING, "Classifier not found in Enum: {0}. Skipping...", name);
        }
    }

    public static List<String> loadProjects() {
        List<String> projectList = new ArrayList<>();
        Properties prop = loadProperties();
        String projectsString = prop.getProperty("projects");

        if (projectsString != null && !projectsString.isEmpty()) {
            for (String name : projectsString.split(",")) {
                if (!name.trim().isEmpty()) {
                    projectList.add(name.trim());
                }
            }
        }
        return projectList;
    }

    private static String getProperty(String projectName, String key) {
        Properties props = loadProperties();

        // Search if there is a specific value for the specific project
        String specificKey = projectName.toUpperCase() + "." + key;
        String value = props.getProperty(specificKey);

        // If there isn't use the default value
        if (value == null) {
            value = props.getProperty(key);
        }

        return value;
    }

    public static double getVersionDiscardPercentage(String projectName) {
        return Double.parseDouble(getProperty(projectName, "version.discard.percentage"));
    }

}
