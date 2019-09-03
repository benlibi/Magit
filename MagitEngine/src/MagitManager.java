import Models.*;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class MagitManager {

    private User currentUser = new User();
    private Branch currentBranch;
    protected Commit currentCommit;
    protected Repository currentRepo;
    private Folder latestFolderReflection;

    protected void deleteBranch(String name) throws IOException {
        File branchFile = new File(this.currentRepo.BRANCHES_DIR_PATH.concat("/" + name));
        FileUtils.forceDelete(branchFile);
    }

    protected ArrayList<String> getAvailableBranches() {
        ArrayList<String> availableBranches = new ArrayList<>();
        File[] branchFiles = new File(this.currentRepo.BRANCHES_DIR_PATH).listFiles();
        Arrays.stream(branchFiles)
                .map(File::getName)
                .filter(branchFile -> !branchFile.equals("HEAD"))
                .filter(branchFile -> !branchFile.equals(this.currentBranch.getName()))
                .forEach(availableBranches::add);

        availableBranches.add(this.currentBranch.getName() + " (HEAD)");

        return availableBranches;
    }

    protected List<String> showCommit() throws RuntimeException {
        if (this.currentRepo != null &&
                this.currentCommit != null) {
            Folder rootFolder = this.latestFolderReflection;
            return showCommit(rootFolder);
        } else {
            throw new RuntimeException("Operation Not Available, Please Commit Your Changes First");
        }
    }

    private List<String> showCommit(Folder rootFolder) {
        List<String> commitDetails = new ArrayList<>();
        commitDetails.add(rootFolder.getPath() + ":");
        commitDetails.add(rootFolder.getDirContentToString());

        rootFolder.getChildFolders().forEach(folder -> {
            commitDetails.add(folder.getPath() + ":");
            commitDetails.add(folder.getDirContentToString());
        });

        return commitDetails;
    }

    public void loadRepository(String newRepoPath) throws FileNotFoundException, IOException {

        File[] repoFiles = new File(newRepoPath).listFiles();

        if (repoFiles == null || Arrays.stream(repoFiles).noneMatch(file -> file.getName().equals(".magit"))) {
            throw new FileNotFoundException("Repository Not Exist\nPlease Create It First And Try Again");
        }

        this.currentRepo = new Repository(newRepoPath, null);
        String branchName = readBranchFile("HEAD");
        String branchCommitSha1 = readBranchFile(branchName);
        String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + branchCommitSha1),
                this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));

        currentCommit = new Commit(commitRepresentation.replace("\n", ""));
        checkoutBranchNewWC(branchName);
    }

    protected void checkoutBranch(String branchName, boolean forceCheckout) throws IOException, RuntimeException {
        Folder mainFolder = new Folder(this.currentRepo.get_path());
        if (isChangesFound(mainFolder)) {
            throw new RuntimeException("Changes Detected!");
        }

        if (forceCheckout) {
            checkoutRevision(branchName);
            mainFolder = new Folder(this.currentRepo.get_path());
            this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());
            this.currentRepo.setHead(branchName);
            String commitSha1 = readBranchFile(branchName);
            String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + commitSha1),
                    this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            this.currentCommit = new Commit(commitRepresentation.replace("\n", ""));
            this.currentBranch = new Branch(branchName, this.currentCommit);
            this.latestFolderReflection = mainFolder;
        }
    }

    private void checkoutRevision(String branchName) {
        deleteWorkingDir();
        String[] mainFolderContent = unzipMainFolderFiles(readBranchFile(branchName));
        createRepoTree(mainFolderContent);
    }

    private void checkoutBranchToMaster() throws IOException, RuntimeException {
        Folder mainFolder = new Folder(this.currentRepo.get_path());
        if (!isChangesFound(mainFolder)) {
            deleteWorkingDir();
            String[] mainFolderContent = unzipMainFolderFiles(readBranchFile("master"));
            createRepoTree(mainFolderContent);

            this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());
            this.currentRepo.setHead("master");
            this.currentBranch = new Branch("master", this.currentCommit);
            this.latestFolderReflection = mainFolder;
            this.currentRepo.set_mainProjectSha1(this.latestFolderReflection.getFolderSha1());
            this.currentRepo.setRootFolder(this.latestFolderReflection);
        } else {
            throw new RuntimeException("Changes Detected!\nPlease Commit Your Change First");
        }
    }

    private void checkoutBranchNewWC(String branchName) throws IOException {
        Folder mainFolder = new Folder(this.currentRepo.get_path());
        deleteWorkingDir();
        String[] mainFolderContent = unzipMainFolderFiles(readBranchFile(branchName));
        createRepoTree(mainFolderContent);

        this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());
        this.currentRepo.setHead(branchName);
        this.currentBranch = new Branch(branchName, this.currentCommit);
        this.latestFolderReflection = mainFolder;
        this.currentRepo.set_mainProjectSha1(this.latestFolderReflection.getFolderSha1());
        this.currentRepo.setRootFolder(this.latestFolderReflection);
    }

    private void checkoutBranchNewWC(String branchName, Folder mainFolder) throws IOException {
        deleteWorkingDir();
        String[] mainFolderContent = unzipMainFolderFiles(readBranchFile(branchName));
        createRepoTree(mainFolderContent);

        this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());
        this.currentRepo.setHead(branchName);
        this.currentBranch = new Branch(branchName, this.currentCommit);
        this.latestFolderReflection = mainFolder;
        this.currentRepo.set_mainProjectSha1(this.latestFolderReflection.getFolderSha1());
        this.currentRepo.setRootFolder(this.latestFolderReflection);
    }

    private void createRepoTree(String[] mainFolderContent) {
        Utils.createBranchTree(mainFolderContent, this.currentRepo.get_path(), this.currentRepo.OBJECTS_DIR_PATH,
                this.currentRepo.MAGIT_DIR_PATH);
    }

    private String[] unzipMainFolderFiles(String branchPointer) {
        String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + branchPointer),
                this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));

        Commit tmp = new Commit(commitRepresentation.replace("\n", ""));

        return Utils.getContentFromZip(
                this.currentRepo.OBJECTS_DIR_PATH.concat("/" + tmp.getMainRepoSha1()),
                this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/mainFolderContent")).split("\n");
    }

    private String readBranchFile(String branchName) {
        return Branch.getBranchCommitPointer(this.currentRepo.BRANCHES_DIR_PATH.concat("/" + branchName));
    }

    private void deleteWorkingDir() {
        try (Stream<File> files = Arrays.stream(Objects.requireNonNull(new File(this.currentRepo.get_path()).listFiles()))) {
            files
                    .filter(file -> !file.getAbsolutePath().contains(".magit"))
                    .forEach(directory -> {
                        try {
                            FileUtils.forceDelete(directory);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void deleteRepo(String path) {
        try (Stream<File> files = Arrays.stream(Objects.requireNonNull(new File(path).listFiles()))) {
            files
                    .forEach(directory -> {
                        try {
                            FileUtils.forceDelete(directory);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void showBranchCommitHistory() {
        printCommitHistory(currentCommit);
    }

    private void printCommitHistory(Commit commit) {
        ConsoleMenu.displayMsg(commit.commitInfo());
        List<String> previousCommitsSha1 = commit.getCommitHistory();
        for (String previousCommitSha1 : previousCommitsSha1) {
            String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + previousCommitSha1),
                    this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            Commit previousCommit = new Commit(commitRepresentation.replace("\n", ""));
            printCommitHistory(previousCommit);
        }
    }

    private List<String> cretaeSha1List(File[] objectFiles) {
        List<String> sha1List = new ArrayList<String>();
        for (File objectFile : objectFiles) {
            sha1List.add(objectFile.getName());
        }
        return sha1List;
    }

    private Folder createRepo(List<String> sha1List, File rootFolder, Folder lastRootFolderRepresentation) {
        File[] files = rootFolder.listFiles();
        List<Blob> childBlobs = new ArrayList<>();
        List<Folder> childFolders = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().equals(".magit") && !file.getName().equals(".magittemp")) {
                    Folder relevantChildFolder = null;
                    for (Folder childFolder : lastRootFolderRepresentation.getChildFolders()) {
                        if (childFolder.getName().equals(file.getName())) {
                            relevantChildFolder = childFolder;
                        }
                    }
                    childFolders.add(createRepo(sha1List, file, relevantChildFolder));
                }
            } else {
                Blob blob = new Blob(file);
                if (!sha1List.contains(blob.getBlobSha1())) {
                    blob.createBlobRepresentation(currentRepo.OBJECTS_DIR_PATH);
                } else {
                    for (Blob childBlob : lastRootFolderRepresentation.getChildBlobs()) {
                        if (childBlob.getName().equals(file.getName())) {
                            blob = childBlob;
                        }
                    }
                }
                childBlobs.add(blob);
            }
        }
        Folder folder = new Folder(childBlobs, childFolders, rootFolder);
        if (!sha1List.contains(folder.getFolderSha1())) {
            folder.createFolderRepresentation(currentRepo.OBJECTS_DIR_PATH);
        } else {
            folder = lastRootFolderRepresentation;
        }
        return folder;
    }

    public void commit(String commitMsg) throws IOException {
        Folder currentMainFolderReflection = new Folder(this.currentRepo.get_path());
        File[] objectFiles = new File(this.currentRepo.OBJECTS_DIR_PATH).listFiles();
        List<String> sha1List = cretaeSha1List(objectFiles);
        if (!isChangesFound(currentMainFolderReflection)) {
            throw new IOException("No Changes Detected !");
        } else {
//            Folder rootFolder = createRepo(sha1List, new File(this.currentRepo.get_path()), latestFolderReflection);
            Folder rootFolder = createRepo(sha1List, new File(this.currentRepo.get_path()), currentMainFolderReflection);
            rootFolder.createFolderRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
            if (currentCommit == null) {
                currentCommit = new Commit(commitMsg, rootFolder.getFolderSha1(), null);
            } else {
                currentCommit = new Commit(commitMsg, rootFolder.getFolderSha1(), currentCommit.getCommitSha1());
            }
            currentCommit.createCommitRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
            latestFolderReflection = rootFolder;
            this.currentBranch.setBranchFile(this.currentBranch.getName(),
                    this.currentCommit.getCommitSha1(), this.currentRepo.BRANCHES_DIR_PATH);
        }
    }

    public Map<String, List<String>> showStatus() throws IOException {
        List<String> wcFiles = new ArrayList<>();
        List<String> commitFiles = new ArrayList<>();
        List<String> blobsSha1 = new ArrayList<>();
        List<String> updatedFiles = new ArrayList<>();
        Files.walk(Paths.get(this.currentRepo.get_path()))
                .filter(Files::isRegularFile)
                .filter(path -> !path.normalize().toAbsolutePath().toString().contains(".magit"))
                .filter(path -> !path.normalize().toAbsolutePath().toString().contains(".magittemp"))
                .forEach(path -> wcFiles.add(path.normalize().toAbsolutePath().toString()));
        File[] objectFiles = new File(this.currentRepo.OBJECTS_DIR_PATH).listFiles();
        commitFiles = latestFolderReflection.getChildBlobsReqStringList();
        blobsSha1 = latestFolderReflection.getChildBlobsSha1();
        List<String> deletedFiles = getDeletedFiles(wcFiles, commitFiles);
        List<String> newFiles = getNewFiles(wcFiles, commitFiles);
        getUpdatedFiles(updatedFiles, new File(this.currentRepo.get_path()), blobsSha1);
        Map<String, List<String>> statusMap = new HashMap<>();
        statusMap.put("Deleted Files:", deletedFiles);
        statusMap.put("Updated Files:", updatedFiles);
        statusMap.put("New:", newFiles);
        return statusMap;
    }

    private List<String> getDeletedFiles(List<String> wcFiles, List<String> commitFiles) {
        List<String> deletedFiles = new ArrayList<>();
        for (String commitFile : commitFiles) {
            if (!wcFiles.contains(commitFile)) {
                deletedFiles.add(commitFile);
            }
        }
        return deletedFiles;
    }

    private List<String> getNewFiles(List<String> wcFiles, List<String> commitFiles) {
        List<String> newFiles = new ArrayList<>();
        for (String wcFile : wcFiles) {
            if (!commitFiles.contains(wcFile)) {
                newFiles.add(wcFile);
            }
        }
        return newFiles;
    }

    private void getUpdatedFiles(List<String> updatedFiles, File rootFolder, List<String> commitFilessha1) {
        for (File file : rootFolder.listFiles()) {
            if (!file.getName().equals(".magit") && !file.getName().equals(".magittemp")) {
                if (file.isDirectory()) {
                    getUpdatedFiles(updatedFiles, file, commitFilessha1);
                } else {
                    Blob blob = new Blob(file);
                    if (!commitFilessha1.contains(blob.getBlobSha1())) {
                        updatedFiles.add(file.getPath());
                    }
                }
            }
        }
    }

    private boolean isChangesFound(Folder mainFolder) {

        return this.currentCommit == null || !mainFolder.getFolderSha1().equals(this.currentCommit.getMainRepoSha1());
    }

    private void handleCommit(Folder mainFolder, String commitMsg) throws IOException {
        this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());

//        String commitMsg = ConsoleMenu.displayMsgAndReturnInput("Please Enter Commit Msg");
        this.currentCommit = new Commit(commitMsg, this.currentRepo.get_mainProjectSha1(), currentCommit.getCommitSha1());
        this.currentCommit.createCommitRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
        this.currentBranch.setBranchFile(this.currentBranch.getName(),
                this.currentCommit.getCommitSha1(), this.currentRepo.BRANCHES_DIR_PATH);

        this.currentRepo.setRootFolder(mainFolder);
        this.latestFolderReflection = mainFolder;
        this.currentRepo.set_mainProjectSha1(this.latestFolderReflection.getFolderSha1());
    }

    public boolean branchExists(String branchName) {
        File[] branchFiles = new File(this.currentRepo.BRANCHES_DIR_PATH).listFiles();
        return Arrays.stream(branchFiles)
                .map(File::getName)
                .anyMatch(branchName::equals);
    }

    protected void createBranch(String branchName, boolean checkout) throws IOException {

        Branch newBranch = new Branch(branchName, this.currentCommit);
        newBranch.createBranchFile(this.currentRepo.BRANCHES_DIR_PATH);
        if (checkout) {
            this.checkoutBranch(branchName, true);
        }
    }


    protected void loadXml(XmlLoader xmlLoader) throws IOException, JAXBException {
        boolean isXmlValid;
        isXmlValid = xmlLoader.checkXml();
        if (!isXmlValid) {
            throw new IOException (xmlLoader.getXmlPropriety());
        }
        this.currentRepo = new Repository(xmlLoader.get_path(), null);
        this.currentRepo.createBlankRepository();
        xmlLoader.createRepoRep();
        this.currentRepo.setRootFolder(xmlLoader.getCurrentRootFolder());
        this.currentBranch = xmlLoader.getHeadBranch();
        this.currentCommit = xmlLoader.getCurrentCommit();
        this.currentRepo.createHead(this.currentBranch.getName());
        this.currentRepo.set_mainProjectSha1(currentCommit.getMainRepoSha1());
        this.latestFolderReflection = xmlLoader.getCurrentRootFolder();
        checkoutBranchNewWC(currentBranch.getName(), this.latestFolderReflection);
    }

    private void createFirstCommit(Folder currentMainFolderReflection, String commitMessage) throws IOException {

        currentMainFolderReflection.getChildBlobs().forEach(blob -> {
            blob.createBlobRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
        });

        currentMainFolderReflection.getChildFolders().forEach(folder -> {
            folder.createFolderRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
        });

        currentMainFolderReflection.createFolderRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
        this.currentRepo.setRootFolder(currentMainFolderReflection);
        this.currentRepo.set_mainProjectSha1(currentMainFolderReflection.getFolderSha1());

        this.currentCommit = new Commit(commitMessage, currentMainFolderReflection.getFolderSha1(), "");
        this.currentCommit.createCommitRepresentation(this.currentRepo.OBJECTS_DIR_PATH);

        this.currentBranch.setpCommit(this.currentCommit);
        this.currentBranch.setBranchFile(this.currentBranch.getName(),
                this.currentCommit.getCommitSha1(), this.currentRepo.BRANCHES_DIR_PATH);

        this.latestFolderReflection = currentMainFolderReflection;
    }

    protected void createEmptyRepository(String path) throws IOException {
        this.currentRepo = new Repository(path, null);
        this.currentRepo.createBlankRepository();

        this.currentBranch = new Branch("master", null);

        // Create Both Head && branchFile
        this.currentRepo.createHead(this.currentBranch.getName());
        this.currentBranch.createBranchFile(this.currentRepo.BRANCHES_DIR_PATH);

        Folder mainFolder = new Folder(this.currentRepo.get_path());
        this.currentRepo.setRootFolder(mainFolder);

        this.currentBranch.createBranchFile(this.currentRepo.BRANCHES_DIR_PATH);
    }

    public void setCurrentUser(String userName) {
        this.currentUser.setName(userName);
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }
}