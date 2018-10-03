package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.Repo;
import repo.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Commit extends GitObject {
    private final Tree tree;
    private final String date;

    private final Set<Commit> parents;
    private final String message;

    //  create from existing Commit from objects dir
    public Commit(Repo repo, String commitSha) throws Exception {
        super(repo, commitSha);

        Path commitObjectPath = repo.objectsDir.resolve(commitSha);
        if (!Files.exists(commitObjectPath)) {
            throw new IOException(" Commit object does not exists " + commitObjectPath.toString());

        }
        List<String> lines = Utils.readFileContentList(commitObjectPath);
        tree = new Tree(repo, lines.get(0).split(" ")[1]);
        date = lines.get(1).split(" ")[1];

        Set<Commit> parents_ = new HashSet<>();
        StringBuilder message_ = new StringBuilder();
        for (String line : lines) {
            String[] line_list = line.split(" ");
            if (!line_list[0].equals("parent")) {
                if (!line_list[0].equals("tree") && !line_list[0].equals("date"))
                    message_.append(line_list[0]);
                continue;
            }
            Commit parent_commit = new Commit(repo, line_list[1]);
            parents_.add(parent_commit);
        }
        parents = parents_;
        message = message_.toString();

    }

    //  create absolutely new commit
    public Commit(Repo repo, String treeSha, Set<String> parentsShas, String message) throws Exception {
        super(repo);
        tree = new Tree(repo, treeSha);
        date = (new Date()).toString();
        Set<Commit> parents_ = new HashSet<>();

        if (parentsShas != null) {
            for (String parentSha : parentsShas) {
                parents_.add(new Commit(repo, parentSha));
            }
        }

        parents = parents_;
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

    public void store() throws IOException {
        //        store
        Path commitObjectPath = repo.objectsDir.resolve(sha);
        Utils.writeContent(commitObjectPath, repr());
    }

}
