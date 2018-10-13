package exceptions;

import org.jetbrains.annotations.NotNull;

public class CommandStateException extends MyGitException {
    public CommandStateException(@NotNull String s) {
        super(s);
    }
}
