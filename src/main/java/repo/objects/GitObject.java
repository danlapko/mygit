package repo.objects;

import repo.Repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class GitObject {
    final Repo repo;
    public String sha;

    //  create from existing object in objects dir
    GitObject(Repo repo, String objectSha) throws IOException {
        this.repo = repo;
        this.sha = objectSha;

        Path objectPath = repo.objectsDir.resolve(objectSha);
        if (!Files.exists(objectPath)) {
            throw new IOException();
        }
    }


    abstract public String repr() throws Exception;

}

