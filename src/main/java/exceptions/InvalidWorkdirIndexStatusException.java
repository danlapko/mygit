package exceptions;

import org.jetbrains.annotations.NotNull;


public class InvalidWorkdirIndexStatusException extends MyGitException {
    public InvalidWorkdirIndexStatusException(@NotNull String relativeFileName) {
        super("FATAL: invalid wroking_dir-index status for file " + relativeFileName);
    }
}
