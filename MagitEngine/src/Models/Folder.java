package Models;

import generated.MagitSingleFolder;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Folder {
    private static final String MAGIT_HOME_DIR_PATH = ".magit";

    private String name;
    private String folderSha1;
    private String path;
    private String parentDir;
    private String owner = User.getName();
    private String lastModifyDate;
    private List<Blob> childBlobs = new ArrayList<>();
    private List<Folder> childFolders = new ArrayList<>();

    Folder(MagitSingleFolder folder, List<Blob> magitChildBlobs, List<Folder> magitChildFolders, File folderFile) {
        this.name = folderFile.getName();
        this.lastModifyDate = folder.getLastUpdateDate();
        this.owner = folder.getLastUpdater();
        this.path = folderFile.getPath();
        this.parentDir = folderFile.getParentFile().getName();
        this.childBlobs = magitChildBlobs;
        this.childFolders = magitChildFolders;
        this.folderSha1 = this.getDirContentToSha1();
    }

    public Folder(List<Blob> magitChildBlobs, List<Folder> magitChildFolders, File folderFile) {
        this.name = folderFile.getName();
        this.setOwner(User.getName());
        this.defineLastModifyDate();
        this.path = folderFile.getPath();
        this.parentDir = folderFile.getParentFile().getName();
        this.childBlobs = magitChildBlobs;
        this.childFolders = magitChildFolders;
        this.folderSha1 = this.getDirContentToSha1();
    }

    public Folder(String path) {
        this.path = path;
        List<Folder> allChildFolders = getSubFoldersRecursively();
        setAllChildFolders(allChildFolders);
        setSubBlobsRecursively();
        this.name = new File(this.path).getName();
        this.folderSha1 = this.getDirContentToSha1();
        this.parentDir = new File(this.path).getParentFile().getName();
        this.setOwner(User.getName());
        this.defineLastModifyDate();
    }

    private void setAllChildFolders(List<Folder> allChildFolders) {
        File currentFolder = new File(path);
        for (Folder subFolder : allChildFolders) {
            File subFolderFile = new File(subFolder.getPath());
            if (currentFolder.getPath().equals(subFolderFile.getParent())) {
                childFolders.add(subFolder);
            }
            ;
        }
    }

    public void createFolderRepresentation(String path) {
        try {
            File file = new File(path, this.getDirContentToSha1());
            ZipOutputStream out;
            out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(this.getDirContentToSha1());

            out.putNextEntry(e);
            out.write(this.getDirContentToString().getBytes());
            out.closeEntry();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getChildBlobsSha1() {
        List<String> ChildBlobsListString = new ArrayList<>();
        for (Folder childFolder : childFolders) {
            getChildBlobsSha1(ChildBlobsListString, childFolder);
        }
        for (Blob childBlob : childBlobs) {
            ChildBlobsListString.add(childBlob.getBlobSha1());
        }
        return ChildBlobsListString;
    }

    private List<String> getChildBlobsSha1(List<String> ChildBlobsListString, Folder rootfolder) {
        for (Folder childFolder : rootfolder.getChildFolders()) {
            getChildBlobsSha1(ChildBlobsListString, childFolder);
        }
        for (Blob childBlob : rootfolder.getChildBlobs()) {
            ChildBlobsListString.add(childBlob.getBlobSha1());
        }
        return ChildBlobsListString;
    }

    public List<String> getChildBlobsReqStringList() {
        List<String> ChildBlobsListString = new ArrayList<>();
        for (Folder childFolder : childFolders) {
            getChildBlobsReqStringList(ChildBlobsListString, childFolder);
        }
        for (Blob childBlob : childBlobs) {
            ChildBlobsListString.add(path + "/" + childBlob.getName());
        }
        return ChildBlobsListString;
    }

    private List<String> getChildBlobsReqStringList(List<String> ChildBlobsListString, Folder rootfolder) {
        for (Folder childFolder : rootfolder.getChildFolders()) {
            getChildBlobsReqStringList(ChildBlobsListString, childFolder);
        }

        for (Blob childBlob : rootfolder.getChildBlobs()) {
            ChildBlobsListString.add(rootfolder.getPath() + "/" + childBlob.getName());
        }
        return ChildBlobsListString;
    }

    private void setSubBlobsRecursively() {
        File currentFolder = new File(path);
        for (File file : currentFolder.listFiles()) {
            if (!file.isDirectory()) {
                Blob blob = new Blob(file);
                childBlobs.add(blob);
            }
        }
    }

    private List<Folder> getSubFoldersRecursively() {
        try {
            return Files.walk(Paths.get(this.path))
                    .filter(Files::isDirectory)
                    .filter(path -> !path.normalize().toAbsolutePath().toString().contains(MAGIT_HOME_DIR_PATH))
                    .map(Path::normalize)
                    .map(Path::toString)
                    .filter(Folder::isNotEmptyDirectory)
                    .filter(path1 -> !path1.equals(this.path))
                    .map(Folder::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static boolean isNotEmptyDirectory(String path) {

        try {
            return Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .filter(subPath -> !subPath.getFileName().startsWith("."))
                    .filter(subPath -> !subPath.normalize().toAbsolutePath().toString().contains(MAGIT_HOME_DIR_PATH))
                    .map(Path::toFile)
                    .map(Blob::new)
                    .count() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        String delimiter = ",";
        String nextLine = "\n";
        String folderKey = "folder";

        return this.name + delimiter + this.folderSha1 + delimiter + folderKey + delimiter + this.owner + delimiter +
                this.lastModifyDate + nextLine;
    }

    public String getDirContentToString() {

        final String[] ans = {""};
        childFolders.stream()
                .filter(folder -> folder.parentDir.equals(this.name))
                .forEach(folder -> ans[0] += folder.toString());
        childBlobs.stream()
                .filter(blob -> blob.getParentDir().equals(this.name))
                .forEach(blob -> ans[0] += blob.toString());

        return ans[0];
    }

    private String getDirContentToSha1() {

        String delimiter = ",";
        String nextLine = "\n";
        String folderKey = "folder";
        String fileKey = "file";

        final String[] ans = {""};
        childFolders.stream()
                .filter(folder -> folder.parentDir.equals(this.name))
                .sorted(Comparator.comparing(p -> p.name))
                .forEach(folder -> ans[0] += folder.name + delimiter + folder.folderSha1 + delimiter + folderKey + nextLine);
        childBlobs.stream()
                .filter(blob -> blob.getParentDir().equals(this.name))
                .sorted(Comparator.comparing(Blob::getName))
                .forEach(blob -> ans[0] += blob.getName() + delimiter + blob.getBlobSha1() + delimiter + fileKey + nextLine);

        return DigestUtils.sha1Hex(ans[0]);
    }

    public String getFolderSha1() {
        return folderSha1;
    }

    public String getPath() {
        return path;
    }

    public List<Blob> getChildBlobs() {
        return childBlobs;
    }

    public List<Folder> getChildFolders() {
        return childFolders;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setChildBlobs(List<Blob> blobs) {
        this.childBlobs = blobs;
    }

    public void setChildFolders() {
        this.childFolders = getSubFoldersRecursively();
    }

    public void setChildFolders(List<Folder> folders) {
        this.childFolders = folders;
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

    public String getName() {
        return name;
    }

}