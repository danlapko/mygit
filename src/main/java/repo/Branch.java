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

    //  load branch from file
    Branch(Repo repo, String branchName) throws IOException {
        this.repo = repo;
        this.name = branchName;

        Path absoluteBranchFilePath = this.repo.branchesDir.resolve(this.name);
        if (Files.exists(absoluteBranchFilePath)) {
            commitSha = Utils.readFileContentList(absoluteBranchFilePath).get(0);
        } else {
            throw new IOException("branch " + branchName + " does not exist!");
        }
    }

    //  create absolutely new branch
    Branch(Repo repo, String branchName, String commitSha) throws Exception {
        this.repo = repo;
        this.name = branchName;
        this.commitSha = commitSha;

        Path absoluteBranchFilePath = this.repo.branchesDir.resolve(this.name);
        if (Files.exists(absoluteBranchFilePath)) {
            throw new IOException("branch " + branchName + " already exists!");
        } else {
            Files.createFile(absoluteBranchFilePath);
            Utils.writeContent(absoluteBranchFilePath, commitSha);
        }
    }


    public String getName() {
        return name;
    }

    public String getCommitSha() {
        return commitSha;
    }
}
