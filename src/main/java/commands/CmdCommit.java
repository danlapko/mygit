package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import repo.Branch;
import repo.Repo;
import repo.objects.Commit;
import repo.objects.Tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Command(name = "commit", description = "commit files from index to current branch")
public class CmdCommit implements GitCommand {

    @Arguments(description = "commit message", required = true)
    private String message;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        Tree tree = repo.index.getTree();

        Set<String> parentCommits = new HashSet<>();
        parentCommits.add(repo.head.getCommit().sha);

        Commit commit = new Commit(repo, tree.sha, parentCommits, message);

        if (repo.head.detached()) {
            repo.head.moveToCommit(commit.sha);
        } else {
            Branch branch = repo.head.getBranch();
            branch.moveToCommit(commit.sha);
        }
        return 0;
    }
}
