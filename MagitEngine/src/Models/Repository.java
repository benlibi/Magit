package Models;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Repository {

    private String _path;
    private String _mainProjectSha1;
    private Folder rootFolder;
    private String HEAD_PATH;
    private String name = "";
    public String MAGIT_DIR_PATH;
    public String OBJECTS_DIR_PATH;
    public String BRANCHES_DIR_PATH;


    public Repository(String path, Folder rootFolder) {
        this._path = path;
        MAGIT_DIR_PATH = this._path.concat("/.magit");
        OBJECTS_DIR_PATH = MAGIT_DIR_PATH.concat("/Objects");
        BRANCHES_DIR_PATH = MAGIT_DIR_PATH.concat("/Branches");
        HEAD_PATH = BRANCHES_DIR_PATH.concat("/HEAD");
        this.rootFolder = rootFolder;
    }

    public void createBlankRepository() throws IOException {

        // create the .magit directory
        new File(MAGIT_DIR_PATH).mkdir();
        // create the Branches directory as sub directory of .magit
        new File(BRANCHES_DIR_PATH).mkdirs();
        // create the Objects directory as sub directory of .magit
        new File(OBJECTS_DIR_PATH).mkdirs();

        // Create Main Path Sha1
        this._mainProjectSha1 = DigestUtils.sha1Hex("");
    }

    public void createHead(String branchName) throws IOException {
        File file = new File(HEAD_PATH);
        if (file.createNewFile()) {
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(branchName);
            output.close();
        }
    }

    public void setHead(String val) throws IOException {
        Path filePath = Paths.get(HEAD_PATH);
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(filePath), charset);
        content = content.replaceAll(".+", val);
        Files.write(filePath, content.getBytes(charset));
    }

    public String get_path() {
        return _path;
    }

    public String get_mainProjectSha1() {
        return _mainProjectSha1;
    }

    public void set_mainProjectSha1(String _mainProjectSha1) {
        this._mainProjectSha1 = _mainProjectSha1;
    }

    public void setRootFolder(Folder rootFolder) {
        this.rootFolder = rootFolder;
    }

    public Folder getRootFolder() {
        return rootFolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

