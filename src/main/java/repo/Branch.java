package repo;

import exceptions.BranchNotExistsException;
import exceptions.MyGitException;
import repo.objects.Blob;
import repo.objects.Commit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

// Branch could be updated during life, but it is also autosaves on each update.
// There are two possibilities: * load branch from branch file
//                              * create absolute new branch (it will be stored into branch file inplace)

public class Branch implements GitGettable {
    private final Repo repo;
    private final String name;
    private Commit commit;

    //  load branch from file
    public Branch(Repo repo, String branchName) throws IOException, MyGitException {
        this.repo = repo;
        this.name = branchName;

        Path absoluteBranchFilePath = this.repo.branchesDir.resolve(this.name);
        if (Files.exists(absoluteBranchFilePath)) {
            String commitSha = Utils.readFileContentList(absoluteBranchFilePath).get(0);
            commit = new Commit(repo, commitSha);
        } else {
            throw new BranchNotExistsException(branchName);
        }
    }

    //  create absolutely new branch
    public Branch(Repo repo, String branchName, String commitSha) throws Exception {
        this.repo = repo;
        this.name = branchName;
        this.commit = new Commit(repo, commitSha);
        store();
    }


    public String getName() {
        return name;
    }

    public Commit getCommit() {
        return commit;
    }

    public void moveToCommit(String commitSha) throws Exception {
        this.commit = new Commit(repo, commitSha); // create from existing commit
        store();
    }


    @Override
    public Blob get(Path relativeFilePath) {
        return commit.get(relativeFilePath);
    }

    @Override
    public Map<String, Blob> getAll() {
        return commit.getAll();
    }

    @Override
    public boolean contains(Path relativeFilePath) {
        return commit.contains(relativeFilePath);
    }

    @Override
    public boolean empty() {
        return commit.empty();
    }

//    ============== private =============

    private void store() throws IOException {
        Path absoluteBranchFilePath = this.repo.branchesDir.resolve(this.name);

        Utils.writeContent(absoluteBranchFilePath, commit.sha);
    }
}
