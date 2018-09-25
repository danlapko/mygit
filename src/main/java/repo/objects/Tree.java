package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.Repo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Tree extends GitObject {
    private final Map<String, Blob> blobs; // name -> blob
    private final Map<String, Tree> trees; // name -> tree

    public Tree(Repo repo, String sha) throws Exception {
        super(repo, sha);

//        reconstructFromObjectFile
        Map<String, Blob> blobs_ = new HashMap<>();
        Map<String, Tree> trees_ = new HashMap<>();
        Path treeObjectPath = repo.objectsDir.resolve(sha);
        if (!Files.exists(treeObjectPath)) {
            throw new IOException(" Tree object does not exists " + treeObjectPath.toString());
        }


        for (String line : Files.readAllLines(treeObjectPath, StandardCharsets.UTF_8)) {
            String[] line_list = line.split(" ");
            String type = line_list[0];
            String line_sha = line_list[1];
            String name = line_list[2];
            switch (type) {
                case "blob":
                    blobs_.put(name, new Blob(repo, line_sha));
                    break;
                case "tree":
                    trees_.put(name, new Tree(repo, line_sha));
                    break;
                default:
                    throw new Exception();
            }

        }
//        end of reconstruct

        blobs = blobs_;
        trees = trees_;
    }

    public Tree(Repo repo, Map<String, String> blobs, Map<String, String> trees) throws Exception {
        super(repo, "");

        Map<String, Blob> blobs_ = new HashMap<>();
        Map<String, Tree> trees_ = new HashMap<>();
        for (Map.Entry<String, String> entry : blobs.entrySet()) {
            Blob blob = new Blob(repo, entry.getValue());
            blobs_.put(entry.getKey(), blob);
        }

        for (Map.Entry<String, String> entry : trees.entrySet()) {
            Tree tree = new Tree(repo, entry.getValue());
            trees_.put(entry.getKey(), tree);
        }
        this.blobs = blobs_;
        this.trees = trees_;

        sha = DigestUtils.sha256Hex(repr());

//        store
        Path treeObjectPath = repo.objectsDir.resolve(sha);
        if (repr().length() == 0)
            Files.write(treeObjectPath, new ArrayList<>());
        else
            Files.write(treeObjectPath, Arrays.asList(repr().split("\n")));
    }


    @Override
    public String repr() throws Exception {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<String, Blob> entry : blobs.entrySet()) {
            s.append("blob ").append(entry.getValue().sha).append(" ").append(entry.getKey()).append("\n");
        }

        for (Map.Entry<String, Tree> entry : trees.entrySet()) {
            s.append("tree ").append(entry.getValue().sha).append(" ").append(entry.getKey()).append("\n");
        }

        return s.toString();
    }

}
