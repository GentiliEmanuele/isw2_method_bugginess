package org.isw2.git.controller;

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
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.git.model.Author;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;
import org.isw2.git.model.MyEdit;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GetCommitFromGit implements Controller<String, List<Commit>> {
    private final List<Commit> cleanedCommits = new ArrayList<>();
    private Git git;

    @Override
    public List<Commit> execute(String projectName) throws ProcessingException {
        try {
            this.git = cloneRepository(projectName);
            getCommitFromGit();
            return cleanedCommits;
        } catch (GitAPIException | IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    public static Git cloneRepository(String projectName) throws GitAPIException, IOException {
        File dir = new File(formatDirectoryName(projectName));
        if (!dir.exists()) {
            return Git.cloneRepository().setURI(formatGitHubURL(projectName)).setDirectory(dir).call();
        } else {
            return Git.open(dir);
        }
    }

    private static String formatDirectoryName(String projectName) {
        String baseDir = "/home/emanuele/isw2/temp/";
        return baseDir.concat(projectName);
    }

    private static String formatGitHubURL(String projectName) {
        String url = "https://github.com/apache/";
        return url.concat(projectName).concat(".git");
    }

    private void getCommitFromGit() throws GitAPIException, IOException {
        Iterable<RevCommit> commits = git.log().all().call();
        for (RevCommit commit : commits) {
            Author author = new Author(commit.getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress());
            if (commit.getParents().length != 0) {
                if (commit.getParentCount() > 1 || commit.getParentCount() == 0) {
                    continue;
                }
                RevCommit prevCommit = commit.getParent(0);
                Commit newCommit = new Commit(
                        commit.getId().getName(),
                        author, formatDate(commit.getAuthorIdent().getWhenAsInstant().toString()), commit.getFullMessage(),
                        getFileDiffBetweenCommit(git, prevCommit, commit));
                cleanedCommits.add(newCommit);
            }
        }
    }


    private static String formatDate(String dateTime) {
        OffsetDateTime odt = OffsetDateTime.parse(dateTime);
        return odt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private List<Change> getFileDiffBetweenCommit(Git git, RevCommit startCommit, RevCommit endCommit) throws IOException, GitAPIException {
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
                            MyEdit myEdit = new MyEdit();
                            myEdit.setOldStart(edit.getBeginA());
                            myEdit.setOldEnd(edit.getEndA());
                            myEdit.setNewStart(edit.getBeginB());
                            myEdit.setNewEnd(edit.getEndB());
                            change.getEdits().add(myEdit);
                        }
                    }
                    changes.add(change);
                }
            }
            return changes;
        }
    }
}
