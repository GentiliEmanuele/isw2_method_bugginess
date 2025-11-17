package org.isw2.git.model;

public class Change {
    private String type;
    private String oldPath;
    private String newPath;
    private int oldStart;
    private int oldEnd;
    private int newStart;
    private int newEnd;

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

    public int getOldStart() {
        return oldStart;
    }

    public void setOldStart(int oldStart) {
        this.oldStart = oldStart;
    }

    public int getOldEnd() {
        return oldEnd;
    }

    public void setOldEnd(int oldEnd) {
        this.oldEnd = oldEnd;
    }

    public int getNewStart() {
        return newStart;
    }

    public void setNewStart(int newStart) {
        this.newStart = newStart;
    }

    public int getNewEnd() {
        return newEnd;
    }

    public void setNewEnd(int newEnd) {
        this.newEnd = newEnd;
    }

    @Override
    public String toString() {
        return "Change{" +
                "type='" + type + '\'' +
                ", oldPath='" + oldPath + '\'' +
                ", newPath='" + newPath + '\'' +
                ", oldStart=" + oldStart +
                ", oldEnd=" + oldEnd +
                ", newStart=" + newStart +
                ", newEnd=" + newEnd +
                '}';
    }
}
