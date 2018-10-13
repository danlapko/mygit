package exceptions;

import org.jetbrains.annotations.NotNull;
import repo.objects.Tree;


public class TreeNotContainsPathException extends MyGitException {
    public TreeNotContainsPathException(@NotNull Tree tree, @NotNull String relativeFileName) {
        super("FATAL: tree "+ tree.sha+" doesn't contains path " + relativeFileName);
    }
}
