package repo;

import org.apache.commons.codec.digest.DigestUtils;
import repo.objects.Blob;
import repo.objects.Tree;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Index implements GitGettable, GitSettable {
    private final Repo repo;
    private final Tree tree;

    //  load Index from file "index" on any other command
    Index(Repo repo) throws IOException {
        this.repo = repo;

        String treeSha = Utils.readFileContent(repo.indexPath);
        tree = new Tree(repo, treeSha);
    }

    //  create absolutely new Index on command "mygit init"
    public Index(Repo repo, Tree tree) throws IOException {
        this.repo = repo;
        this.tree = tree;
        store();
    }

    public Tree getTree() {
        return tree;
    }

    public Map<String, INDEX_HEAD_STATUS> getIndexHeadStatuses() throws Exception {
        Map<String, Blob> headFiles = repo.head.getAll(); // relativeFileName -> Blob
        Map<String, Blob> indexedFiles = getAll(); // relativeFileName -> Blob

        Map<String, INDEX_HEAD_STATUS> result = new HashMap<>();
        Set<String> resultKeys = new HashSet<>();

        resultKeys.addAll(headFiles.keySet());
        resultKeys.addAll(indexedFiles.keySet());

        for (String relativeFileName : resultKeys) {
            INDEX_HEAD_STATUS indexHeadStatus = INDEX_HEAD_STATUS.getFileStatus(relativeFileName, headFiles, indexedFiles);

            result.put(relativeFileName, indexHeadStatus);
        }
        return result;
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
        return false;
    }

    @Override
    public void add(Path relativeFilePath) throws IOException {
        tree.add(relativeFilePath);
        store();
    }

    @Override
    public void addAll(Set<Path> paths) throws IOException {
        tree.addAll(paths);
        store();

    }

    @Override
    public void remove(Path relativeFilePath) throws IOException {
        tree.remove(relativeFilePath);
        store();
    }

    @Override
    public void removeAll(Set<Path> paths) throws IOException {
        tree.removeAll(paths);
        store();
    }


    String repr() {
        return tree.sha;
    }

//    ============== private =============

    private void store() throws IOException {
        Utils.writeContent(repo.indexPath, repr());
    }

}


//    //    relativeFileName -> Blob
//    public SortedMap<String, Blob> getRecords() {
//        return records;
//    }
//
//    //   relativeFileNames
//    public Set<String> getIndexedFileNames() {
//        return records.keySet();
//    }