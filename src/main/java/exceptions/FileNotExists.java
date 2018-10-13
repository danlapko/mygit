package exceptions;

import org.jetbrains.annotations.NotNull;


public class FileNotExists extends MyGitException {
    public FileNotExists(@NotNull String absoluteFileName) {
        super("FATAL: file not exists " + absoluteFileName);
    }
}
