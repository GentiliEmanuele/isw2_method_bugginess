package org.isw2.dataset.core.controller;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.context.TouchedByContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.git.model.Change;
import org.isw2.dataset.git.model.Commit;

public class FileIsTouchedBy implements Controller<TouchedByContext, Boolean> {
    @Override
    public Boolean execute(TouchedByContext context) throws ProcessingException {
        return fileIsTouchedBy(context.commit(), context.classPath());
    }

    private boolean fileIsTouchedBy(Commit commit, String classPath) {
        if (commit.changes() == null) return false;
        boolean isTouched = false;
        for (Change change : commit.changes()) {
            isTouched = isTouchedByAdd(change, classPath) ||
                    isTouchedByModify(change, classPath) ||
                    isTouchedByDelete(change, classPath) ||
                    isTouchedByRename(change, classPath) ||
                    isTouchedByCopy(change, classPath);
            if (isTouched) return true;
        }
        return isTouched;
    }

    private boolean isTouchedByAdd(Change change, String classPath) {
        return change.getType().equals("ADD") && change.getNewPath().equals(classPath);
    }

    private boolean isTouchedByModify(Change change, String classPath) {
        return change.getType().equals("MODIFY") && change.getNewPath().equals(classPath);
    }

    private boolean isTouchedByDelete(Change change, String classPath) {
        return change.getType().equals("DELETE") && change.getOldPath().equals(classPath);
    }

    private boolean isTouchedByRename(Change change, String classPath) {
        return change.getType().equals("RENAME") && change.getNewPath().equals(classPath);
    }

    private boolean isTouchedByCopy(Change change, String classPath) {
        return change.getType().equals("COPY") && change.getNewPath().equals(classPath);
    }
}
