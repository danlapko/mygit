package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import repo.*;
import repo.objects.Blob;
import repo.objects.Commit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Command(name = "reset", description = "reset head, branch tip, index and working tree to revision (analog of `git reset --hard revision`)")
public class CmdReset implements GitCommand {

    @Arguments(description = "revision to reset", required = true)
    private String revisionSha;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        if (repo.head.detached()) {
            throw new Exception("FORBIDDEN to reset in detached state");
        }
        if (!repo.head.getCommit().checkRevisionIsAncestor(revisionSha)) {
            throw new Exception("There is no such revision in head ancestors");
        }

        // Map<String, INDEX_HEAD_STATUS> indexHeadStatuses = repo.index.getIndexHeadStatuses();

        Branch branch = repo.head.getBranch();

        Index oldIndex = repo.index;
        Commit oldCommit = branch.getCommit();
        Map<String, Blob> oldBlobs = oldCommit.getAll();

        Commit newCommit = new Commit(repo, revisionSha);
        Map<String, Blob> newBlobs = newCommit.getAll();
        Index newIndex = new Index(repo, newCommit.getTree());

        // =========== reset working dir ============

        // remove files from working dir (files mentioned in oldCommit)
        for (String relativeFileName : oldBlobs.keySet()) {
            Path absoluteFilePath = repo.trackingDir.resolve(relativeFileName);
            Files.deleteIfExists(absoluteFilePath);
        }

        // remove files from working dir (files mentioned in newCommit)
        for (String relativeFileName : newBlobs.keySet()) {
            Path absoluteFilePath = repo.trackingDir.resolve(relativeFileName);
            Files.deleteIfExists(absoluteFilePath);
        }

        // create files in working dir (files mentioned in newCommit)
        for (Map.Entry<String, Blob> entry : newBlobs.entrySet()) {
            Path absoluteFilePath = repo.trackingDir.resolve(entry.getKey());
            Blob blob = entry.getValue();
            Utils.writeContent(absoluteFilePath, blob.repr());
        }

        // ======== reset branch tip (and automatically update head because head is not detached)===========
        branch.moveToCommit(newCommit.sha);

        // ======== reset index ===========
        repo.index = newIndex;

        return 0;
    }

}