package commands;

import io.airlift.airline.Command;
import repo.Repo;

import java.nio.file.Path;

@Command(name = "commit", description = "commit files from index to current branch")
public class Commit implements GitCommand {

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        repo.init();
        return 0;
    }
}
