package org.isw2.git.model;

public class MyEdit {
    private int oldStart;
    private int oldEnd;
    private int newStart;
    private int newEnd;

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
}
