package exceptions;

import org.jetbrains.annotations.NotNull;


public class InvalidIndexHeadStatusException extends MyGitException {
    public InvalidIndexHeadStatusException(@NotNull String relativeFileName) {
        super("FATAL: invalid index-HEAD status for file " + relativeFileName);
    }
}
