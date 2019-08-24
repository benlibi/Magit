import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
}