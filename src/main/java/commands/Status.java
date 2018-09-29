package commands;

import io.airlift.airline.Command;
import repo.INDEX_HEAD_STATUS;
import repo.Repo;
import repo.STATUS;
import repo.WORKDIR_INDEX_STATUS;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Command(name = "status", description = "prints modified/deleted/not_tracked files")
public class Status implements GitCommand {
    @Override
    public int execute(Repo repo, Path workingDir) throws Exception {

        Map<String, STATUS> statuses = repo.getFilesStatuses();

        // changes to be committed
        Map<INDEX_HEAD_STATUS, List<String>> changesToBeCommited = new HashMap<>();
        for (INDEX_HEAD_STATUS possibleStatus : INDEX_HEAD_STATUS.values()) {
            changesToBeCommited.put(possibleStatus, new LinkedList<>());
        }

        for (Map.Entry<String, STATUS> entry : statuses.entrySet()) {
            String relativeFileName = entry.getKey();
            STATUS status = entry.getValue();
            changesToBeCommited.get(status.indexHeadStatus).add(relativeFileName);
        }

        System.out.println("\nCHANGES TO BE COMMITTED:");

        System.out.println("\tNEW_FILE:");
        for (String relativeName : changesToBeCommited.get(INDEX_HEAD_STATUS.NEWFILE)) {
            System.out.println("\t\t" + relativeName);
        }

        System.out.println("\tMODIFIED:");
        for (String relativeName : changesToBeCommited.get(INDEX_HEAD_STATUS.MODIFIED)) {
            System.out.println("\t\t" + relativeName);
        }

        System.out.println("\tDELETED:");
        for (String relativeName : changesToBeCommited.get(INDEX_HEAD_STATUS.DELETED)) {
            System.out.println("\t\t" + relativeName);
        }



        // unstaged changes
        Map<WORKDIR_INDEX_STATUS, List<String>> unstagedChanges = new HashMap<>();
        for (WORKDIR_INDEX_STATUS possibleStatus : WORKDIR_INDEX_STATUS.values()) {
            unstagedChanges.put(possibleStatus, new LinkedList<>());
        }

        for (Map.Entry<String, STATUS> entry : statuses.entrySet()) {
            String relativeFileName = entry.getKey();
            STATUS status = entry.getValue();
            unstagedChanges.get(status.workdirIndexStatus).add(relativeFileName);
        }

        System.out.println("\nUNSTAGED CHANGES:");

        System.out.println("\tUNTRACKED:");
        for (String relativeName : unstagedChanges.get(WORKDIR_INDEX_STATUS.UNTRACKED)) {
            System.out.println("\t\t" + relativeName);
        }

        System.out.println("\tMODIFIED:");
        for (String relativeName : unstagedChanges.get(WORKDIR_INDEX_STATUS.MODIFIED)) {
            System.out.println("\t\t" + relativeName);
        }

        System.out.println("\tDELETED:");
        for (String relativeName : unstagedChanges.get(WORKDIR_INDEX_STATUS.DELETED)) {
            System.out.println("\t\t" + relativeName);
        }

        return 0;
    }

}
