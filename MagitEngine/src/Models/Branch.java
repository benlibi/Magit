package Models;

import generated.MagitSingleBranch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Branch {

    private String name;
    private Commit pCommit;

    Branch(MagitSingleBranch branch, Commit pointedCommit) {
        name = branch.getName();
        pCommit = pointedCommit;
    }

    public Branch(String name, Commit pCommit) {
        this.name = name;
        this.pCommit = pCommit;
    }

    public static String getBranchCommitPointer(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString().trim();
    }

    public void createBranchFile(String branchFilePath) throws IOException {
        File file = new File(branchFilePath, name);
        if (file.createNewFile()) {
            if (pCommit != null) {
                BufferedWriter output = new BufferedWriter(new FileWriter(file));
                output.write(pCommit.getCommitSha1());
                output.close();
            }
        }
    }

    public void setBranchFile(String branchName, String commitSha1, String branchFilePath) throws IOException {
        Path filePath = Paths.get(branchFilePath.concat("/" + branchName));
        Charset charset = StandardCharsets.UTF_8;
        Files.write(filePath, commitSha1.getBytes(charset));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setpCommit(Commit pCommit) {
        this.pCommit = pCommit;
    }
}