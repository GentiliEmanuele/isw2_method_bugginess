package org.isw2.git.model;

import java.util.ArrayList;
import java.util.List;

public class Change {
    private String type;
    private String oldPath;
    private String newPath;
    private final List<MyEdit> edits;

    public Change() {
        this.edits = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public List<MyEdit> getEdits() {
        return edits;
    }

}
