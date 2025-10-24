package org.isw2.complexity_and_smell_metrics.model;

public class Method {
    private String className;
    private String signature;
    private final Metrics metrics;

    public Method() {
        metrics = new Metrics();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    public Metrics getMetrics() {
        return metrics;
    }
}
