package org.isw2.complexity_and_smell_metrics.model;

public class HalsteadComplexity {
    private int vocabulary = 0;
    private int programLength = 0;
    private double estimatedProgramLength = 0;
    private double volume = 0;
    private double difficulty = 0;
    private double effort = 0;

    public double getEffort() {
        return effort;
    }

    public void setEffort(double effort) {
        this.effort = effort;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getEstimatedProgramLength() {
        return estimatedProgramLength;
    }

    public void setEstimatedProgramLength(double estimatedProgramLength) {
        this.estimatedProgramLength = estimatedProgramLength;
    }

    public int getProgramLength() {
        return programLength;
    }

    public void setProgramLength(int programLength) {
        this.programLength = programLength;
    }

    public int getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(int vocabulary) {
        this.vocabulary = vocabulary;
    }

    @Override
    public String toString() {
        return "HalsteadComplexity{" +
                "vocabulary=" + vocabulary +
                ", programLength=" + programLength +
                ", estimatedProgramLength=" + estimatedProgramLength +
                ", volume=" + volume +
                ", difficulty=" + difficulty +
                ", effort=" + effort +
                '}';
    }
}
