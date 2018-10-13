package exceptions;

import org.jetbrains.annotations.NotNull;


public class BranchNotExistsException extends MyGitException {
    public BranchNotExistsException(@NotNull String branchName) {
        super("FATAL: branch " + branchName + " does not exists");
    }
}
