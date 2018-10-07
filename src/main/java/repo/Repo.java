package repo;

import org.apache.commons.codec.digest.DigestUtils;
import repo.objects.Blob;
import repo.objects.Commit;
import repo.objects.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Repo {
    public Path trackingDir;
    public final Path repoDir;
    public final Path objectsDir;
    public final Path branchesDir;
    public final Path headPath;
    public final Path indexPath;


    private final String REPO_DIR_NAME = ".mygit";
    private final String OBJECTS_DIR_NAME = "objects";
    private final String BRANCHES_DIR_NAME = "branches";
    private final String HEAD_FILE_NAME = "HEAD";
    private final String INDEX_FILE_NAME = "index";
    private final String MASTER_BRANCH_NAME = "master";


    Map<String, Branch> branches = new HashMap<>(); // branchName -> Branch
    public Head head;
    public Index index;


    public Repo(Path trackingDir) {
        this.trackingDir = trackingDir;

        repoDir = trackingDir.resolve(REPO_DIR_NAME);
        objectsDir = repoDir.resolve(OBJECTS_DIR_NAME);
        branchesDir = repoDir.resolve(BRANCHES_DIR_NAME);
        headPath = repoDir.resolve(HEAD_FILE_NAME);
        indexPath = repoDir.resolve(INDEX_FILE_NAME);
    }

    //  initialize empty repository on command "mygit init"
    public void init() throws Exception {
        if (Files.exists(repoDir)) // if repository .mygit already exists simply load it
            load();
        else {
            Files.createDirectory(repoDir);
            Files.createDirectory(objectsDir);
            Files.createDirectory(branchesDir);
            Files.createFile(headPath);
            Files.createFile(indexPath);

            Tree tree = new Tree(this);
            Commit commit = new Commit(this, tree.sha, null, "INITIAL COMMIT");
            Branch master = new Branch(this, MASTER_BRANCH_NAME, commit.sha);
            branches.put(MASTER_BRANCH_NAME, master);

            head = new Head(this, master.getName());
            index = new Index(this, tree);

        }
    }

    //  load repository on any command except "mygit init"
    //  returns false if there is no repository dir (".mygit")
    public boolean load() throws Exception {
        if (!Files.exists(repoDir))
            return false;

        head = new Head(this);
        index = new Index(this);

        for (File branchFileName : branchesDir.toFile().listFiles()) {
            Branch oneAnotherBranch = new Branch(this, branchFileName.getName());
            branches.put(branchFileName.getName(), oneAnotherBranch);
        }

        System.out.println("<master tree>");
        branches.get("master").getCommit().getTree().print("");
        System.out.println("</master tree>\n");

        System.out.println("<index tree>");
        index.getTree().print("");
        System.out.println("</index tree>\n");
        return true;
    }

    // relativeFilePaths
    public List<Path> getWorkdirFiles() throws IOException {
        List<Path> allRelativeFilePaths = Files
                .walk(trackingDir)
                .map(Path::normalize)
                .filter(pth -> !pth.startsWith(repoDir))
                .filter(Files::isRegularFile)
                .filter(Files::isReadable)
                .map(pth -> trackingDir.relativize(pth)) // make relative
                .collect(Collectors.toList());
        return allRelativeFilePaths;
    }

    // relativeFileName -> Sha
    public Map<String, String> getWorkdirPathsShas() throws IOException {

        Map<String, String> result = new HashMap<>();
        List<Path> allRelativeFileNames = getWorkdirFiles();


        for (Path relativeFilePath : allRelativeFileNames) {
            Path absoluteFilePath = trackingDir.resolve(relativeFilePath);


            String content = Utils.readFileContent(absoluteFilePath);
            String contentSha = DigestUtils.sha256Hex(content);
            result.put(relativeFilePath.toString(), contentSha);
        }

        return result;
    }


    // relativeFileName -> STATUS
    public Map<String, STATUS> getFilesStatuses() throws Exception {
        Map<String, Blob> headFiles = head.getAll(); // relativeFileName -> Blob
        Map<String, Blob> indexedFiles = index.getAll(); // relativeFileName -> Blob
        Map<String, String> workdirFiles = getWorkdirPathsShas(); // relativeFilePath -> Sha

        Map<String, STATUS> result = new HashMap<>();
        Set<String> resultKeys = new HashSet<>();

        resultKeys.addAll(headFiles.keySet());
        resultKeys.addAll(indexedFiles.keySet());
        resultKeys.addAll(workdirFiles.keySet());


        for (String relativeFileName : resultKeys) {
            STATUS status;
            WORKDIR_INDEX_STATUS workdirIndexStatus;
            INDEX_HEAD_STATUS indexHeadStatus;

            // assign workdirIndexStatus
            workdirIndexStatus = WORKDIR_INDEX_STATUS.getFileStatus(relativeFileName, workdirFiles, indexedFiles);


            // assign indexHeadStatus
            indexHeadStatus = INDEX_HEAD_STATUS.getFileStatus(relativeFileName, headFiles, indexedFiles);

            status = new STATUS(workdirIndexStatus, indexHeadStatus);
            result.put(relativeFileName, status);
        }
        return result;
    }


}


//    public STATUS getFileStatus(Path relativeFilePath) throws Exception {
//        Path absoluteFilePath = trackingDir.resolve(relativeFilePath);
//
//        Map<String, Blob> headFiles = head.getFiles(); // relativeFileName -> Blob
//        Map<String, Blob> indexedFiles = index.getIndexedFiles(); // relativeFileName -> Blob
//        Map<String, String> workdirFiles = getWorkdirPathsShas(); // relativeFilePath -> Sha
//
//
//        STATUS status = null;
//        WORKDIR_INDEX_STATUS workdirIndexStatus = null;
//        INDEX_HEAD_STATUS indexHeadStatus = null;
//
//        return null;
//    }

/*
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
    */

    /*
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
    */