package org.isw2.weka.procedure;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.factory.SplitterFactory;
import org.isw2.weka.utils.context.SplitterContext;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.List;

public interface FeatureSelection {
    Instances selectFeatures(Instances training) throws ProcessingException;

    static List<Instances> splitData(Instances data, double splittingPercentage) throws ProcessingException {
        AbstractControllerFactory<SplitterContext, List<Instances>> splitterFactory = new SplitterFactory();
        return splitterFactory.process(new SplitterContext(splittingPercentage, data));
    }

    static Instances removeIds(Instances data) throws ProcessingException {
        Remove remove = new Remove();
        remove.setAttributeIndices("1-5");
        try {
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    static Instances setIndexToBeRemoved(int i, Instances training) throws Exception {
        Remove remove = new Remove();
        remove.setAttributeIndicesArray(new int[]{i});
        remove.setInputFormat(training);
        return Filter.useFilter(training, remove);
    }
}
