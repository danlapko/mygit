package repo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Branch {
    private final Repo repo;
    private final String name;
    private final String commitSha;

    public Branch(Repo repo, String name) throws IOException {
        this.repo = repo;
        this.name = name;

        Path path = this.repo.branchesDir.resolve(this.name);
        if (!Files.exists(path)) {
            commitSha = repo.head.getCommitSha();
        } else {
            commitSha = Files.readAllLines(path, StandardCharsets.UTF_8).get(0);
        }
    }

    public Branch(Repo repo, String name, String commitSha) throws Exception {
        this.repo = repo;
        this.name = name;
        this.commitSha = commitSha;

        Path path = this.repo.branchesDir.resolve(this.name);
        if (!Files.exists(path)) {
            Files.createFile(path);
            Files.write(path, Arrays.asList(commitSha.split("\n")));
        } else {
            String currentCommitSha = Files.readAllLines(path, StandardCharsets.UTF_8).get(0);
            if (this.commitSha!= currentCommitSha)
                throw new Exception(); // branch already exists and branch head differs from commitSha
        }
    }


    public String getName() {
        return name;
    }

    public String getCommitSha() {
        return commitSha;
    }
}
