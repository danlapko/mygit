package repo.objects;

import repo.Repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class GitObject {
    final Repo repo;
    public String sha;


    public GitObject(Repo repo, String sha) throws IOException {
        this.repo = repo;
        this.sha = sha;

        Path objectPath = repo.objectsDir.resolve(sha);
        if (!Files.exists(objectPath)) {
            throw new IOException();
        }
    }




    abstract public String repr() throws Exception;

}

