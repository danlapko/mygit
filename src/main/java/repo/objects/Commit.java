package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.GitGettable;
import repo.Repo;
import repo.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Commit could not be updated during life.
// There are two possibilities: * load commit from object file
//                              * create absolute new commit (it will be stored into object file inplace)

public class Commit extends GitObject implements GitGettable {
    private final Tree tree;
    private final String date;

    private final Set<Commit> parents = new HashSet<>();
    private final String message;

    //  create from existing Commit from objects dir
    public Commit(Repo repo, String commitSha) throws IOException {
        super(repo, commitSha);

        Path commitObjectPath = repo.objectsDir.resolve(commitSha);
        if (!Files.exists(commitObjectPath)) {
            throw new IOException(" Commit object does not exists " + commitObjectPath.toString());

        }
        List<String> lines = Utils.readFileContentList(commitObjectPath);
        tree = new Tree(repo, lines.get(0).split(" ")[1]);

        String[] dateLineList = lines.get(1).split(" ");
        dateLineList = Arrays.copyOfRange(dateLineList, 1, dateLineList.length); // copy without first item
        date = String.join(" ", dateLineList);

        StringBuilder message_ = new StringBuilder();
        for (String line : lines) {
            String[] line_list = line.split(" ");
            if (!line_list[0].equals("parent") && !line_list[0].equals("tree") && !line_list[0].equals("date"))
                message_.append(line);
            else if (line_list[0].equals("parent")) {
                Commit parent_commit = new Commit(repo, line_list[1]);
                parents.add(parent_commit);
            }
        }
        message = message_.toString();

    }


    //  create absolutely new commit
    public Commit(Repo repo, String treeSha, Set<String> parentsShas, String message) throws Exception {
        super(repo);
        tree = new Tree(repo, treeSha);
        date = (new Date()).toString();

        if (parentsShas != null) {
            for (String parentSha : parentsShas) {
                parents.add(new Commit(repo, parentSha));
            }
        }

        this.message = message;

        sha = DigestUtils.sha256Hex(repr());
        store();
    }

    public Tree getTree() {
        return tree;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public Set<Commit> getParents() {
        return parents;
    }

    private boolean visited = false;

    public void logVisit(Deque<String> reps, String fromRevision, boolean readyToLog) {
        if (visited) {
            return;
        }

        String s = "";
        s += "commit " + sha + "\n";
        s += repr();

        if (!readyToLog && sha.equals(fromRevision)) {
            readyToLog = true;
        }
        if (readyToLog) {
            reps.push(s);
        }

        visited = true;

        if (parents == null) {
            return;
        } else {
            for (Commit parent : parents) {
                parent.logVisit(reps, fromRevision, readyToLog);
            }
        }
    }

    public boolean checkRevisionIsAncestor(String revisionSha) {
        if (revisionSha.equals(sha))
            return true;
        if (parents == null)
            return false;

        boolean result = false;
        for (Commit parent : parents) {
            result = result || parent.checkRevisionIsAncestor(revisionSha);
        }
        return result;
    }

    @Override
    public String repr() {
        StringBuilder s = new StringBuilder();
        s.append("tree ").append(tree.sha).append("\n");
        s.append("date ").append(date).append("\n");

        for (Commit parent : parents) {
            s.append("parent ").append(parent.sha).append("\n");
        }

        s.append("\n");
        s.append(message);

        return s.toString();
    }


    @Override
    public Blob get(Path relativeFilePath) {
        return tree.get(relativeFilePath);
    }

    @Override
    public Map<String, Blob> getAll() {
        return tree.getAll();
    }

    @Override
    public boolean contains(Path relativeFilePath) {
        return tree.contains(relativeFilePath);
    }

    @Override
    public boolean empty() {
        return tree.empty();
    }

//    ============== private =============

    private void store() throws IOException {
        Path commitObjectPath = repo.objectsDir.resolve(sha);
        Utils.writeContent(commitObjectPath, repr());
    }
}
