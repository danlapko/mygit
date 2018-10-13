package exceptions;

import org.jetbrains.annotations.NotNull;


public class TreeNotExistsException extends MyGitException {
    public TreeNotExistsException(@NotNull String treeSha) {
        super("FATAL: tree " + treeSha + " does not exists");
    }
}
