package exceptions;

import org.jetbrains.annotations.NotNull;


public class InvalidBlobStateException extends MyGitException {
    public InvalidBlobStateException(@NotNull String blobSha, @NotNull String blobContent) {
        super("FATAL: blob name " + blobSha + " is not the sha of its content " + blobContent);
    }
}
