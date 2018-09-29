package repo;

import org.apache.commons.codec.digest.DigestUtils;
import repo.objects.Blob;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Index {
    private final Repo repo;
    private final SortedMap<String, Blob> records = new TreeMap<>(); // relativeFileName -> Blob

    Index(Repo repo) {
        this.repo = repo;
    }

    //  create absolutely new Index on command "mygit init"
    void init() {
        // nothing to do :)
    }

    //  load Index from file "index" on any other command
    void load() throws IOException {
        List<String> lines = Utils.readFileContentList(repo.indexPath);
        for (String line : lines) {
            String[] lineElems = line.split(" ");
            records.put(lineElems[0], new Blob(repo, lineElems[1]));
        }
    }

    public void add(Path relativeFilePath) throws IOException {
        Path absoluteFilePath = repo.trackingDir.resolve(relativeFilePath);

        String content = Utils.readFileContent(absoluteFilePath);

        String contentSha = DigestUtils.sha256Hex(content);

        if (!records.containsKey(relativeFilePath.toString()) || (!records.get(relativeFilePath.toString()).sha.equals(contentSha))) {
            records.put(relativeFilePath.toString(), new Blob(repo, relativeFilePath));
            store();
        }
    }

    public void remove(Path relativeFilePath) throws IOException {
        if (records.containsKey(relativeFilePath.toString())) {
            records.remove(relativeFilePath.toString());
            store();
        }
    }

    public boolean contains(String relativeFileName) {
        return records.containsKey(relativeFileName);
    }

    //    relativeFileName -> Blob
    public SortedMap<String, Blob> getRecords() {
        return records;
    }

    //   relativeFileNames
    public Set<String> getIndexedFileNames() {
        return records.keySet();
    }

    private void store() throws IOException {
        Utils.writeContent(repo.indexPath, repr());
    }

    String repr() {
        List<String> recs = new ArrayList<>();
        for (Map.Entry<String, Blob> entry : records.entrySet()) { // entry: relativeFileName -> Blob
            recs.add(entry.getKey() + " " + entry.getValue().sha);

        }
        return String.join("\n", recs);
    }
}

