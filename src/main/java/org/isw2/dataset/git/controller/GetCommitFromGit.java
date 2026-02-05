package org.isw2.dataset.git.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.git.model.Author;
import org.isw2.dataset.git.model.Change;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.git.model.MyEdit;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
        Repository repository = git.getRepository();
        ObjectId head = repository.resolve("HEAD");
        if (head == null) {
            return;
        }
        Iterable<RevCommit> commits = git.log().add(head).call();
        for (RevCommit commit : commits) {
            Author author = new Author(commit.getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress());
            if (commit.getParents().length != 0) {
                if (commit.getParentCount() > 1) {
                    continue;
                }
                RevCommit prevCommit = (commit.getParentCount() > 0) ? commit.getParent(0) : null;
                Commit newCommit = new Commit(
                        commit.getId().getName(),
                        author, formatDate(commit.getAuthorIdent().getWhenAsInstant().toString()), commit.getFullMessage(),
                        getFileDiffBetweenCommit(git, prevCommit, commit));
                if (!newCommit.changes().isEmpty()) {
                    cleanedCommits.add(newCommit);
                }
            }
        }
        cleanedCommits.sort(Comparator.comparing(commit -> LocalDate.parse(commit.commitTime())));
    }


    private static String formatDate(String dateTime) {
        OffsetDateTime odt = OffsetDateTime.parse(dateTime);
        return odt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private List<Change> getFileDiffBetweenCommit(Git git, RevCommit startCommit, RevCommit endCommit) throws IOException {
        Repository repository = git.getRepository();
        ObjectId oldTree = startCommit.getTree();
        ObjectId newTree =  endCommit.getTree();
        List<Change> changes = new ArrayList<>();

        try (DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            formatter.setRepository(repository);
            formatter.setDiffComparator(RawTextComparator.DEFAULT);
            formatter.setDetectRenames(true);

            List<DiffEntry> diffs = formatter.scan(oldTree, newTree);
            for (DiffEntry entry : diffs) {
                boolean isJava = (entry.getOldPath() != null && isAJavaFile(entry.getOldPath())) ||
                        (entry.getNewPath() != null && isAJavaFile(entry.getNewPath()));

                if (isJava) {
                    Change change = new Change();
                    change.setType(entry.getChangeType().name());
                    change.setOldPath(entry.getOldPath());
                    change.setNewPath(entry.getNewPath());

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
        }
        return changes;
    }

    private boolean isAJavaFile(String path) {
        return path.endsWith(".java") && !path.endsWith("package-info.java");
    }
}
