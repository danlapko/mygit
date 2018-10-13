package exceptions;


import org.jetbrains.annotations.NotNull;

public class BranchAlreadyExistsException extends MyGitException {
    public BranchAlreadyExistsException(@NotNull String branchName) {
        super("FATAL: branch " + branchName + " already exists");
    }
}
