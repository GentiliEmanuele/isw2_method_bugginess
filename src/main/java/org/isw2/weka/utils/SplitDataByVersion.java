package org.isw2.weka.utils;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.weka.utils.context.SplitDataByVersionContext;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class SplitDataByVersion implements Controller<SplitDataByVersionContext, List<Instances>> {
    @Override
    public List<Instances> execute(SplitDataByVersionContext context) throws ProcessingException {
        List<Instances> releases = new ArrayList<>();

        // Find index of release id column
        Attribute versionAttr = context.data().attribute(context.colVersionName());
        if (versionAttr == null) {
            throw new ProcessingException("Version attribute not found");
        }
        int versionIdx = versionAttr.index();

        // Prepare variable for the cycle
        Instances currentRelease = new Instances(context.data(), 0);
        String previousVersionVal = null;

        // Iterate on each row
        for (int i = 0; i < context.data().numInstances(); i++) {
            // Get i-th row
            Instance inst = context.data().instance(i);

            // Get version value as string
            String currentVersionVal = inst.stringValue(versionIdx);

            // If is the first version init the tracker
            if (previousVersionVal == null) {
                previousVersionVal = currentVersionVal;
            }

            // If the version change save the current dataset and open a newer
            if (!currentVersionVal.equals(previousVersionVal)) {
                // Add the completed release to the list
                releases.add(currentRelease);

                // Prepare the new container for the new release
                currentRelease = new Instances(context.data(), 0);
                previousVersionVal = currentVersionVal;
            }

            // Add the current row to the current release list
            currentRelease.add(inst);
        }

        // Add the last opened release
        if (currentRelease.numInstances() > 0) {
            releases.add(currentRelease);
        }
        return releases;
    }
}
