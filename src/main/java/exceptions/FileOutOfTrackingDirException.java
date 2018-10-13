package exceptions;

import org.jetbrains.annotations.NotNull;

import repo.Repo;

public class FileOutOfTrackingDirException extends MyGitException {
    public FileOutOfTrackingDirException(@NotNull Repo repo, @NotNull String absoluteFileName) {
        super("FATAL: working dir path: " + repo.trackingDir.toString() + "; file path:" + absoluteFileName);
    }
}
