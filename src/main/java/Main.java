import org.apache.commons.codec.digest.DigestUtils;
import repo.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Path last_txt =  Paths.get("/home/danila/se_6f/java/hw/myGit/mytest/last.txt");
        Path obj =  Paths.get("/home/danila/se_6f/java/hw/myGit/mytest/.mygit/objects/082e6b88dca40cb298ca28e9ad1ed0da8b215b310046806f68b84b51e418d436");

        String lastContetn = Utils.readFileContent(last_txt);
        String objContetn = Utils.readFileContent(obj);


        System.out.println(DigestUtils.sha256Hex(lastContetn));
        System.out.println(DigestUtils.sha256Hex(objContetn));

    }
}
