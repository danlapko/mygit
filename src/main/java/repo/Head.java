package repo;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Head {
    private final Repo repo;
    private boolean detached;
    private String pointer; // branchName or commitSha

    public Head(Repo repo) {
        this.repo = repo;
    }

    void moveToBranch(String branchName) throws IOException {
        this.detached = false;
        this.pointer = branchName;

        Files.write(repo.headFile, Arrays.asList(branchName.split("\n")));
    }

    void moveToCommit(String sha) {
        this.detached = true;
        this.pointer = sha;
    }

    public String getCommitSha() {
        if (this.detached) {
            return this.pointer;
        } else {
            return repo.branches.get(this.pointer).getCommitSha();
        }
    }
}
