import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Utils {

    static String getContentFromZip(String pathToZippedFolder, String pathToUnzips) {
        pathToUnzips = unzipFolderContent(pathToZippedFolder, pathToUnzips);
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(pathToUnzips), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private static String unzipFolderContent(String pathToZippedFolder, String pathToUnzips) {
        try {
            new File(pathToUnzips).mkdirs();
            File destDir = new File(pathToUnzips);
            byte[] buffer = new byte[1024];
            ZipInputStream zis = null;
            zis = new ZipInputStream(new FileInputStream(pathToZippedFolder));
            ZipEntry zipEntry = zis.getNextEntry();
            File newFile = newFile(destDir, zipEntry);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zis.closeEntry();
            zis.close();

            return newFile.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    static void createBranchTree(String[] folderStructure, String folderPath, String objectDirPath, String magitDirPath) {

        Arrays.stream(folderStructure)
                .forEach(content -> {
                    if (content.contains("folder")) {
                        String folderSha1 = content.split(",")[1];
                        String[] folderContent = Utils.getContentFromZip(
                                objectDirPath.concat("/" + folderSha1),
                                magitDirPath.concat("temp/resources/mainFolderContent")).split("\n");

                        new File(folderPath.concat("/" + content.split(",")[0])).mkdir();
                        createBranchTree(folderContent, folderPath.concat("/" + content.split(",")[0]), objectDirPath, magitDirPath);

                    } else if (content.contains("file")) {
                        String blopSha1 = content.split(",")[1];
                        String blobContent = Utils.getContentFromZip(
                                objectDirPath.concat("/" + blopSha1),
                                magitDirPath.concat("temp/resources/mainFolderContent")).trim();
                        createUserFile(folderPath, content.split(",")[0], blobContent);
                    }
                });


    }

    public static void createRepoFile(String path, String name,String remotePath, String remoteName) throws IOException {
        File file = new File(path, "remote");
        if (file.createNewFile()){
            String s;
            if(remotePath.equals("")){
                s = name;
            }else{
                s = name + "," + remotePath + "," + remoteName;
            }
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(s);
            output.close();
        }
    }

    public static void createDir(String path){
        File file = new File(path);
        file.mkdir();
    }

    private static void createUserFile(String path, String fileName, String blobContent) {
        try {
            File file = new File(path, fileName);
            if (file.createNewFile()) {
                BufferedWriter output = new BufferedWriter(new FileWriter(file));
                output.write(blobContent);
                output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFolderContent(String source, String destination) throws IOException {
        File srcDir = new File(source);
        File destDir = new File(destination);
        FileUtils.copyDirectory(srcDir, destDir);
    }

    public static void moveFile(String sourceFile, String destination) throws IOException {
        File srcFile = new File(sourceFile);
        File destDir = new File(destination);
        FileUtils.moveFile(srcFile,destDir);
    }

    public static void copyFile(String sourceFile, String destination) throws IOException {
        Path srcFile = FileSystems.getDefault().getPath(sourceFile);
        Path destDir =FileSystems.getDefault().getPath(destination);
        //FileUtils.copyFile(srcFile,destDir);
        Files.copy(srcFile, destDir, StandardCopyOption.REPLACE_EXISTING);
    }

    public static boolean isRemoteExist(String branchName, String remotePath){
        File file = new File(remotePath + "/"  + branchName);
        return file.exists();
    }
}