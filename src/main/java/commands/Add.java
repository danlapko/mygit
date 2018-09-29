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

@Command(name = "add", description = "adds files to index")
public class Add implements GitCommand {
    private List<Path> absoluteFilePaths = new LinkedList<>();

    @Arguments(description = "Directories and file names to be added", required = true)
    private List<String> dirtyFileNames;

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
//      check and normalize dirty file names
        for (String fileName : dirtyFileNames) {
            Path filePath = Paths.get(fileName);
            Path absoluteFilePath;


            if (filePath.isAbsolute() && !filePath.startsWith(repo.repoDir)) {
                throw new IOException("file could not be tracked (out of repo) " + filePath.toString());
            }

            if (filePath.isAbsolute()) {
                absoluteFilePath = filePath;
            } else {
                absoluteFilePath = repo.trackingDir.resolve(filePath);
            }

            if (!Files.exists(absoluteFilePath)) {
                throw new IOException("file does not exists " + filePath.toString());
            }

            if (Files.isDirectory(absoluteFilePath)) {
                List<Path> dirFileNames = Files
                        .walk(absoluteFilePath)
                        .map(Path::normalize)
                        .filter(pth -> !pth.startsWith(repo.repoDir))
                        .filter(Files::isRegularFile)
                        .filter(Files::isReadable)
                        .collect(Collectors.toList());
                absoluteFilePaths.addAll(dirFileNames);
            } else {
                absoluteFilePaths.add(absoluteFilePath);
            }


        }

//      add relative file paths to index
        for (Path absoluteFilePath : absoluteFilePaths) {
            Path relativeFilePath = repo.trackingDir.relativize(absoluteFilePath);
            repo.index.add(relativeFilePath);
        }

        return 0;
    }

}
