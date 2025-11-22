package org.isw2.core.model;

import org.isw2.metrics.model.CodeSmell;

import java.util.List;

public class FileClass {
    private final String path;
    private final String content;
    private List<Method> methods;
    private List<CodeSmell> smells;

    public FileClass(String className, String content) {
        this.path = className;
        this.content = content;
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

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public void setSmells(List<CodeSmell> smells) {
        this.smells = smells;
    }

}
