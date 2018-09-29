package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.Repo;
import repo.Utils;

import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Tree extends GitObject {
    private final Map<String, Blob> blobs; // relativeFileName -> Blob
    private final Map<String, Tree> trees; // relativeFileName -> Tree

    // create from existing tree from objects dir
    Tree(Repo repo, String objectSha) throws Exception {
        super(repo, objectSha);

        // reconstruct from object file
        Map<String, Blob> blobs_ = new HashMap<>();
        Map<String, Tree> trees_ = new HashMap<>();
        Path treeObjectPath = repo.objectsDir.resolve(objectSha);
        if (!Files.exists(treeObjectPath)) {
            throw new IOException(" Tree object does not exists " + treeObjectPath.toString());
        }


        for (String line : Utils.readFileContentList(treeObjectPath)) {
            String[] lineList = line.split(" ");
            String type = lineList[0];
            String lineSha = lineList[1];
            String lineRelativeFileName = lineList[2];
            switch (type) {
                case "blob":
                    blobs_.put(lineRelativeFileName, new Blob(repo, lineSha));
                    break;
                case "tree":
                    trees_.put(lineRelativeFileName, new Tree(repo, lineSha));
                    break;
                default:
                    throw new Exception();
            }

        }
        // end of reconstruct

        blobs = blobs_;
        trees = trees_;
    }


    //  create absolutely new tree
    // blobs: relativeFileName -> blobSha
    // trees: relativeFileName -> treeSha
    public Tree(Repo repo, Map<String, String> blobs, Map<String, String> trees) throws Exception {
        super(repo, "");

        Map<String, Blob> blobs_ = new HashMap<>(); // relativeFileName -> Blob
        Map<String, Tree> trees_ = new HashMap<>(); // relativeFileName -> Tree

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

        // store
        Path currentTreeObjectPath = repo.objectsDir.resolve(sha);
        if (repr().length() == 0)
            Utils.writeContent(currentTreeObjectPath,"");
        else
            Utils.writeContent(currentTreeObjectPath, repr());
    }

    //  return: relativeFileName -> Blob
    public Map<String, Blob> getBlobs() {
        return blobs;
    }

    //  return: relativeFileName -> Tree
    public Map<String, Tree> getTrees() {
        return trees;
    }

    //  return: relativeFileName -> Blob
    public Map<String, Blob> getAllSubBlobs() {
        Map<String, Blob> result = new HashMap<>(blobs);
        for (Map.Entry<String, Tree> entry : trees.entrySet()) {
            result.putAll(entry.getValue().getAllSubBlobs());
        }
        return result;
    }

    @Override
    public String repr() {
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
