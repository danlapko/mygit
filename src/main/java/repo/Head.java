package repo;

import repo.objects.Blob;
import repo.objects.Commit;
import repo.objects.Tree;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Head {
    private final Repo repo;
    private String pointer; // branchName or commitSha
    private boolean detached;

    Head(Repo repo) {
        this.repo = repo;
    }

    //  create absolutely new Head on command "mygit init"
    void init(String branchName) throws IOException {
        pointer = branchName;
        detached = false;

        storeHead();
    }

    //  load Head from file "HEAD" on any other command
    void load() throws IOException {
        List<String> lines = Utils.readFileContentList(repo.headPath);
        assert lines.size() == 2;
        pointer = lines.get(0);
        detached = lines.get(1).equals("true");
    }

    void moveToBranch(String branchToMoveName) throws IOException {
        if (!Files.exists(repo.branchesDir.resolve(branchToMoveName))) {
            throw new IOException(" such branch " + branchToMoveName + " does not exists and I can't move to it!");
        }
        detached = false;
        pointer = branchToMoveName;

        storeHead();
    }

    public boolean contains(String relativeFileName) throws Exception {
        return getFiles().containsKey(relativeFileName);
    }

    public Map<String, Blob> getFiles() throws Exception {
        Map<String, Blob> headFiles;
        String headCommitSha = getCommitSha();

        if (headCommitSha.equals("empty_sha")) {
            headFiles = new HashMap<>();
        } else {
            Commit headCommit = new Commit(repo, headCommitSha);
            Tree headTree = headCommit.getTree();
            headFiles = headTree.getAllSubBlobs();
        }

        return headFiles;
    }


    void moveToCommit(String sha) throws IOException {
        this.detached = true;
        this.pointer = sha;

        storeHead();
    }

    String getCommitSha() {
        if (this.detached) {
            return this.pointer;
        } else {
            return repo.branches.get(this.pointer).getCommitSha();
        }
    }

    private void storeHead() throws IOException {
        List<String> listToWrite = new LinkedList<>();
        listToWrite.add(pointer);
        if (detached) {
            listToWrite.add("true");
        } else {
            listToWrite.add("false");
        }
        Utils.writeContent(repo.headPath, listToWrite);
    }
}
