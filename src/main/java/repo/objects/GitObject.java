package repo.objects;

import repo.Repo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class GitObject {
    final Repo repo;
    public String sha;

    //  create absolutely new object without storing (don't forget to store after sha calculated)
    GitObject(Repo repo){
        this.repo = repo;
        sha = "";
    }

    //  create from existing object in objects dir
    GitObject(Repo repo, String objectSha) throws FileNotFoundException {
        this.repo = repo;
        this.sha = objectSha;

        Path objectPath = repo.objectsDir.resolve(objectSha);
        if (!Files.exists(objectPath)) {
            throw new FileNotFoundException();
        }
    }

    abstract public String repr() throws Exception;

}

