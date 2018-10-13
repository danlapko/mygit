package exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidTreeObjectStructureException extends MyGitException {
    public InvalidTreeObjectStructureException(@NotNull String treeShA) {
        super("FATAL: treeSha = " +treeShA);
    }
}
