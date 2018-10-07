package repo;

import repo.objects.Blob;
import repo.objects.Commit;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Head could be updated during life, but it is also autosaves on each update.
// There are two possibilities: * load head from branch file
//                              * create absolute new head (it will be stored into branch file inplace)

public class Head implements GitGettable {

    private final Repo repo;
    private GitGettable pointer = null; // branch or commit

    //  load existing Head from file "HEAD"
    Head(Repo repo) throws Exception {
        this.repo = repo;
        List<String> lines = Utils.readFileContentList(repo.headPath);

        assert lines.size() == 2;

        boolean detached = lines.get(1).equals("true");

        if (!detached) {
            String branchName = lines.get(0);
            moveToBranch(branchName);
        } else {
            String commitSha = lines.get(0);
            moveToCommit(commitSha);
        }
    }

    //  create new head
    Head(Repo repo, String branchName) throws Exception {
        this.repo = repo;
        moveToBranch(branchName);
    }

    public Commit getCommit() throws Exception {
        if (!detached()) {
            return ((Branch) pointer).getCommit();
        } else {
            return (Commit) pointer;
        }
    }

    public Branch getBranch() throws Exception {
        if (!detached()) {
            return (Branch) pointer;
        } else {
            return null;
        }
    }


    public void moveToBranch(String branchName) throws Exception {
        pointer = new Branch(repo, branchName); // from existing branch
        store();
    }

    public void moveToCommit(String commitSha) throws Exception {
        pointer = new Commit(repo, commitSha); // from existing commit
        store();
    }

    public boolean detached() throws Exception {
        if (pointer instanceof Branch) {
            return false;
        } else if (pointer instanceof Commit) {
            return true;
        } else {
            throw new Exception("incorrect head state");
        }
    }

    @Override
    public Blob get(Path relativeFilePath) {
        return pointer.get(relativeFilePath);
    }

    @Override
    public Map<String, Blob> getAll() {
        return pointer.getAll();
    }

    @Override
    public boolean contains(Path relativeFilePath) {
        return pointer.contains(relativeFilePath);
    }

    @Override
    public boolean empty() {
        return pointer.empty();
    }

//    ============== private =============

    private void store() throws Exception {
        List<String> listToWrite = new LinkedList<>();

        if (!detached()) {
            listToWrite.add(((Branch) pointer).getName());
        } else {
            listToWrite.add(((Commit) pointer).sha);
        }

        listToWrite.add(Boolean.toString(detached()));

        Utils.writeContent(repo.headPath, listToWrite);
    }

}
