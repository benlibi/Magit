package Models;

import generated.MagitBlob;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Blob {

    private String name;
    private String content;
    private String blobSha1;
    private String owner;
    private String lastModifyDate;
    private String parentDir;

    public Blob(File file) {
        this.name = file.getName();
        this.parentDir = file.getParentFile().getName();

        try {
            this.content = this.readFile(file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.blobSha1 = DigestUtils.sha1Hex(content);
        this.defineLastModifyDate();
        this.owner = User.getName();
    }

    public Blob(MagitBlob maBlob, File blob) {
        this.name = maBlob.getName();
        this.content = maBlob.getContent();
        this.blobSha1 = DigestUtils.sha1Hex(maBlob.getContent());
        this.owner = maBlob.getLastUpdater();
        this.lastModifyDate = maBlob.getLastUpdateDate();
        this.parentDir = blob.getParentFile().getName();
    }

    public void createBlobRepresentation(String path) {
        try {
            File file = new File(path, this.blobSha1);
            ZipOutputStream out = null;
            out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(this.blobSha1);

            out.putNextEntry(e);
            out.write(this.content.getBytes());
            out.closeEntry();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    @Override
    public String toString() {
        String delimiter = ",";
        String nextLine = "\n";
        String fileKey = "file";

        return this.name + delimiter + this.blobSha1 + delimiter + fileKey + delimiter + this.owner + delimiter + this.lastModifyDate +
                nextLine;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBlobSha1() {
        return blobSha1;
    }

    String getParentDir() {
        return parentDir;
    }

    public String getOwner() {
        return owner;
    }

    public String getLastModifyDate() {
        return lastModifyDate;
    }

    public void defineLastModifyDate() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:sss");
        this.lastModifyDate = formatter.format(date);
    }

    public void setLastModifyDate(String lastModifyDate) {
        this.lastModifyDate = lastModifyDate;
    }
}