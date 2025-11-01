package org.isw2.changes.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.isw2.changes.model.Change;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetFileDiffBetweenCommit {
    public List<Change> getFileDiffBetweenCommit(Git git, RevCommit startCommit, RevCommit endCommit) throws IOException, GitAPIException {
        Repository repository = git.getRepository();
        ObjectId oldHead = startCommit.getTree().getId();
        ObjectId head =  endCommit.getTree().getId();
        List<Change> changes = new ArrayList<>();
        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        formatter.setRepository(repository);
        formatter.setDiffComparator(RawTextComparator.DEFAULT);
        formatter.setDetectRenames(true);
        // Prepare the two iterator to compute the diff between commit
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
            for (DiffEntry entry : diffs) {
                if ((entry.getOldPath() != null && entry.getOldPath().endsWith(".java"))  || (entry.getNewPath() != null && entry.getNewPath().endsWith(".java"))) {
                    Change change = new Change();
                    change.setType(entry.getChangeType().name());
                    change.setOldPath(entry.getOldPath());
                    change.setNewPath(entry.getNewPath());
                    // This is for capture touched method in a class
                    FileHeader fileHeader = formatter.toFileHeader(entry);
                    List<HunkHeader> hunks = new ArrayList<>(fileHeader.getHunks());
                    for (HunkHeader hunk : hunks) {
                        EditList edits = hunk.toEditList();
                        for (Edit edit : edits) {
                            change.setOldStart(edit.getBeginA());
                            change.setOldEnd(edit.getEndA());
                            change.setNewStart(edit.getBeginB());
                            change.setNewEnd(edit.getEndB());
                        }
                    }
                    changes.add(change);
                }
            }
            return changes;
        }

    }
}
