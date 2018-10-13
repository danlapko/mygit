package repo.objects;

import exceptions.InvalidTreeObjectStructureException;
import exceptions.MyGitException;
import exceptions.TreeNotExistsException;
import org.apache.commons.codec.digest.DigestUtils;
import repo.GitGettable;
import repo.GitSettable;
import repo.Repo;
import repo.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

// Tree can be updated during life, but it is also autosaves on each update.
// There are two possibilities: * load tree from object file
//                              * create absolute new tree (it will be not stored until first update come)

public class Tree extends GitObject implements GitGettable, GitSettable {
    private final Map<String, Blob> blobs; // relativeFileName -> Blob
    private final Map<String, Tree> trees; // relativeFileName -> Tree

    // create from existing tree from objects dir
    public Tree(Repo repo, String objectSha) throws IOException, MyGitException {
        super(repo, objectSha);

        // reconstruct from object file
        blobs = new HashMap<>();
        trees = new HashMap<>();
        Path treeObjectPath = repo.objectsDir.resolve(objectSha);
        if (!Files.exists(treeObjectPath)) {
            throw new TreeNotExistsException(objectSha);
        }


        for (String line : Utils.readFileContentList(treeObjectPath)) {
            String[] lineList = line.split(" ");
            if (lineList.length < 2)
                continue;
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
                    throw new InvalidTreeObjectStructureException(objectSha);
            }

        }
        // end of reconstruct
        sha = DigestUtils.sha256Hex(repr());
    }


    //  create absolutely new tree
    public Tree(Repo repo) throws IOException {
        super(repo);
        blobs = new HashMap<>();
        trees = new HashMap<>();
        store();
    }

    @Override
    public Blob get(Path relativeFilePath) {
        Deque<String> nameSegments = convertRelativePathToSegments(relativeFilePath);
        return get(nameSegments);
    }

    // relativeFileName -> blob
    @Override
    public Map<String, Blob> getAll() {

        Map<String, Blob> result = new HashMap<>(blobs);

        for (Map.Entry<String, Tree> treeEntry : trees.entrySet()) {
            String subtreeName = treeEntry.getKey();
            Tree subtree = treeEntry.getValue();
            Map<String, Blob> subresult = subtree.getAll();
            for (Map.Entry<String, Blob> blobEntry : subresult.entrySet()) {
                result.put(subtreeName + "/" + blobEntry.getKey(), blobEntry.getValue());
            }
        }
        return result;
    }

    @Override
    public boolean contains(Path relativeFilePath) {
        Deque<String> nameSegments = convertRelativePathToSegments(relativeFilePath);
        return contains(nameSegments);
    }

    @Override
    public boolean empty() {
        return 0 == trees.size() + blobs.size();
    }

    @Override
    public void add(Path relativeFilePath) throws IOException {
        Blob blob = new Blob(repo, relativeFilePath);
        Deque<String> nameSegments = convertRelativePathToSegments(relativeFilePath);
        add(nameSegments, blob);
        store();
    }

    @Override
    public void addAll(Set<Path> paths) throws IOException {
        for (Path relativeFilePath : paths) {
            Blob blob = new Blob(repo, relativeFilePath);

            Deque<String> nameSegments = convertRelativePathToSegments(relativeFilePath);
            add(nameSegments, blob); // internalAdd
        }

        store();
    }


    public void addAll(Map<Path, Blob> blobs) throws IOException {
        for (Map.Entry<Path, Blob> entry : blobs.entrySet()) {

            Deque<String> nameSegments = convertRelativePathToSegments(entry.getKey());
            add(nameSegments, entry.getValue()); // internalAdd
        }

        store();
    }

    @Override
    public void remove(Path relativeFilePath) throws IOException {
        Deque<String> nameSegments = convertRelativePathToSegments(relativeFilePath);
        remove(nameSegments);

        store();
    }

    @Override
    public void removeAll(Set<Path> paths) throws IOException {
        for (Path relativeFilePath : paths) {

            Deque<String> nameSegments = convertRelativePathToSegments(relativeFilePath);
            remove(nameSegments); // internalRemove
        }

        store();
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

    public void print(String indent) {
        for (Map.Entry<String, Blob> blobEntry : blobs.entrySet()) {
            System.out.println(indent + "-" + blobEntry.getKey() + " " + blobEntry.getValue());
        }
        for (Map.Entry<String, Tree> treeEntry : trees.entrySet()) {
            System.out.println(indent + treeEntry.getKey());
            treeEntry.getValue().print(indent + "   ");
        }
    }


//    ================= private ================

    private Blob get(Deque<String> nameSegments) {
        String nextSegment = nameSegments.pop();
        if (nameSegments.size() == 0) {
            return blobs.getOrDefault(nextSegment, null);
        } else {
            if (!trees.containsKey(nextSegment)) return null;
            else return trees.get(nextSegment).get(nameSegments);
        }
    }

    private boolean contains(Deque<String> nameSegments) {
        return null != get(nameSegments);
    }

    private static Deque<String> convertRelativePathToSegments(Path relativePath) {
        return new ArrayDeque<>(Arrays.asList(relativePath.toString().split(Pattern.quote(File.separator))));
    }

    private void add(Deque<String> nameSegments, Blob blob) throws IOException {
        String nextSegment = nameSegments.pop(); // TODO: is it correct?
        if (nameSegments.size() == 0) { // if it is last component in path
            blobs.put(nextSegment, blob);
        } else {
            if (trees.containsKey(nextSegment)) { // if subtree contains in this.trees
                Tree nextTree = trees.get(nextSegment);
                nextTree.add(nameSegments, blob);
            } else { // create new subtree
                Tree subTree = new Tree(repo);
                trees.put(nextSegment, subTree);
                subTree.add(nameSegments, blob);
            }
        }

    }

    private void remove(Deque<String> nameSegments) {
        String nextSegments = nameSegments.pop(); // TODO: is it correct?
        if (nameSegments.size() == 0) { // if it is last component in path
            blobs.remove(nextSegments);
        } else {
            Tree nextTree = trees.get(nextSegments);
            nextTree.remove(nameSegments);
            if (nextTree.empty()) { // if subtree become empty
                trees.remove(nextSegments);
            }
        }

    }

    private void store() throws IOException {
        for (Map.Entry<String, Tree> entry : trees.entrySet()) {
            Tree subtree = entry.getValue();
            subtree.store();
        }

        sha = DigestUtils.sha256Hex(repr());

        Path currentTreeObjectPath = repo.objectsDir.resolve(sha);
        if (repr().length() == 0)
            Utils.writeContent(currentTreeObjectPath, "");
        else
            Utils.writeContent(currentTreeObjectPath, repr());
    }
}

//    //  create absolutely new tree
//    // blobs: relativeFileName -> blobSha
//    // trees: relativeFileName -> treeSha
//    public Tree(Repo repo, Map<String, String> blobs, Map<String, String> trees) throws Exception {
//        super(repo);
//
//        this.blobs = new HashMap<>(); // relativeFileName -> Blob
//        this.trees = new HashMap<>(); // relativeFileName -> Tree
//
//        for (Map.Entry<String, String> entry : blobs.entrySet()) {
//            Blob blob = new Blob(repo, entry.getValue());
//            this.blobs.put(entry.getKey(), blob);
//        }
//
//        for (Map.Entry<String, String> entry : trees.entrySet()) {
//            Tree tree = new Tree(repo, entry.getValue());
//            this.trees.put(entry.getKey(), tree);
//        }
//
//        store();
//    }


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


//    //  return: relativeFileName -> Blob
//    public Map<String, Blob> getAll() {
//        Map<String, Blob> result = new HashMap<>(blobs);
//        for (Map.Entry<String, Tree> entry : trees.entrySet()) {
//            result.putAll(entry.getValue().getAll());
//        }
//        return result;
//    }