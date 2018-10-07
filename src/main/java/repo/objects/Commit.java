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
        date = lines.get(1).split(" ")[1];

        StringBuilder message_ = new StringBuilder();
        for (String line : lines) {
            String[] line_list = line.split(" ");
            if (!line_list[0].equals("parent")) {
                if (!line_list[0].equals("tree") && !line_list[0].equals("date"))
                    message_.append(line_list[0]);
                continue;
            }
            Commit parent_commit = new Commit(repo, line_list[1]);
            parents.add(parent_commit);
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
