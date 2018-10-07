package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import repo.Branch;
import repo.Index;
import repo.Repo;
import repo.Utils;
import repo.objects.Blob;
import repo.objects.Commit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Command(name = "checkout", description = "checkout head, index and working tree to revision or branchName (analog of `git checkout revision/branchName`)" +
        "or if `--` provided overwrites files in working dir by files from index")
public class CmdCheckout implements GitCommand {

    @Arguments(description = "revision or branchName to checkout", required = false)
    private String pointer = "";

    @Option(name = {"--"}, description = "list of files in file mode", required = false)
    private List<String> dirtyFileNames;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        //      check and normalize dirty file names
        List<Path> absoluteFilePaths = Utils.dirtyFileNamesToPaths(repo, dirtyFileNames);

        if (pointer.equals("") && absoluteFilePaths.size() == 0) {
            throw new Exception("should be given either pointer or -- with file names");
        } else if (!pointer.equals("") && !(absoluteFilePaths.size() == 0)) {
            throw new Exception("should be given either pointer or -- with file names");
        }

        if (!pointer.equals("")) {
            Branch oldBranch = repo.head.getBranch();
            Branch newBranch = oldBranch;
            if (repo.branches.containsKey(pointer)) {
                newBranch = repo.branches.get(pointer);
            }

            Index oldIndex = repo.index;
            Commit oldCommit = repo.head.getCommit();
            Map<String, Blob> oldBlobs = oldCommit.getAll();

            Commit newCommit;
            if (oldBranch == newBranch) {
                newCommit = new Commit(repo, pointer);
            } else {
                newCommit = newBranch.getCommit();
            }

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

            // ======== move head to revision/branch ===========

            if (oldBranch == newBranch) {
                repo.head.moveToCommit(newCommit.sha);
            } else {
                repo.head.moveToBranch(pointer);
            }

            // ======== reset index ===========
            repo.index = newIndex;


        } else {      // if `--` provided overwrites files in working dir by files from index
            for (Path absolutePath : absoluteFilePaths) {
                Path relativePath = repo.trackingDir.relativize(absolutePath);
                Blob blob = repo.index.get(relativePath);
                Utils.writeContent(absolutePath, blob.repr());
            }

        }


        // Map<String, INDEX_HEAD_STATUS> indexHeadStatuses = repo.index.getIndexHeadStatuses();
        return 0;
    }
}