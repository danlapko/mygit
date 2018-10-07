package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import repo.Repo;
import repo.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "add", description = "adds files to index")
public class CmdAdd implements GitCommand {

    @Arguments(description = "Directories and file names to be added", required = true)
    private List<String> dirtyFileNames;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
//      check and normalize dirty file names
        List<Path> absoluteFilePaths = Utils.dirtyFileNamesToPaths(repo, dirtyFileNames);


//      add relative file paths to index
        for (Path absoluteFilePath : absoluteFilePaths) {
            Path relativeFilePath = repo.trackingDir.relativize(absoluteFilePath);
            repo.index.add(relativeFilePath);
        }

        return 0;
    }

}
