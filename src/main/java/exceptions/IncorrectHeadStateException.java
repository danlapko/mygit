package exceptions;


import repo.GitGettable;
import org.jetbrains.annotations.NotNull;

public class IncorrectHeadStateException extends MyGitException {
    public IncorrectHeadStateException(@NotNull GitGettable pointer) {
        super("FATAL: incorrect head state " + pointer);
    }
}
