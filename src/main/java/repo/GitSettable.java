package repo;

import repo.objects.Blob;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface GitSettable {
    void add(Path relativeFilePath) throws IOException;

    /**
     * @enities relativeFileName -> blob
     */
    void addAll(Set<Path> paths) throws IOException;

    void remove(Path relativeFilePath) throws IOException;

    /**
     * @enities relativeFileName -> blob
     */
    void removeAll(Set<Path> paths) throws IOException;

}
