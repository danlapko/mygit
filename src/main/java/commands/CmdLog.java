package commands;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import repo.Repo;
import repo.objects.Commit;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

@Command(name = "log", description = "logs commits history from head or from revision")
public class CmdLog implements GitCommand {

    @Arguments(description = "log from revision", required = false)
    private String revisionSha = "";

    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {
        boolean readyToLog = false;
        if (revisionSha.equals(""))
            readyToLog = true;

        Deque<String> deque = new ArrayDeque<>();

        Commit currentCommit = repo.head.getCommit();

        currentCommit.logVisit(deque, revisionSha, readyToLog, new HashSet<>());

        while (!deque.isEmpty()) {
            System.out.println();
            System.out.println(deque.pop());
        }

        return 0;
    }
}