package repo;

import repo.objects.Blob;
import repo.objects.GitObject;

import java.nio.file.Path;
import java.util.Map;

public interface GitGettable {
    GitObject get(Path relativeFilePath);

    /**
     * @return relativeFileName -> blob
     */
    Map<String, Blob> getAll();

    boolean contains(Path relativeFilePath);

    boolean empty();
}
