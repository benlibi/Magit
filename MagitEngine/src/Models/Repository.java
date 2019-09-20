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
import java.util.stream.Stream;

public class Repository {
    private String remote_path="";
    private String _path;
    private String _mainProjectSha1;
    private String HEAD_PATH;
    private String name;
    private String remote_name = "";
    public String MAGIT_DIR_PATH;
    public String OBJECTS_DIR_PATH;
    public String BRANCHES_DIR_PATH;
    public boolean isRemote=false;


    public Repository(String path) {
        this._path = path;
        MAGIT_DIR_PATH = this._path.concat("/.magit");
        OBJECTS_DIR_PATH = MAGIT_DIR_PATH.concat("/Objects");
        BRANCHES_DIR_PATH = MAGIT_DIR_PATH.concat("/Branches");
        HEAD_PATH = BRANCHES_DIR_PATH.concat("/HEAD");
        String[] repoFile = getRepoFile(MAGIT_DIR_PATH.concat("/remote"));
        name=repoFile[0];
        if(repoFile.length>1){
            isRemote=true;
            remote_path=repoFile[1];
            remote_name=repoFile[2];
        }
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


    public String getRemote_path() {
        if(remote_path.equals("")){
            return _path;
        }
        return remote_path;    }

    public String getRemote_name() {
        if(remote_name.equals("")){
            return name;
        }
        return remote_name;
    }

    public String getName() {
        return name;
    }

    public String[] getRepoFile(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString().trim().split(",");
    }

    public String getRemoteString(){
        if(remote_path.equals("")){
            return "Remote Repo";
        }else{
            return "Local Repo Cloned From Repo: " + remote_name + "From Path: " + remote_path;
        }
    }

    public void setName(String name) {
        this.name = name;
    }
}

