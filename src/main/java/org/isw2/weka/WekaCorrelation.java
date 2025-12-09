package org.isw2.weka;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

import java.io.File;

public class WekaCorrelation {
    public static void main(String[] args) {
        try {

            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("output/" + "BOOKKEEPER" + ".csv"));
            Instances data = loader.getDataSet();


            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            Resample resample = new Resample();
            resample.setInputFormat(data);

            resample.setSampleSizePercent(40.0);
            resample.setNoReplacement(false);
            Instances sampledData = Filter.useFilter(data, resample);

            CorrelationAttributeEval eval = new CorrelationAttributeEval();

            Ranker search = new Ranker();

            AttributeSelection attsel = new AttributeSelection();
            attsel.setEvaluator(eval);
            attsel.setSearch(search);

            attsel.SelectAttributes(sampledData);

            int[] indices = attsel.selectedAttributes();

            System.out.println("Ranking of attribute correlation:\n");

            for (int i = 0; i < indices.length; i++) {
                int attrIndex = indices[i];

                if (attrIndex != data.classIndex()) {

                    double corr = eval.evaluateAttribute(attrIndex);

                    String attrName = data.attribute(attrIndex).name();

                    System.out.printf("%-20s : %.4f%n", attrName, corr);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
