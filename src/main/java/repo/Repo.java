package repo;

import repo.objects.Blob;
import repo.objects.Commit;
import repo.objects.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repo {
    Path trackingDir;
    public final Path repoDir;
    public final Path objectsDir;
    public final Path branchesDir;
    public final Path headFile;

    private final String REPO_DIR_NAME = ".mygit";
    private final String OBJECTS_DIR_NAME = "objects";
    private final String BRANCHES_DIR_NAME = "branches";
    private final String HEAD_FILE_NAME = "HEAD";
    private final String MASTER_BRANCH_NAME = "master";


    Map<String, Branch> branches = new HashMap<>(); // name - Branch
    Head head = new Head(this); // sha of commit


    public Repo(Path trackingDir) {
        this.trackingDir = trackingDir;

        repoDir = trackingDir.resolve(REPO_DIR_NAME);
        objectsDir = repoDir.resolve(OBJECTS_DIR_NAME);
        branchesDir = repoDir.resolve(BRANCHES_DIR_NAME);
        headFile = repoDir.resolve(HEAD_FILE_NAME);
    }

    public void initialize() throws Exception {
        if (Files.exists(repoDir))
            load();
        else {
            Files.createDirectory(repoDir);
            Files.createDirectory(objectsDir);
            Files.createDirectory(branchesDir);
            Files.createFile(headFile);

            Commit firstCommit = new Commit(this, buildFullTree(trackingDir.resolve("test_dir")).sha, null, "init commit");
            branches.put(MASTER_BRANCH_NAME, new Branch(this, MASTER_BRANCH_NAME, firstCommit.sha));
            head.moveToBranch(MASTER_BRANCH_NAME);
        }
    }

    public boolean load() throws IOException {
        if (!Files.exists(repoDir))
            return false;

        for (File brnch_file : branchesDir.toFile().listFiles()) {
            Branch new_branch = new Branch(this, Files.readAllLines(brnch_file.toPath(), StandardCharsets.UTF_8).get(0));
            branches.put(brnch_file.getName(), new_branch);

            String HEAD_NAME = Files.readAllLines(headFile).get(0);
            head.moveToBranch(HEAD_NAME);
        }
        return true;
    }

    private Tree buildFullTree(Path path) throws Exception {
        HashMap<String, String> blobs = new HashMap<>(); // name -> sha
        HashMap<String, String> trees = new HashMap<>(); // name -> sha

        for (File file : path.toFile().listFiles()) {
            if (file.isFile()) {
                System.out.println("building blob " + file.toString());
                Blob blob = new Blob(this, file.toPath());
                blobs.put(file.getName(), blob.sha);
            } else if (file.isDirectory()) {
                Tree tree = buildFullTree(file.toPath());
                trees.put(file.getName(), tree.sha);
            }
        }
        System.out.println("building tree " + path.toString() + " " + blobs.size() + " " + trees.size());
        Tree result_tree = new Tree(this, blobs, trees);
//        System.out.println("\trepr:"+result_tree.repr().split("\n").length);
        return result_tree;
    }

    private Tree buildSingleFile(Path path) throws Exception {
        HashMap<String, String> blobs = new HashMap<>(); // name -> sha
        HashMap<String, String> trees = new HashMap<>(); // name -> sha

        String pathName = path.toFile().getName();

        if (path.toFile().isFile()) {
            Blob tmpBlob = new Blob(this, path);
            blobs.put(pathName, tmpBlob.sha);
        } else {
            Tree tmpTree = buildSingleFile(path);
            trees.put(pathName, tmpTree.sha);
        }
    }

    private Tree addPathesList(List<Path> pathes) throws Exception {
        for (Path path : pathes) {
            if (!Files.exists(path) || !path.startsWith(trackingDir)) {
                throw new Exception("invalid file path");
            }
        }

        HashMap<String, String> blobs = new HashMap<>(); // name -> sha
        HashMap<String, String> trees = new HashMap<>(); // name -> sha

        for (Path path : pathes) {
            String pathName = path.toFile().getName();

            if (Files.isDirectory(path)) {

                Tree tmpTree = buildFullTree(path);
                trees.put(pathName, tmpTree.sha);

            } else if (path.toFile().isFile()) {

                if (path.getParent().toString() == trackingDir.toString()) {

                    Blob tmpBlob = new Blob(this, path);
                    blobs.put(pathName, tmpBlob.sha);

                } else {
                    Tree tmpTree = buildSingleFile(path);
                    trees.put(pathName, tmpTree.sha);
                }

            }
        }

        Tree result_tree = new Tree(this, blobs, trees);
        return result_tree;

    }
}
