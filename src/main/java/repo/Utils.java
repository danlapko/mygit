package repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Utils {
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
