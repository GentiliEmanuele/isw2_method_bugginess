package org.isw2.core.model;

import org.isw2.metrics.complexity.model.CodeSmell;

import java.util.ArrayList;
import java.util.List;

public class FileClass {
    private final String path;
    private final String content;
    private final List<Method> methods;
    private List<CodeSmell> smells;

    public FileClass(String className, String content) {
        this.path = className;
        this.content = content;
        this.methods = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public List<CodeSmell> getSmells() {
        return smells;
    }

    public void setSmells(List<CodeSmell> smells) {
        this.smells = smells;
    }

}
