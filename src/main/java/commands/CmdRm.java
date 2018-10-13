package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import repo.Repo;
import repo.Utils;

import java.nio.file.Path;
import java.util.List;

@Command(name = "rm", description = "remove files from index")
public class CmdRm implements GitCommand {

    @Arguments(description = "Directories and file names to be removed", required = true)
    private List<String> dirtyFileNames;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        // check and normalize dirty file names
        List<Path> absoluteFilePaths = Utils.dirtyFileNamesToPathsInWorkDir(repo, dirtyFileNames);


        // remove relative file paths from index
        for (Path absoluteFilePath : absoluteFilePaths) {
            Path relativeFilePath = repo.trackingDir.relativize(absoluteFilePath);
            repo.index.remove(relativeFilePath);
        }

        return 0;
    }

}

