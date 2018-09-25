package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.Repo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Commit extends GitObject {
    private final Tree tree;
    private final String date;

    private final Set<Commit> parents;
    private final String message;

    public Commit(Repo repo, String sha) throws Exception {
        super(repo, sha);

        Path commitObjectPath = repo.objectsDir.resolve(sha);
        if (!Files.exists(commitObjectPath)) {
            throw new IOException(" Commit object does not exists " + commitObjectPath.toString());

        }
        List<String> lines = Files.readAllLines(commitObjectPath, StandardCharsets.UTF_8);
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

    public Commit(Repo repo, String tree, Set<String> parents, String message) throws Exception {
        super(repo, "");
        this.tree = new Tree(repo, tree);
        this.date = (new Date()).toString();
        Set<Commit> parents_ = new HashSet<>();

        if (parents != null) {
            for (String parent : parents) {
                parents_.add(new Commit(repo, parent));
            }
        }
        this.parents = parents_;
        this.message = message;

        sha = DigestUtils.sha256Hex(repr());

//        store
        Path commitObjectPath = repo.objectsDir.resolve(sha);
        Files.write(commitObjectPath, Arrays.asList(repr().split("\n")));
    }

    @Override
    public String repr() throws Exception {
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

}
