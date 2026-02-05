package org.isw2.weka.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.*;

public class ClassifierFactory {

    private ClassifierFactory() {}

    public static Classifier createClassifier(ClassifierType classifierType) {
        return switch (classifierType) {
            case DECISION_STUMP -> new DecisionStump();
            case DECISION_TABLE -> new DecisionTable();
            case IBK -> new IBk();
            case J48 -> new J48();
            case J_RIP -> new JRip();
            case K_STAR -> new KStar();
            case NAIVE_BAYES -> new NaiveBayes();
            case ONE_R -> new OneR();
            case PART -> new PART();
            case RANDOM_FOREST -> new RandomForest();
            case RANDOM_TREE -> new RandomTree();
            case REP_TREE -> new REPTree();
            case SMO -> new SMO();
        };
    }
}
