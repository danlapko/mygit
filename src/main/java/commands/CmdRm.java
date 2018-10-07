package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import repo.Repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "rm", description = "remove files from index")
public class CmdRm implements GitCommand {
    private List<Path> absoluteFilePaths = new LinkedList<>();

    @Arguments(description = "Directories and file names to be removed", required = true)
    private List<String> dirtyFileNames;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
//      check and normalize dirty file names
        for (String fileName : dirtyFileNames) {
            Path filePath = Paths.get(fileName);
            Path absoluteFilePath;
            Path relativeFilePath;

            if (filePath.isAbsolute() && !filePath.startsWith(repo.repoDir)) {
                throw new IOException("file could not be tracked (out of repo) " + filePath.toString());
            }

            if (filePath.isAbsolute()) {
                absoluteFilePath = filePath;
                relativeFilePath = repo.trackingDir.relativize(absoluteFilePath);
            } else {
                absoluteFilePath = repo.trackingDir.resolve(filePath);
                relativeFilePath = filePath;
            }

            if (Files.isDirectory(absoluteFilePath)) {
                List<Path> dirFileNames = Files
                        .walk(absoluteFilePath)
                        .map(Path::normalize)
                        .filter(pth -> !pth.startsWith(repo.repoDir))
                        .filter(Files::isRegularFile)
                        .filter(Files::isReadable)
//                        .filter(pth -> (repo.index.contains(pth)))
                        .collect(Collectors.toList());
                absoluteFilePaths.addAll(dirFileNames);
            } else {
                absoluteFilePaths.add(absoluteFilePath);
            }


        }

//      remove relative file paths from index
        for (Path absoluteFilePath : absoluteFilePaths) {
            Path relativeFilePath = repo.trackingDir.relativize(absoluteFilePath);
            repo.index.remove(relativeFilePath);
        }

        return 0;
    }

}

