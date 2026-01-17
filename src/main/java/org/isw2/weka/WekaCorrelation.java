package org.isw2.weka;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.model.Correlation;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WekaCorrelation implements Controller<String, List<Correlation>> {

    @Override
    public List<Correlation> execute(String projectName) throws ProcessingException {
        // Load data from CSV
        Instances data = loadData(projectName);

        // Set the class to be predicted
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        // Build evaluator
        CorrelationAttributeEval eval = buildEvaluator(data);

        List<Correlation> correlations = new ArrayList<>();
        for (int i = 0; i < data.numAttributes() - 1; i++) {
            double corr = computeCorrelation(i, eval);
            correlations.add(new Correlation(data.attribute(i).name(), corr));
        }
        return correlations;
    }

    private Instances loadData(String projectName) throws ProcessingException {
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("output/" + projectName + ".csv"));
            return loader.getDataSet();
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private CorrelationAttributeEval buildEvaluator(Instances data) throws ProcessingException {
        try {
            CorrelationAttributeEval eval = new CorrelationAttributeEval();
            eval.buildEvaluator(data);
            return eval;
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private double computeCorrelation(int attributeIndex, CorrelationAttributeEval eval) throws ProcessingException {
        try {
            return eval.evaluateAttribute(attributeIndex);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }
}
