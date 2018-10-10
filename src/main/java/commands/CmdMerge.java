package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import repo.Branch;
import repo.Repo;
import repo.Utils;
import repo.objects.Blob;
import repo.objects.Commit;
import repo.objects.Tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Command(name = "merge", description = "merges other branch to HEAD; if there is no conflicts --> created new commit immediately " +
        "otherwise the commit without conflicts should be prepared before merge")
public class CmdMerge implements GitCommand {

    @Arguments(description = "other branch name", required = true)
    private String branchName = "";

    @Option(name = {"-m"}, description = "merge message", required = true)
    private String message;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        if (!repo.branches.containsKey(branchName)) {
            throw new Exception("There is no such branch " + branchName);
        } else if (!repo.head.detached() && repo.head.getBranch().getName().equals(branchName)) {
            throw new Exception("You are already on " + branchName);
        }

        Map<String, Blob> currentBlobs = repo.head.getAll();
        Branch otherBranch = repo.branches.get(branchName);
        Map<String, Blob> otherBlobs = otherBranch.getAll();

        // detecting potentially conflict files
        Set<String> candidatesRelativeFileNames = new HashSet<>(currentBlobs.keySet());
        candidatesRelativeFileNames.retainAll(otherBlobs.keySet());

        Map<String, Blob> conflictBlobs = new HashMap<>(); // relativeFileNames

        // detecting conflict files
        for (String candidateRelativeFileName : candidatesRelativeFileNames) {
            Blob blobA = currentBlobs.get(candidateRelativeFileName);
            Blob blobB = otherBlobs.get(candidateRelativeFileName);

            if (!blobA.sha.equals(blobB.sha)) {
                String conflictContent = "<<<<<<< HEAD\n" + blobA.repr() + "\n>>>>>>> " +
                        "\n<<<<<<< " + otherBranch.getName() + "\n" + blobB.repr() + "\n>>>>>>> ";

                // writing conflict content directly to file
                Path absoluteConflictFilePath = repo.trackingDir.resolve(candidateRelativeFileName);
                Path relativeConflictFilePath = repo.trackingDir.relativize(absoluteConflictFilePath);

                Utils.writeContent(absoluteConflictFilePath, conflictContent);
                Blob conflictBlob = new Blob(repo, relativeConflictFilePath);
                conflictBlobs.put(candidateRelativeFileName, conflictBlob);
            }
        }

        // just printing out information about conflicts
        if (conflictBlobs.size() > 0) {
            System.out.println("CONFLICT in files: ");
            for (Map.Entry<String, Blob> entry : conflictBlobs.entrySet()) {
                System.out.println("\t" + entry.getKey());
            }
            System.out.println("Conflicted files will be overwritten and committed. Pay attention to these files.");
        }

        // building merge-commit tree
        Map<String, Blob> nameBlobsToBuildTree = new HashMap<>(currentBlobs);
        nameBlobsToBuildTree.putAll(otherBlobs);
        nameBlobsToBuildTree.putAll(conflictBlobs);

        Map<Path, Blob> pathBlobsToBuildTree = new HashMap<>();
        for (Map.Entry<String, Blob> entry: nameBlobsToBuildTree.entrySet()){
            pathBlobsToBuildTree.put(Paths.get(entry.getKey()), entry.getValue());
        }

        Tree tree = new Tree(repo);
        tree.addAll(pathBlobsToBuildTree);

        // building merge-commit
        Set<String> parentCommits = new HashSet<>();
        parentCommits.add(repo.head.getCommit().sha);
        parentCommits.add(otherBranch.getCommit().sha);

        Commit newCommit = new Commit(repo, tree.sha, parentCommits, message);

        // moving head to merge-commit
        if (repo.head.detached()) {
            repo.head.moveToCommit(newCommit.sha);
        } else {
            Branch branch = repo.head.getBranch();
            branch.moveToCommit(newCommit.sha);
        }

        // checkout to current head
        CmdCheckout.checkoutToCommit(repo, newCommit.sha);

        return 0;
    }
}