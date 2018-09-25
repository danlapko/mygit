package commands;

import repo.Repo;

import java.nio.file.Path;

public interface GitCommand {


    int execute(Repo repo, Path workingDir) throws Exception;
}
