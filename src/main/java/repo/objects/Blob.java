package repo.objects;

import repo.Repo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Blob extends GitObject {
    private final String content;

    public Blob(Repo repo, String sha) throws IOException {
        super(repo, sha);

        Path blobObjectPath = repo.objectsDir.resolve(sha);
        if (!Files.exists(blobObjectPath)) {
            throw new IOException(" Blob object does not exists " + blobObjectPath.toString());
        }
        content = new String(Files.readAllBytes(blobObjectPath), StandardCharsets.UTF_8);
    }

    public Blob(Repo repo, Path path) throws IOException, NoSuchAlgorithmException {
        super(repo, "");
        content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        sha = DigestUtils.sha256Hex(content);

//        store
        Path blobObjectPath = repo.objectsDir.resolve(sha);
        Files.write(blobObjectPath, Arrays.asList(repr().split("\n")));
    }

    @Override
    public String repr() {
        return content;
    }

}
