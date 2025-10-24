package org.isw2.complexity.model;

public class Metrics {
    private int linesOfCode;
    private int statementsCount;
    private int cyclomaticComplexity;
    private int cognitiveComplexity;
    private HalsteadComplexity halsteadComplexity;
    private int nestingDepth;
    private int numberOfBranchesAndDecisionPoint;
    private int parameterCount;

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

    public int getStatementsCount() {
        return statementsCount;
    }

    public void setStatementsCount(int statementsCount) {
        this.statementsCount = statementsCount;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getCognitiveComplexity() {
        return cognitiveComplexity;
    }

    public void setCognitiveComplexity(int cognitiveComplexity) {
        this.cognitiveComplexity = cognitiveComplexity;
    }

    public HalsteadComplexity getHalsteadComplexity() {
        return halsteadComplexity;
    }

    public void setHalsteadComplexity(HalsteadComplexity halsteadComplexity) {
        this.halsteadComplexity = halsteadComplexity;
    }

    public int getNestingDepth() {
        return nestingDepth;
    }

    public void setNestingDepth(int nestingDepth) {
        this.nestingDepth = nestingDepth;
    }

    public int getNumberOfBranchesAndDecisionPoint() {
        return numberOfBranchesAndDecisionPoint;
    }

    public void setNumberOfBranchesAndDecisionPoint(int numberOfBranchesAndDecisionPoint) {
        this.numberOfBranchesAndDecisionPoint = numberOfBranchesAndDecisionPoint;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public void setParameterCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }
}
