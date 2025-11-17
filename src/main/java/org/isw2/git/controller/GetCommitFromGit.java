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
import org.isw2.core.controller.context.EntryPointContext;
import org.isw2.git.controller.context.GetCommitFromGitContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ExecutionContext;
import org.isw2.git.model.Author;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetCommitFromGit implements Controller {
    private final List<Commit> cleanedCommits = new ArrayList<>();
    private Map<String, List<Change>> diffCache = new HashMap<>();

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof GetCommitFromGitContext(String projectName, GitController gitController))) {
            throw new IllegalArgumentException("Required params: GetCommitFromGitContext. Received: " +
                    (context != null ? context.getClass().getSimpleName() : "null"));
        }
        try {
            getCommitFromGit(projectName, gitController);
        } catch (GitAPIException | IOException | ProcessingException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    public List<Commit> getCommits() {
        return cleanedCommits;
    }

    private void getCommitFromGit(String projectName, GitController gitController) throws GitAPIException, IOException, ProcessingException {
        gitController.execute(new EntryPointContext(projectName));
        Git git = gitController.getGit();
        Iterable<RevCommit> commits = git.log().all().call();
        int length = getCommitsLength(commits);
        commits = git.log().all().call();
        int processed = 0;
        for (RevCommit commit : commits) {
            String commitId = commit.getId().getName();
            String name = commit.getAuthorIdent().getName();
            String authorEmail = commit.getAuthorIdent().getEmailAddress();
            Author author = new Author();
            author.setName(name);
            author.setAuthorEmail(authorEmail);
            Commit newCommit = new Commit();
            newCommit.setId(commitId);
            newCommit.setAuthor(author);
            newCommit.setCommitTime(formatDate(commit.getAuthorIdent().getWhenAsInstant().toString()));

            if (commit.getParents().length != 0) {
                RevCommit prevCommit = commit.getParent(0);
                String key = prevCommit.getName() + "->" + commit.getName();
                if (!diffCache.containsKey(key)) {
                    diffCache.put(key, getFileDiffBetweenCommit(git, prevCommit, commit));
                }
                newCommit.setChanges(diffCache.get(key));
            }
            processed++;
            int percent = 0;
            if (length != 0) percent = (processed * 100) / length;
            System.out.print("\rGet commit from git: progress: " + percent + "%");

            cleanedCommits.add(newCommit);
        }
        System.out.println();
    }

    private static int getCommitsLength(Iterable<RevCommit> commits) {
        int lenght = 0;
        for (var _ : commits) {
            lenght++;
        }
        return lenght;
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
