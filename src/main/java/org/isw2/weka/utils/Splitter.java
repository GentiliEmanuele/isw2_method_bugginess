package org.isw2.weka.utils;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.utils.context.SplitterContext;
import weka.core.Instances;

import java.util.List;

public class Splitter implements Controller<SplitterContext, List<Instances>> {
    @Override
    public List<Instances> execute(SplitterContext context) throws ProcessingException {
        // Compute the size of the two set
        int set1 = (int) Math.round(context.data().numInstances() * context.splittingPercentage());
        int set2 = context.data().numInstances() - set1;

        Instances set1Data = new Instances(context.data(), 0, set1);
        Instances set2Data = new Instances(context.data(), set1, set2);
        return List.of(set1Data, set2Data);
    }
}
