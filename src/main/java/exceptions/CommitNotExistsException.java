package exceptions;

import org.jetbrains.annotations.NotNull;


public class CommitNotExistsException extends MyGitException {
    public CommitNotExistsException(@NotNull String commitSha) {
        super("FATAL: commit " + commitSha + " does not exists");
    }
}
