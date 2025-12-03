package org.isw2.dataset.core.boundary;

public class Outcome {

    private String className;
    private String path;
    private String signature;
    private String version;
    private int linesOfCode;
    private int statementsCount;
    private int cyclomaticComplexity;
    private int cognitiveComplexity;
    private int vocabulary;
    private int programLength;
    private double estimatedProgramLength;
    private double volume;
    private double difficulty;
    private double effort;
    private int nestingDepth;
    private int numberOfBranchesAndDecisionPoint;
    private int parameterCount;
    private int codeSmellCounter;
    private long methodHistories;
    private int authors;
    private int stmtAdded;
    private int maxStmtAdded;
    private int stmtDeleted;
    private int maxStmtDeleted;
    private boolean buggy = false;

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

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public int getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(int vocabulary) {
        this.vocabulary = vocabulary;
    }

    public int getProgramLength() {
        return programLength;
    }

    public void setProgramLength(int programLength) {
        this.programLength = programLength;
    }

    public double getEstimatedProgramLength() {
        return estimatedProgramLength;
    }

    public void setEstimatedProgramLength(double estimatedProgramLength) {
        this.estimatedProgramLength = estimatedProgramLength;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getEffort() {
        return effort;
    }

    public void setEffort(double effort) {
        this.effort = effort;
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

    public int getCodeSmellCounter() {
        return codeSmellCounter;
    }

    public void setCodeSmellCounter(int codeSmellCounter) {
        this.codeSmellCounter = codeSmellCounter;
    }

    public long getMethodHistories() {
        return methodHistories;
    }

    public void setMethodHistories(long methodHistories) {
        this.methodHistories = methodHistories;
    }

    public int getAuthors() {
        return authors;
    }

    public void setAuthors(int authors) {
        this.authors = authors;
    }

    public int getStmtAdded() {
        return stmtAdded;
    }

    public void setStmtAdded(int stmtAdded) {
        this.stmtAdded = stmtAdded;
    }

    public int getMaxStmtAdded() {
        return maxStmtAdded;
    }

    public void setMaxStmtAdded(int maxStmtAdded) {
        this.maxStmtAdded = maxStmtAdded;
    }

    public int getStmtDeleted() {
        return stmtDeleted;
    }

    public void setStmtDeleted(int stmtDeleted) {
        this.stmtDeleted = stmtDeleted;
    }

    public int getMaxStmtDeleted() {
        return maxStmtDeleted;
    }

    public void setMaxStmtDeleted(int maxStmtDeleted) {
        this.maxStmtDeleted = maxStmtDeleted;
    }

    public boolean getBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isBuggy() {
        return buggy;
    }
}
