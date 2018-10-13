


import exceptions.MyGitException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import repo.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Tests {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    private static void execCmd(String commandLine) throws Exception {
        final String[] args = commandLine.split(" ");
        MyGit.main(args);
    }

    private static String readFile(String relativeFileName) throws IOException {
        return Utils.readFileContent(Paths.get(relativeFileName));
    }

    @Before
    public void init() throws Exception {


        new ProcessBuilder("bash", "-c",
                "mkdir test_dir; " +
                        "cd test_dir; " +
//                        "java -jar ../build/libs/myGit-1.0-SNAPSHOT.jar init; " +
                        "touch last.txt empty.txt; echo 'last content' > last.txt; " +
                        "touch README.txt; echo 'readme content' > README.txt; " +
                        "mkdir sub_dir empty_dir; " +
                        "cd sub_dir; touch a.txt b.txt; echo 'a content' > a.txt; echo 'b content' > b.txt;" +
                        "cd ..;").start().waitFor();

        String cmdinit = "init";
        execCmd(cmdinit);
    }


    @After
    public void cleanOut() throws IOException, InterruptedException {
        new ProcessBuilder("bash", "-c", "rm -rf test_dir .mygit").start().waitFor();
    }


    @Test
    public void add_removeFromWorkDir_checkout() throws Exception {
        execCmd("add test_dir/sub_dir");
        new ProcessBuilder("bash", "-c", "cd test_dir; rm -rf *").start().waitFor();
        execCmd("checkout -- test_dir/sub_dir");
        System.out.println(System.getProperty("user.dir"));
        Assert.assertTrue(Files.exists(Paths.get("test_dir/sub_dir/a.txt")));
        Assert.assertTrue(Files.exists(Paths.get("test_dir/sub_dir/b.txt")));
        Assert.assertEquals("a content", readFile("test_dir/sub_dir/a.txt"));
    }

    @Test
    public void commit_newBranch_modify_commit_checkoutToNewBranch_checkoutToMaster() throws Exception {
        execCmd("add test_dir/README.txt");
        execCmd("commit 'my message'");

        execCmd("branch new_branch");

        new ProcessBuilder("bash", "-c", "echo 'modifications' > test_dir/README.txt").start().waitFor();

        execCmd("add test_dir/README.txt");
        execCmd("commit 'my message 2'");

        execCmd("checkout new_branch");

        Assert.assertEquals("readme content", readFile("test_dir/README.txt"));

        execCmd("checkout master");

        Assert.assertEquals("modifications", readFile("test_dir/README.txt"));
    }

    @Test
    public void commit_modify_commit_resetToPrev_tryResetToNext() throws Exception {
        execCmd("add test_dir/README.txt");
        execCmd("commit message1");

        new ProcessBuilder("bash", "-c", "echo 'modifications' > test_dir/README.txt").start().waitFor();

        execCmd("add test_dir/README.txt");
        execCmd("commit message2");

        String commitSha1 = outContent.toString().split("\n")[0];
        String commitSha2 = outContent.toString().split("\n")[1];

        execCmd("log");
//        System.err.println(outContent);

        Assert.assertEquals("modifications", readFile("test_dir/README.txt"));
        execCmd("reset " + commitSha1);
        Assert.assertEquals("readme content", readFile("test_dir/README.txt"));
    }

    @Test
    public void commit_rm_commit_deleteFromWorkDir_checkoutToPrev_checkoutToNext() throws Exception {
        execCmd("add test_dir");
        execCmd("commit message1");

        String commitSha1 = outContent.toString().split("\n")[0];

        execCmd("rm test_dir/README.txt");
        execCmd("commit message2");

        String commitSha2 = outContent.toString().split("\n")[1];

        new ProcessBuilder("bash", "-c", "cd test_dir; rm -rf *").start().waitFor();

        execCmd("log");
//        System.err.println(outContent);

        Assert.assertTrue(!Files.exists(Paths.get("test_dir/README.txt")));

        execCmd("checkout " + commitSha1);
        Assert.assertTrue(Files.exists(Paths.get("test_dir/README.txt")));

        execCmd("checkout " + commitSha2);
        Assert.assertTrue(!Files.exists(Paths.get("test_dir/README.txt")));
    }

    @Test
    public void commit_newBranch_modifyREADMEinBothBranches_merge() throws Exception {
        execCmd("add test_dir");
        execCmd("commit message1");


        execCmd("branch new_branch");

        new ProcessBuilder("bash", "-c", "echo 'modifications master' > test_dir/README.txt").start().waitFor();
        execCmd("add test_dir/README.txt");
        execCmd("commit master_modify_readme");

        execCmd("checkout new_branch");

        new ProcessBuilder("bash", "-c", "echo 'modifications new_branch' > test_dir/README.txt").start().waitFor();
        execCmd("add test_dir/README.txt");
        execCmd("commit new_branch_modify_readme");


        execCmd("checkout master");
        execCmd("merge new_branch -m merge_commit");

        String commitSha1 = outContent.toString().split("\n")[0]; // before forking
        String commitSha2 = outContent.toString().split("\n")[1]; // modify in master
        String commitSha3 = outContent.toString().split("\n")[2]; // modify in new_branch
        String commitSha4 = outContent.toString().split("\n")[6]; // merge

//        execCmd("log");
        new ProcessBuilder("bash", "-c", "cd test_dir; rm -rf *").start().waitFor();

        execCmd("checkout " + commitSha1); // before forking
        Assert.assertEquals("readme content", readFile("test_dir/README.txt"));
        System.err.println(outContent);

        execCmd("checkout " + commitSha2); // master before merge
        Assert.assertEquals("modifications master", readFile("test_dir/README.txt"));

        execCmd("checkout " + commitSha3); // new_branch before merge
        Assert.assertEquals("modifications new_branch", readFile("test_dir/README.txt"));

        execCmd("checkout " + commitSha4); // master after before merge
        Assert.assertEquals("<<<<<<< HEAD\n" +
                "modifications master\n" +
                ">>>>>>> \n" +
                "<<<<<<< new_branch\n" +
                "modifications new_branch\n" +
                ">>>>>>> ", readFile("test_dir/README.txt"));

        execCmd("checkout new_branch"); // new_branch tip
        Assert.assertEquals("modifications new_branch", readFile("test_dir/README.txt"));

        execCmd("checkout master"); // master tip
        Assert.assertEquals("<<<<<<< HEAD\n" +
                "modifications master\n" +
                ">>>>>>> \n" +
                "<<<<<<< new_branch\n" +
                "modifications new_branch\n" +
                ">>>>>>> ", readFile("test_dir/README.txt"));

        execCmd("branch -d new_branch");
    }
}
