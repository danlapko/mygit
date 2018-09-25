package repo;

import java.util.List;

public class Index {
    private final Repo repo;

    private final

    public Index(Repo repo) {
        this.repo = repo;
    }
}


abstract class Node {
    private String name;
}

class InternalNode extends Node {
    List<Node> children;
}

class Leaf {

}