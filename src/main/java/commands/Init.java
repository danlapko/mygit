package commands;

import repo.Repo;

import io.airlift.airline.Command;

import java.nio.file.Path;

@Command(name = "init", description = "initializes empty wit repository")
public class Init implements GitCommand {

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        repo.initialize();
        return 0;
    }
}
