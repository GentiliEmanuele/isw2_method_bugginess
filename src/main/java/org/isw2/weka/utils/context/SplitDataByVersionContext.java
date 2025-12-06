package org.isw2.weka.utils.context;

import weka.core.Instances;

public record SplitDataByVersionContext(Instances data, String colVersionName) {
}
