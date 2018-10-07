package repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static List<Path> dirtyFileNamesToPaths(Repo repo, List<String> dirtyFileNames) throws IOException {
        List<Path> absoluteFilePaths = new LinkedList<>();
        if (dirtyFileNames == null)
            return absoluteFilePaths;
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

        return absoluteFilePaths;
    }

    public static String readFileContent(Path absolutePath) throws IOException {
        List<String> lines;
        String content;

        try {
            lines = Files.readAllLines(absolutePath);
        } catch (IOException e) {
            System.out.println("Error: not text file " + absolutePath.toString());
            throw e;
        }

        content = String.join("\n", lines);
        return content;
    }

    public static List<String> readFileContentList(Path absolutePath) throws IOException {
        List<String> lines;

        try {
            lines = Files.readAllLines(absolutePath);
        } catch (IOException e) {
            System.out.println("Error: not text file " + absolutePath.toString());
            throw e;
        }

        return lines;
    }

    public static void writeContent(Path absolutePath, String content) throws IOException {
        Files.write(absolutePath, Arrays.asList(content.split("\n")));
    }

    public static void writeContent(Path absolutePath, List<String> list) throws IOException {
        Files.write(absolutePath, list);
    }
}
