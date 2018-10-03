package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.Repo;
import repo.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Tree extends GitObject {
    private final Map<String, Blob> blobs; // relativeFileName -> Blob
    private final Map<String, Tree> trees; // relativeFileName -> Tree

    // create from existing tree from objects dir
    Tree(Repo repo, String objectSha) throws Exception {
        super(repo, objectSha);

        // reconstruct from object file
        blobs = new HashMap<>();
        trees = new HashMap<>();
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
                    blobs.put(lineRelativeFileName, new Blob(repo, lineSha));
                    break;
                case "tree":
                    trees.put(lineRelativeFileName, new Tree(repo, lineSha));
                    break;
                default:
                    throw new Exception();
            }

        }
        // end of reconstruct
    }


    //  create absolutely new tree
    public Tree(Repo repo) throws IOException {
        super(repo);
        blobs = new HashMap<>();
        trees = new HashMap<>();
        store(); // TODO ?

    }

    //  create absolutely new tree
    // blobs: relativeFileName -> blobSha
    // trees: relativeFileName -> treeSha
    public Tree(Repo repo, Map<String, String> blobs, Map<String, String> trees) throws Exception {
        super(repo);

        this.blobs = new HashMap<>(); // relativeFileName -> Blob
        this.trees = new HashMap<>(); // relativeFileName -> Tree

        for (Map.Entry<String, String> entry : blobs.entrySet()) {
            Blob blob = new Blob(repo, entry.getValue());
            this.blobs.put(entry.getKey(), blob);
        }

        for (Map.Entry<String, String> entry : trees.entrySet()) {
            Tree tree = new Tree(repo, entry.getValue());
            this.trees.put(entry.getKey(), tree);
        }

        store();
    }


    public void addPath(Queue<String> nameComponents, Blob blob) throws Exception {
        String nextComponent = nameComponents.peek(); // TODO: is it correct?
        if (nameComponents.size() == 1) { // if it is last component in path
            blobs.put(nextComponent, blob);
        } else {
            if (trees.containsKey(nextComponent)) { // if subtree contains in this.trees
                Tree nextTree = trees.get(nextComponent);
                nextTree.addPath(nameComponents, blob);
            } else { // create new subtree
                Tree subTree = new Tree(repo);
                subTree.addPath(nameComponents, blob);
            }
        }

    }

    public void removePath(Queue<String> nameComponents) throws Exception {
        String nextComponent = nameComponents.peek(); // TODO: is it correct?
        if (nameComponents.size() == 1) { // if it is last component in path
            blobs.remove(nextComponent);
        } else {
            Tree nextTree = trees.get(nextComponent);
            nextTree.removePath(nameComponents);
            if (nextTree.getBlobs().size() == 0 && nextTree.getTrees().size() == 0) { // if subtree become empty
                trees.remove(nextComponent);
            }
        }

    }

    public void storeRecursivly() throws IOException {
        for (Map.Entry<String, Tree> entry : trees.entrySet()) {
            Tree subtree = entry.getValue();
            subtree.storeRecursivly();
        }

        for (Map.Entry<String, Blob> entry : blobs.entrySet()) {
            Blob blob = entry.getValue();
            blob.store();
        }
        store();

    }

    public void store() throws IOException {
        sha = DigestUtils.sha256Hex(repr());

        // store
        Path currentTreeObjectPath = repo.objectsDir.resolve(sha);
        if (repr().length() == 0)
            Utils.writeContent(currentTreeObjectPath, "");
        else
            Utils.writeContent(currentTreeObjectPath, repr());
    }

//    private Tree buildFullTree(Path relativeDirPath) throws Exception {
//        HashMap<String, String> blobs = new HashMap<>(); // name -> sha
//        HashMap<String, String> trees = new HashMap<>(); // name -> sha
//        Tree resultTree;
//
//        Path absoluteDirPath = repo.trackingDir.resolve(relativeDirPath);
//
//        for (File fileFile : absoluteDirPath.toFile().listFiles()) {
//            Path absoluteFilePath = fileFile.toPath();
//            Path relativeFilePath = repo.trackingDir.relativize(absoluteFilePath);
//            if (fileFile.isFile()) {
//                Blob blob = new Blob(this.repo, relativeFilePath);
//                blobs.put(relativeFilePath.toString(), blob.sha);
//            } else if (fileFile.isDirectory()) {
//                Tree tree = buildFullTree(relativeFilePath);
//                trees.put(relativeFilePath.toString(), tree.sha);
//            }
//        }
//        System.out.println("building tree " + relativeDirPath.toString() + " " + blobs.size() + " " + trees.size());
//        resultTree = new Tree(this.repo, blobs, trees);
////        System.out.println("\trepr:"+resultTree.repr().split("\n").length);
//        return resultTree;
//    }

    //  return: relativeFileName -> Blob
    public Map<String, Blob> getBlobs() {
        return blobs;
    }

    //  return: relativeFileName -> Tree
    public Map<String, Tree> getTrees() {
        return trees;
    }

//    //  return: relativeFileName -> Blob
//    public Map<String, Blob> getAllSubBlobs() {
//        Map<String, Blob> result = new HashMap<>(blobs);
//        for (Map.Entry<String, Tree> entry : trees.entrySet()) {
//            result.putAll(entry.getValue().getAllSubBlobs());
//        }
//        return result;
//    }


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
