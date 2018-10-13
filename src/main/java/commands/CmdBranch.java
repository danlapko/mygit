package commands;

import exceptions.BranchAlreadyExistsException;
import exceptions.BranchNotExistsException;
import exceptions.CommandStateException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import repo.Branch;
import repo.Repo;

import java.nio.file.Files;
import java.nio.file.Path;

@Command(name = "branch", description = "creates new branch at HEAD or deletes branch")
public class CmdBranch implements GitCommand {

    @Arguments(description = "branch name", required = true)
    private String branchName = "";

    @Option(name = {"-d"}, description = "deleting branch", required = false)
    private boolean deleting = false;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        if (repo.branches.containsKey(branchName) && !deleting) {
            throw new BranchAlreadyExistsException(branchName);
        } else if (!repo.branches.containsKey(branchName) && deleting) {
            throw new BranchNotExistsException(branchName);
        }

        if (!deleting) {
            Branch newBranch = new Branch(repo, branchName, repo.head.getCommit().sha);
            repo.branches.put(branchName, newBranch);
        } else {
            if (repo.head.getBranch() != null && repo.head.getBranch().getName().equals(branchName)) {
                throw new CommandStateException("HEAD currently on " + branchName + ". You can't delete it.");
            }
            repo.branches.remove(branchName);
            Path pathToRemove = repo.branchesDir.resolve(branchName);
            Files.deleteIfExists(pathToRemove);

        }
        return 0;
    }
}
