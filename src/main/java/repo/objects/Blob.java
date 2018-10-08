package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.Repo;
import repo.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Blob could not be updated during life.
// There are two possibilities: * load blob from object file
//                              * create absolute new blob (it will be stored into object file inplace)

public class Blob extends GitObject {
    private final String content;

    // create from existing object
    public Blob(Repo repo, String objectSha) throws IOException {
        super(repo, objectSha);

        Path blobObjectPath = repo.objectsDir.resolve(objectSha);
        if (!Files.exists(blobObjectPath)) {
            throw new IOException(" Blob object does not exists " + blobObjectPath.toString());
        }
        content = Utils.readFileContent(blobObjectPath);
        String contentSha = DigestUtils.sha256Hex(content);
        if (!contentSha.equals(objectSha)) {
            throw new IOException(" Blob content sha != Blob object name. Blob: " + objectSha + ", contentSha: " + contentSha);
        }
    }

    // create absolutely new object from relativeFilePath
    public Blob(Repo repo, Path relativeFilePath) throws IOException {
        super(repo);
        Path absoluteFilePath = repo.trackingDir.resolve(relativeFilePath);

        content = Utils.readFileContent(absoluteFilePath);
        sha = DigestUtils.sha256Hex(content);

        store();
    }

    @Override
    public String repr() {
        return content;
    }

//    ============== private =============

    private void store() throws IOException {
        Path blobObjectPath = repo.objectsDir.resolve(sha);
        Utils.writeContent(blobObjectPath, repr());
    }

}
