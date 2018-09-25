package commands;

import repo.Repo;

import java.nio.file.Path;

public class Commit implements GitCommand {
    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {

        return 0;
    }
}
