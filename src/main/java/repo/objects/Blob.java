package repo.objects;

import org.apache.commons.codec.digest.DigestUtils;
import repo.Repo;
import repo.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Blob extends GitObject {
    private final String content;

    // create from existing object
    public Blob(Repo repo, String shaFromObjects) throws IOException {
        super(repo, shaFromObjects);

        Path blobObjectPath = repo.objectsDir.resolve(shaFromObjects);
        if (!Files.exists(blobObjectPath)) {
            throw new IOException(" Blob object does not exists " + blobObjectPath.toString());
        }
        content = Utils.readFileContent(blobObjectPath);
    }

    // create absolutely new object from relativeFilePath
    public Blob(Repo repo, Path relativeFilePath) throws IOException {
        super(repo);
        Path absoluteFilePath = repo.trackingDir.resolve(relativeFilePath);

        content = Utils.readFileContent(absoluteFilePath);
        sha = DigestUtils.sha256Hex(content);

//        store
        store();
    }
    public void store() throws IOException {
        Path blobObjectPath = repo.objectsDir.resolve(sha);
        Utils.writeContent(blobObjectPath, repr());
    }

    @Override
    public String repr() {
        return content;
    }

}
