import commands.GitCommand;
import commands.Init;
import io.airlift.airline.Cli;
import io.airlift.airline.ParseException;
import repo.Repo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyGit {
    public static void main(String[] args) throws IOException {
        @SuppressWarnings("unchecked")
        Cli.CliBuilder<GitCommand> builder = Cli.<GitCommand>builder("mygit")
                .withDescription("Dead simple VCS")
                .withCommands(
                        Init.class
                );
        Cli<GitCommand> parser = builder.build();

        // parsing command
        GitCommand cmd;

        try {
            cmd = parser.parse(args);
        } catch (ParseException e) {
            System.out.println("FATAL: " + e.getMessage());
            return;
        }

        // trying to find already initialized repository
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Repo repo = new Repo(workingDir);

        // in case repository not found and command is not useful without it
        // print error
        if (!repo.load() && !(cmd instanceof Init)) {
            System.err.println("FATAL: can't find wit repository near " + workingDir);
            return;
        }

        // executing command
        try {
            cmd.execute(repo, workingDir);
        } catch (IOException e) {
            System.err.println("FATAL: Repository write/read failed!"+e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("FATAL: I don't know what have happened");
            e.printStackTrace();
        }
    }
}
