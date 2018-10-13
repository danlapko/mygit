package exceptions;

import org.jetbrains.annotations.NotNull;

public class BlobNotExistsException extends MyGitException {
    public BlobNotExistsException(@NotNull String blobSha) {
        super("FATAL: blob " + blobSha + " does not exists");
    }
}
