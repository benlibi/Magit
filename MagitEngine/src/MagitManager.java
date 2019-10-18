import Enums.ConflictSections;
import Models.*;
import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagitManager {
    public String rootRepo = "/opt/magit-ex3";
    public String userFileName = "current_user";
    private String currentUserString = User.getName();
    public MagitManager(){
        File rootDir = new File(rootRepo);
        if (rootDir.exists()) {
            rootDir.delete();
        }
        rootDir.mkdir();
    }

    public void setRepo(String repoName, String userName){
        currentRepo = new Repository(rootRepo + "/" + userName + "/" + repoName);
        latestFolderReflection = new Folder(rootRepo + "/" + userName + "/" + repoName);
        String branchName = readBranchFile("HEAD");
        currentCommit = getCommitRep(getHeadCommitOfBranch(branchName));
        currentBranch = new Branch(branchName,currentCommit);
    }
//    public String getUserFromFile(){
//        return Utils.readFile(rootRepo.concat("/" +  userFileName));
//    }
//
//    public Set<Repository> getCurrentUserRepos(){
//        Set<Repository> currentUserRepos = new HashSet<>();
//        File rootDir = new File(rootRepo.concat("/" + currentUserString));
//        for(File userRepo: rootDir.listFiles()){
//            if(!userRepo.isFile()){
//                Repository repo = new Repository(userRepo.getPath());
//                currentUserRepos.add(repo);
//            }
//        }
//        return currentUserRepos;
//    }

    public Set<Repository> getUserRepos(String userName){
        Set<Repository> currentUserRepos = new HashSet<>();
        File rootDir = new File(rootRepo.concat("/" + userName));
        for(File userRepo: rootDir.listFiles()){
            if(!userRepo.isFile()){
                Repository repo = new Repository(userRepo.getPath());
                currentUserRepos.add(repo);
            }
        }
        return currentUserRepos;
    }

    public synchronized void addCurrentUser(String userName){
        File rootDir = new File(rootRepo);
        if(rootDir.exists()) {
            File userDir = new File(rootRepo, userName);
            if(!userDir.exists()){
                userDir.mkdir();
            }
//            Utils.createUserFile(rootRepo,userFileName,userName);
            appendUser(userName);
            currentUserString = userName;
        }
    }


    public boolean isRemote() {
        return currentRepo==null || currentRepo.getRemote_name().equals("");
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    private String remotePath = "";
    boolean isRemote = false;

    public String getCurrentUser() {
        return User.getName();
    }

    private User currentUser = new User();
    private final Set<String> logdInUsers = new HashSet<>();

    public synchronized Set<String> getLogdInUsers(){
        return logdInUsers;
    }
    public synchronized Set<String> getLogdInUsersWhithoutCurrent(){
        return logdInUsers.stream().filter(x -> !x.equals(currentUserString)).collect(Collectors.toCollection(HashSet::new));
    }

    public synchronized void appendUser(String userName){
        logdInUsers.add(userName);
    }

    private Branch currentBranch;
    protected Commit currentCommit;

    public void setCurrentRepo(Repository currentRepo) {
        this.currentRepo = currentRepo;
    }

    public Repository getCurrentRepo() {
        return currentRepo;
    }

    protected Repository currentRepo;
    private Folder latestFolderReflection;

    protected void deleteBranch(String name) throws IOException {
        File branchFile = new File(this.currentRepo.BRANCHES_DIR_PATH.concat("/" + name));
        FileUtils.forceDelete(branchFile);
    }

    protected ArrayList<String> getAvailableBranches(boolean withHead) {
        ArrayList<String> availableBranches = new ArrayList<>();
        File[] branchFiles = new File(this.currentRepo.BRANCHES_DIR_PATH).listFiles();
        Arrays.stream(branchFiles)
                .map(File::getName)
                .filter(branchFile -> !branchFile.equals("HEAD"))
                .filter(branchFile -> !branchFile.equals(this.currentBranch.getName()))
                .forEach(availableBranches::add);

        if(withHead) {
            availableBranches.add(this.currentBranch.getName() + " (HEAD)");
        }else{
            availableBranches.add(this.currentBranch.getName());
        }

        return availableBranches;
    }

    protected ArrayList<String> getRemoteAvailableBranches(String suffix) {
        ArrayList<String> availableBranches = new ArrayList<>();

        String remoteRepoName = this.currentRepo.getRemote_name();
        if(currentRepo.isRemote) {
            File[] branchFiles = new File(this.currentRepo.MAGIT_DIR_PATH+"/"+remoteRepoName).listFiles();
            Arrays.stream(branchFiles)
                    .map(File::getName)
                    .forEach(branch -> availableBranches.add(branch + suffix));

        }
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

    public String getHeadCommitOfBranch(String branchName) {
        return readBranchFile(branchName);
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

    public String getFileContent(String fileSha1) {
        if (this.latestFolderReflection == null) {
            throw new RuntimeException("Couldn't find any commit.");
        }
        if (this.latestFolderReflection.getChildFolders().stream()
                .anyMatch(folder -> folder.getFolderSha1().equals(fileSha1))) {
            return this.latestFolderReflection.getChildFolders().stream()
                    .filter(folder -> folder.getFolderSha1().equals(fileSha1))
                    .map(Folder::getDirContentToString)
                    .collect(Collectors.joining());
        } else if (this.latestFolderReflection.getChildBlobs().stream()
                .anyMatch(blob -> blob.getBlobSha1().equals(fileSha1))) {
            return this.latestFolderReflection.getChildBlobs().stream()
                    .filter(blob -> blob.getBlobSha1().equals(fileSha1))
                    .map(Blob::getContent)
                    .collect(Collectors.joining());
        }

        throw new RuntimeException("There is no such file, try again");
    }

    public void loadRepository(String newRepoPath) throws FileNotFoundException, IOException {

        File[] repoFiles = new File(newRepoPath).listFiles();

        if (repoFiles == null || Arrays.stream(repoFiles).noneMatch(file -> file.getName().equals(".magit"))) {
            throw new FileNotFoundException("Repository Not Exist\nPlease Create It First And Try Again");
        }

        this.currentRepo = new Repository(newRepoPath);
        String branchName = readBranchFile("HEAD");
        String branchCommitSha1 = readBranchFile(branchName);
        String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + branchCommitSha1),
                this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));

        currentCommit = new Commit(commitRepresentation.replace("\n", ""));
        checkoutBranchNewWC(branchName);
    }

    public boolean rtbExist(String branchName){
        List<String> localBranches = getAvailableBranches(false);
        return localBranches.contains(branchName);
    }

    protected void checkoutBranch(String branchName, boolean forceCheckout) throws IOException, RuntimeException {
        if (isChangesFound() && !forceCheckout) {
            throw new NotActiveException("Changes Detected!");
        }

        checkoutRevision(branchName);
        Folder mainFolder = new Folder(this.currentRepo.get_path());
        this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());
        this.currentRepo.setHead(branchName);
        String commitSha1 = readBranchFile(branchName);
        String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + commitSha1),
                this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
        this.currentCommit = new Commit(commitRepresentation.replace("\n", ""));
        this.currentBranch = new Branch(branchName, this.currentCommit, currentRepo.getRemote_name(), Utils.isRemoteExist(branchName, currentRepo.MAGIT_DIR_PATH + "/" + currentRepo.getRemote_name()));
        this.latestFolderReflection = mainFolder;
    }

    private void checkoutRevision(String branchName) {
        deleteWorkingDir();
        String[] mainFolderContent = unzipMainFolderFiles(readBranchFile(branchName));
        createRepoTree(mainFolderContent);
    }

    private void checkoutBranchNewWC(String branchName) throws IOException {
        Folder mainFolder = new Folder(this.currentRepo.get_path());
        deleteWorkingDir();
        String[] mainFolderContent = unzipMainFolderFiles(readBranchFile(branchName));
        createRepoTree(mainFolderContent);

        this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());
        this.currentRepo.setHead(branchName);
        this.currentBranch = new Branch(branchName, this.currentCommit, currentRepo.getRemote_name(), Utils.isRemoteExist(branchName, currentRepo.MAGIT_DIR_PATH + "/" + currentRepo.getRemote_name()));
        this.latestFolderReflection = mainFolder;
        this.currentRepo.set_mainProjectSha1(this.latestFolderReflection.getFolderSha1());
        //this.currentRepo.setRootFolder(this.latestFolderReflection);
    }

    private void checkoutBranchNewWC(String branchName, Folder mainFolder) throws IOException {
        deleteWorkingDir();
        String[] mainFolderContent = unzipMainFolderFiles(readBranchFile(branchName));
        createRepoTree(mainFolderContent);

        this.currentRepo.set_mainProjectSha1(mainFolder.getFolderSha1());
        this.currentRepo.setHead(branchName);
        this.currentBranch = new Branch(branchName, this.currentCommit, currentRepo.getRemote_name(), Utils.isRemoteExist(branchName, currentRepo.MAGIT_DIR_PATH + "/" + currentRepo.getRemote_name()));
        this.latestFolderReflection = mainFolder;
        this.currentRepo.set_mainProjectSha1(this.latestFolderReflection.getFolderSha1());
        //this.currentRepo.setRootFolder(this.latestFolderReflection);
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

    public String readBranchFile(String branchName) {
        return Branch.getBranchCommitPointer(this.currentRepo.BRANCHES_DIR_PATH.concat("/" + branchName));
    }

    public String readRemoteBranchFile(String branchName) {
        return Branch.getBranchCommitPointer(this.currentRepo.MAGIT_DIR_PATH.concat("/" + this.currentRepo.getRemote_name()+"/"+branchName));
    }

    public String readRemoteRepoBranchFile(String branchName) {
        return Branch.getBranchCommitPointer(this.currentRepo.getRemote_path().concat("/.magit/Branches/" + branchName));
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

    public List<String> showBranchCommitHistory() {
        List<String> commitList = new ArrayList<>();
        printCommitHistory(commitList, currentCommit);
        return commitList;
    }

    private void printCommitHistory(List<String> commitList, Commit commit) {
        List<String> previousCommitsSha1 = commit.getCommitHistory();
        commitList.add(commit.toString());
        for (String previousCommitSha1 : previousCommitsSha1) {
            String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + previousCommitSha1),
                    this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            Commit previousCommit = new Commit(commitRepresentation.replace("\n", ""));
            printCommitHistory(commitList, previousCommit);
        }
    }

    private List<String> createSha1List(File[] objectFiles) {
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

    public void commit(String commitMsg, String commitSha1, boolean forceCommit) throws IOException {
        Folder currentMainFolderReflection = new Folder(this.currentRepo.get_path());
        File[] objectFiles = new File(this.currentRepo.OBJECTS_DIR_PATH).listFiles();
        List<String> sha1List = createSha1List(objectFiles);
        if (isChangesFound() || forceCommit) {
            //            Folder rootFolder = createRepo(sha1List, new File(this.currentRepo.get_path()), latestFolderReflection);
            Folder rootFolder = createRepo(sha1List, new File(this.currentRepo.get_path()), currentMainFolderReflection);
            rootFolder.createFolderRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
            if (currentCommit == null) {
                currentCommit = new Commit(commitMsg, rootFolder.getFolderSha1());
            } else {
                List<String> commitHistory = new ArrayList<>();
                commitHistory.add(currentCommit.getCommitSha1());
                if (commitSha1 != null) {
                    commitHistory.add(commitSha1);
                }
                currentCommit = new Commit(commitMsg, rootFolder.getFolderSha1(), commitHistory);
            }
            currentCommit.createCommitRepresentation(this.currentRepo.OBJECTS_DIR_PATH);
            latestFolderReflection = rootFolder;
            this.currentBranch.setBranchFile(this.currentBranch.getName(),
                    this.currentCommit.getCommitSha1(), this.currentRepo.BRANCHES_DIR_PATH);

        } else {
            throw new IOException("No Changes Detected !");
        }
    }

    public Map<String, List<String>> showStatus() throws IOException {
        if (this.latestFolderReflection == null) {
            throw new NullPointerException("Please commit your changes first");
        }
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
        for(String newFile: newFiles){
            updatedFiles.remove(newFile);
        }
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


    public boolean isChangesFound() {
        return !getChangesDetected().isEmpty();
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

    public String checkXML(InputStream xmlInputStream){
        XmlLoader xmlLoader = null;
        try {
            xmlLoader = new XmlLoader(xmlInputStream, rootRepo + "/" + currentUserString);
        } catch (JAXBException e) {
            return e.toString();
        }
        try {
           return checkXmlRepoPathAndLoad(xmlLoader);
        } catch (IOException | JAXBException e) {
            return e.toString();
        }
    }


    private String checkXmlRepoPathAndLoad(XmlLoader xmlLoader) throws IOException, JAXBException {
        String repoPath = xmlLoader.get_path();
        File directory = new File(repoPath);
        String remotePath = xmlLoader.getRemote_path();
        if (checkRemotePath(remotePath)) {
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    return repoPath + " Faild to be created";
                }
                return loadXml(xmlLoader);
            } else {
                String[] repoFiles = directory.list();
                List<String> repoFilesList = new ArrayList<>(Arrays.asList(repoFiles));
                if (repoFilesList.size() != 0) {
                    return repoPath + " Not empy\nAborting repo creation";
                } else {
                    return loadXml(xmlLoader);
                }
            }
        } else {
            return "remote path " + remotePath + " Not exist on not including .magit folder inside\nAborting xml loading" ;
        }
    }

    private boolean checkRemotePath(String path) {
        if (path.equals("")) {
            return true;
        }
        File remote_directory = new File(path);
        if (remote_directory.exists() && remote_directory.isDirectory()) {
            String[] repoFiles = remote_directory.list();
            List<String> repoFilesList = new ArrayList<>(Arrays.asList(repoFiles));
            return repoFilesList.contains(".magit");
        }
        return false;
    }

    public String loadXml(XmlLoader xmlLoader) throws IOException, JAXBException {
        boolean isXmlValid;
        isXmlValid = xmlLoader.checkXml();
        if (!isXmlValid) {
            return xmlLoader.getXmlPropriety();
        }
        Utils.createDir(xmlLoader.getMAGIT_DIR_PATH());
        if(!xmlLoader.getRemote_path().equals("")){
            Utils.createDir(xmlLoader.getMAGIT_DIR_PATH().concat("/" +  xmlLoader.getRemote_name()));
        }
        Utils.createRepoFile(xmlLoader.getMAGIT_DIR_PATH(), xmlLoader.getName(), xmlLoader.getRemote_path(), xmlLoader.getRemote_name());
        this.currentRepo = new Repository(xmlLoader.get_path());
        this.currentRepo.createBlankRepository();
        xmlLoader.createRepoRep();
        //this.currentRepo.setRootFolder(xmlLoader.getCurrentRootFolder());
        this.currentBranch = xmlLoader.getHeadBranch();
        this.currentCommit = xmlLoader.getCurrentCommit();
        this.currentRepo.createHead(this.currentBranch.getName());
        this.currentRepo.set_mainProjectSha1(currentCommit.getMainRepoSha1());
        this.latestFolderReflection = xmlLoader.getCurrentRootFolder();
        checkoutBranchNewWC(currentBranch.getName(), this.latestFolderReflection);
        return "";
    }

    public void createCommitList(Commit commit, List<Commit> commitList) {
        List<String> previousCommitsSha1 = commit.getCommitHistory();
        for (String previousCommitSha1 : previousCommitsSha1) {
            String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + previousCommitSha1),
                    this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            Commit previousCommit = new Commit(commitRepresentation.replace("\n", ""));
            commitList.add(previousCommit);
            createCommitList(previousCommit, commitList);
        }
    }

    private void createFileMap(Map<String, Blob> wcFileMap, File path) {
        File[] files = path.listFiles();
        if (files != null && path.isDirectory()) {
            for (File file : files) {
                if (file.isFile()) {
                    Blob blob = new Blob(file);
                    wcFileMap.put(file.getPath(), blob);
                } else if (file.isDirectory() && !file.getName().contains(".")) {
                    createFileMap(wcFileMap, file);
                }
            }
        }
    }

    private Map<String, Blob> getWcFilesMap() {
        Map<String, Blob> wcFileMap = new HashMap<>();
        File mainRepo = new File(currentRepo.get_path());
        createFileMap(wcFileMap, mainRepo);
        return wcFileMap;
    }

    private List<String> getChangesDetected() {
        List<String> changeList = new ArrayList<>();
        Map<String, Blob> wcFilesMap = getWcFilesMap();
        Map<String, Blob> commitFilesMap = getCommitFilesMap(currentCommit.getCommitSha1());
        for (String wcFile : wcFilesMap.keySet()) {
            if (commitFilesMap.keySet().contains(wcFile)) {
                if (!wcFilesMap.get(wcFile).getBlobSha1().equals(commitFilesMap.get(wcFile).getBlobSha1())) {
                    changeList.add(wcFile);
                }
            } else {
                changeList.add(wcFile);
            }
        }
        for (String commitFile : commitFilesMap.keySet()) {
            if (!wcFilesMap.keySet().contains(commitFile)) {
                changeList.add(commitFile);
            }
        }
        return changeList;
    }

    private List<String> getWcDeletedFiles(Map<String, Blob> wcFilesMap, Map<String, Blob> commitFilesMap){
        List<String> deletedFiles = new ArrayList<>();
        for (String commitFile : commitFilesMap.keySet()) {
            if (!wcFilesMap.keySet().contains(commitFile)) {
                deletedFiles.add(commitFile);
            }
        }
        return deletedFiles;
    }

    private List<String> getWcNewFiles(Map<String, Blob> wcFilesMap, Map<String, Blob> commitFilesMap){
        List<String> changeList = new ArrayList<>();
        for (String wcFile : wcFilesMap.keySet()) {
            if (commitFilesMap.keySet().contains(wcFile)) {
                if (!wcFilesMap.get(wcFile).getBlobSha1().equals(commitFilesMap.get(wcFile).getBlobSha1())) {
                    changeList.add(wcFile);
                }
            } else {
                changeList.add(wcFile);
            }
        }
        return changeList;
    }

    private List<String> getWcUpdatedFiles(Map<String, Blob> wcFilesMap, Map<String, Blob> commitFilesMap){
        List<String> newFiles = new ArrayList<>();
        for (String wcFile : wcFilesMap.keySet()) {
            if (!commitFilesMap.keySet().contains(wcFile)) {
                newFiles.add(wcFile);
            }
        }
        return newFiles;
    }

    public  Map<String, List<String>> getWcChanges() {
        Map<String, List<String>> statusMap = new HashMap<>();
        Map<String, Blob> wcFilesMap = getWcFilesMap();
        Map<String, Blob> commitFilesMap = getCommitFilesMap(currentCommit.getCommitSha1());
        List<String> deletedFiles = getWcDeletedFiles(wcFilesMap,commitFilesMap);
        List<String> updatedFiles = getWcUpdatedFiles(wcFilesMap,commitFilesMap);
        List<String> newFiles = getWcNewFiles(wcFilesMap,commitFilesMap);
        statusMap.put("Deleted Files:", deletedFiles);
        statusMap.put("Updated Files:", updatedFiles);
        statusMap.put("New:", newFiles);
        return statusMap;
    }


    protected void createEmptyRepository(String path, String repo_Name) throws IOException {
        Utils.createDir(path + "/.magit");
        Utils.createRepoFile(path + "/.magit", repo_Name, "", "");
        this.currentRepo = new Repository(path);
        this.currentRepo.createBlankRepository();

        this.currentBranch = new Branch("master", null);

        // Create Both Head && branchFile
        this.currentRepo.createHead(this.currentBranch.getName());
        this.currentBranch.createBranchFile(this.currentRepo.BRANCHES_DIR_PATH);
    }

    protected void createLocalRepository(String path, String repo_Name) throws IOException {
        Utils.createDir(path);
        Utils.createDir(path + "/.magit");
        Utils.createDir(path + "/.magit/" +  currentRepo.getRemote_name());
        Utils.createRepoFile(path + "/.magit", repo_Name, currentRepo.getRemote_path(), currentRepo.getRemote_name());
        this.currentRepo = new Repository(path);
        this.currentRepo.createBlankRepository();
    }

    public void setCurrentUser(String userName) {
        User.setName(userName);
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }

    public Commit getCommitRep(String commitSh1) {
        String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + commitSh1),
                this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
        return new Commit(commitRepresentation.replace("\n", ""));
    }

    private String getSha1Content(String folderSha1) {
        return Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + folderSha1),
                this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1")).trim();
    }

    public String getAncestor(String currentCommitSha1, String mergedSha1) {
        AncestorFinder ancestorFinder = new AncestorFinder(this::getCommitRep);

        // find ancestor
        return ancestorFinder.traceAncestor(currentCommitSha1, mergedSha1);
    }

    private void createFolder(Map<String, Blob> commitFilesMap, String path, String folderSha1) {
        String folderContent = getSha1Content(folderSha1);
        for (String line : folderContent.split("\n")) {
            String[] entry = line.split(",");
            if (entry.length > 2) {
                if (entry[2].equals("file")) {
                    String blobContent = getSha1Content(entry[1]);
                    String blobName = entry[0];
                    String blobOwner = entry[3];
                    String blobLastModifyDate = entry[4];
                    Blob blob = new Blob(blobName, blobContent, blobOwner, blobLastModifyDate, path);
                    commitFilesMap.put(path + "/" + blobName, blob);
                } else {
                    createFolder(commitFilesMap, path + "/" + entry[0], entry[1]);
                }
            }
        }
    }

    public Map<String, Blob> getCommitFilesMap(String commitSha1) {
        Map<String, Blob> commitFilesMap = new TreeMap<>(
                new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        if (s1.length() > s2.length()) {
                            return 1;
                        } else if (s1.length() < s2.length()) {
                            return -1;
                        } else {
                            return s1.compareTo(s2);
                        }
                    }
                }
        );
        Commit commit = getCommitRep(commitSha1);
        createFolder(commitFilesMap, this.currentRepo.get_path(), commit.getMainRepoSha1());
        return commitFilesMap;
    }


    public Map<String, Blob> getNewFilesMap(Map<String, Blob> sonCommit, Map<String, Blob> ancestorCommit) {
        Map<String, Blob> diffFilesMap = new HashMap<>();
        for (String blobPath : sonCommit.keySet()) {
            if (!ancestorCommit.keySet().contains(blobPath)) {
                diffFilesMap.put(blobPath, sonCommit.get(blobPath));
            }
        }
        return diffFilesMap;
    }


    public Map<String, Blob> getCommitDiffsMap(Map<String, Blob> sonCommit, Map<String, Blob> ancestorCommit) {
        Map<String, Blob> diffFilesMap = new HashMap<>();
        for (String blobPath : sonCommit.keySet()) {
            if (ancestorCommit.keySet().contains(blobPath)) {
                if (!sonCommit.get(blobPath).getBlobSha1().equals(ancestorCommit.get(blobPath).getBlobSha1())) {
                    diffFilesMap.put(blobPath, sonCommit.get(blobPath));
                }
            } else {
                diffFilesMap.put(blobPath, sonCommit.get(blobPath));
            }
        }
        for (String blobPath : ancestorCommit.keySet()) {
            if (!sonCommit.keySet().contains(blobPath)) {
                diffFilesMap.put(blobPath, null);
            }
        }
        return diffFilesMap;
    }

    public void createFile(String filePath, String content) {
        File blob = new File(filePath);
        Path path = Paths.get(blob.getParent());
        try {
            Files.createDirectories(path);
            BufferedWriter output = new BufferedWriter(new FileWriter(blob));
            output.write(content);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String path) throws IOException {
        File fileToDelete = new File(path);
        Files.deleteIfExists(fileToDelete.toPath());
    }

    public List<Conflict> getConflictListAndCreateFiles(Map<String, Blob> ourDiff, Map<String, Blob> theirDiff,
                                                        Map<String, Blob> ancestorFiles) throws IOException {
        List<Conflict> conflictList = new ArrayList<>();
        for (String blobPath : theirDiff.keySet()) {
            if (ourDiff.keySet().contains(blobPath)) {
                Map<ConflictSections, String> relatedBlobs = new HashMap<>();
                String originContent = (!ancestorFiles.containsKey(blobPath) || ancestorFiles.get(blobPath) == null) ? "" :
                        ancestorFiles.get(blobPath).getContent();
                String ourContent = (ourDiff.get(blobPath) == null) ? "" : ourDiff.get(blobPath).getContent();
                String theirContent = (theirDiff.get(blobPath) == null) ? "" : theirDiff.get(blobPath).getContent();
                relatedBlobs.put(ConflictSections.ORIGIN, originContent);
                relatedBlobs.put(ConflictSections.YOUR_VERSION, ourContent);
                relatedBlobs.put(ConflictSections.THEIR_VERSION, theirContent);
                Conflict conflict = new Conflict(blobPath, relatedBlobs);
                conflictList.add(conflict);
            } else {
                Blob blob = theirDiff.get(blobPath);
                if (blob != null) {
                    createFile(blobPath, blob.getContent());
                } else {
                    deleteFile(blobPath);
                }
            }
        }
        return conflictList;
    }

    public String getChangesSring(String firstCommitSha1, String secondCommitSha1) {
        Map<String, Blob> firstCommitFiles = getCommitFilesMap(firstCommitSha1);
        Map<String, Blob> secondCommitFiles = getCommitFilesMap(secondCommitSha1);
        Map<String, Blob> changesMap = getCommitDiffsMap(firstCommitFiles, secondCommitFiles);
        Map<String, Blob> newFilesMap = getNewFilesMap(firstCommitFiles, secondCommitFiles);
        StringBuilder newFiles = new StringBuilder("New Files:\n");
        StringBuilder deletedFiles = new StringBuilder("Deleted Files:\n");
        StringBuilder updatedFiles = new StringBuilder("Updated Files:\n");
        for (String filePath : newFilesMap.keySet()) {
            newFiles.append(filePath + "\n");
        }
        for (String filePath : changesMap.keySet()) {
            if (changesMap.get(filePath) == null) {
                deletedFiles.append(filePath + "\n");
            } else if (!newFilesMap.keySet().contains(filePath)) {
                updatedFiles.append(filePath + "\n");
            }
        }
        return newFiles.append(deletedFiles.append(updatedFiles)).toString();
    }

    private Map<String, List<Blob>> createFolderMap(Map<String, Blob> CommitFiles) {
        Map<String, List<Blob>> folderMap = new HashMap<>();
        for (Blob blob : CommitFiles.values()) {
            String parentDir = blob.getParentDir();
            if (!folderMap.keySet().contains(parentDir)) {
                List<Blob> blobList = new ArrayList<>();
                folderMap.put(parentDir, blobList);
            }
            folderMap.get(parentDir).add(blob);
        }
        return folderMap;
    }

    public Map<String, List<Blob>> getStatusMap(String CommitSha1) {
        Map<String, Blob> CommitFiles = getCommitFilesMap(CommitSha1);
        return createFolderMap(CommitFiles);

    }

    protected void resetBranch(String commitSha1, boolean forceCheckout) throws RuntimeException, IOException {
        if (isChangesFound() && !forceCheckout) {
            throw new NotActiveException("Changes Detected!");
        }

        this.currentBranch.setBranchFile(this.currentBranch.getName(),
                commitSha1, this.currentRepo.BRANCHES_DIR_PATH);
        this.checkoutBranch(this.currentBranch.getName(), true);
    }

    private void copyFiles() throws IOException {
        //copy remote branches
        Utils.copyFolderContent(this.currentRepo.getRemote_path()+"/.magit/Branches/", this.currentRepo.MAGIT_DIR_PATH+"/"+this.currentRepo.getRemote_name());
        //move head
        Utils.moveFile(this.currentRepo.MAGIT_DIR_PATH+"/"+this.currentRepo.getRemote_name()+"/HEAD", this.currentRepo.BRANCHES_DIR_PATH+"/HEAD");
        //copy objects
        Utils.copyFolderContent(this.currentRepo.getRemote_path()+"/.magit/Objects/", this.currentRepo.OBJECTS_DIR_PATH);

    }

    private void logicPopulation() throws IOException {
        //create head branch
        String branchName = readBranchFile("HEAD");
        String headCommitSha1 = readRemoteBranchFile(branchName);
        this.currentCommit = getCommitRep(headCommitSha1);
        this.currentBranch = new Branch(branchName, currentCommit, this.currentRepo.getRemote_name(), true);
        currentBranch.createBranchFile(this.currentRepo.BRANCHES_DIR_PATH);
        checkoutBranchNewWC(branchName);
//        this.currentBranch = new Branch(branchName)
    }

    public void clone(String repo_name, String path) throws IOException {
        path = path.concat("/" + currentRepo.getName());
        createLocalRepository(path,repo_name);
        copyFiles();
        logicPopulation();
    }

    private List<String> getRemoteRepoBranchList(){
        ArrayList<String> RemoteRepoBranchList = new ArrayList<>();
            File[] branchFiles = new File(this.currentRepo.getRemote_path() + "/.magit/Branches").listFiles();
            if (branchFiles != null) {
                Arrays.stream(branchFiles)
                        .map(File::getName)
                        .filter(branchFile -> !branchFile.equals("HEAD"))
                        .forEach(RemoteRepoBranchList::add);

            }
        return RemoteRepoBranchList;
    }

    private List<String> getLocalSha1List(){
        List<String> localCommitSha1List = new ArrayList<>();
        File[] sha1Files = new File(this.currentRepo.OBJECTS_DIR_PATH).listFiles();
        if (sha1Files != null) {
            Arrays.stream(sha1Files)
                    .map(File::getName)
                    .forEach(localCommitSha1List::add);
        }
        return localCommitSha1List;
    }

    private List<String> getRemoteSha1List(){
        List<String> localCommitSha1List = new ArrayList<>();
        File[] sha1Files = new File(this.currentRepo.getRemote_path().concat("/.magit/Objects")).listFiles();
        if (sha1Files != null) {
            Arrays.stream(sha1Files)
                    .map(File::getName)
                    .forEach(localCommitSha1List::add);
        }
        return localCommitSha1List;
    }

    private void copyCommits(String commitSha1) throws IOException {
        List<String> localCommitSha1List =  getLocalSha1List();
        if(!localCommitSha1List.contains(commitSha1)){
            Utils.copyFile(this.currentRepo.getRemote_path().concat("/.magit/Objects/" + commitSha1), this.currentRepo.OBJECTS_DIR_PATH + "/" +commitSha1);
            String commitRepresentation = Utils.getContentFromZip(this.currentRepo.getRemote_path().concat("/.magit/Objects/" + commitSha1),
                    this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            Commit remoteCommit =  new Commit(commitRepresentation.replace("\n", ""));
            for(String sonCommitSha1: remoteCommit.getCommitHistory()){
                copyCommits(sonCommitSha1);
            }
        }
    }


    private void copyCommitsToRemote(String commitSha1) throws IOException {
        List<String> remoteCommitSha1List =  getRemoteSha1List();
        if(!remoteCommitSha1List.contains(commitSha1)){
            Utils.copyFile(this.currentRepo.OBJECTS_DIR_PATH + "/" +commitSha1, this.currentRepo.getRemote_path().concat("/.magit/Objects/" + commitSha1));
            String commitRepresentation = Utils.getContentFromZip(this.currentRepo.OBJECTS_DIR_PATH.concat("/" + commitSha1),
                    this.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            Commit remoteCommit =  new Commit(commitRepresentation.replace("\n", ""));
            for(String sonCommitSha1: remoteCommit.getCommitHistory()){
                copyCommitsToRemote(sonCommitSha1);
            }
        }
    }

    private void copyBranchAndCommitsToLocalRepo(String remoteRepoBranchName, List<String> localRepoRemoteBranchList) throws IOException {
        String remoteHeadCommitSha1 = readRemoteRepoBranchFile(remoteRepoBranchName);
        if(localRepoRemoteBranchList.contains(remoteRepoBranchName)){
            String localHeadCommitSha1 = readRemoteBranchFile(remoteRepoBranchName);
            if(!localHeadCommitSha1.equals(remoteHeadCommitSha1)){
                copyCommits(remoteHeadCommitSha1);
            }
        }else{
            copyCommits(remoteHeadCommitSha1);
        }
        //copy branch file
        Utils.copyFile(this.currentRepo.getRemote_path().concat("/.magit/Branches/" + remoteRepoBranchName), this.currentRepo.MAGIT_DIR_PATH + "/" + this.currentRepo.getRemote_name() + "/" + remoteRepoBranchName);
    }


    private void copyBranchAndCommitsToRemoteRepo(String remoteBranchName, List<String> RemoteRepoRemoteBranchList) throws IOException {
        String remoteHeadCommitSha1 = readRemoteBranchFile(remoteBranchName);
        if(RemoteRepoRemoteBranchList.contains(remoteBranchName)){
            String remoteRepoHeadCommitSha1 = readRemoteRepoBranchFile(remoteBranchName);
 //           if(!remoteRepoHeadCommitSha1.equals(remoteHeadCommitSha1)){
            copyCommitsToRemote(remoteHeadCommitSha1);
 //           }
        }else{
            copyCommitsToRemote(remoteHeadCommitSha1);
        }
        //copy branch file
        Utils.copyFile(this.currentRepo.MAGIT_DIR_PATH + "/" + this.currentRepo.getRemote_name() + "/" + remoteBranchName, this.currentRepo.getRemote_path().concat("/.magit/Branches/" + remoteBranchName));
    }


    private void pullCommitsToLocalRepo(String remoteRepoBranchName) throws IOException {
        String remoteHeadCommitSha1 = readRemoteRepoBranchFile(remoteRepoBranchName);
        String localHeadCommitSha1 = readRemoteBranchFile(remoteRepoBranchName);
        if(!localHeadCommitSha1.equals(remoteHeadCommitSha1)){
            copyCommits(remoteHeadCommitSha1);
            //copy branch file
            Utils.copyFile(this.currentRepo.getRemote_path().concat("/.magit/Branches/" + remoteRepoBranchName), this.currentRepo.MAGIT_DIR_PATH + "/" + this.currentRepo.getRemote_name() + "/" + remoteRepoBranchName);
            String headCommitSha1 = readRemoteBranchFile(remoteRepoBranchName);
            this.currentCommit = getCommitRep(headCommitSha1);
            this.currentBranch = new Branch(remoteRepoBranchName, currentCommit, this.currentRepo.getRemote_name(), true);
            currentBranch.createBranchFile(this.currentRepo.BRANCHES_DIR_PATH);
            checkoutBranchNewWC(remoteRepoBranchName);
        }
    }


    public void  fetch() throws IOException {
        List<String> remoteRepoBranchList = getRemoteRepoBranchList();
        List<String> localRepoRemoteBranchList = getRemoteAvailableBranches("");
        for(String remoteRepoBranchName: remoteRepoBranchList){
            copyBranchAndCommitsToLocalRepo(remoteRepoBranchName, localRepoRemoteBranchList);
        }
        Utils.copyFolderContent(this.currentRepo.getRemote_path().concat("/.magit/Objects"), this.currentRepo.OBJECTS_DIR_PATH);
    }

    public void pull() throws IOException {
        String branchName = readBranchFile("HEAD");
        pullCommitsToLocalRepo(branchName);
        Utils.copyFolderContent(this.currentRepo.getRemote_path().concat("/.magit/Objects"), this.currentRepo.OBJECTS_DIR_PATH);

    }

    public void copyRBtoRTB(String branchName) throws IOException {
        Utils.copyFile(this.currentRepo.MAGIT_DIR_PATH.concat("/" + this.currentRepo.getRemote_name() + "/" + branchName), this.currentRepo.BRANCHES_DIR_PATH.concat("/"+branchName));
    }

    private void copyRTBtoRB() throws IOException {
        List<String> localBranchList = getAvailableBranches(false);
        List<String> localRepoRemoteBranchList = getRemoteAvailableBranches("");
        for(String localBranch: localBranchList){
            //if(localRepoRemoteBranchList.contains(localBranch)){
                Utils.copyFile(this.currentRepo.BRANCHES_DIR_PATH.concat("/"+localBranch), this.currentRepo.MAGIT_DIR_PATH.concat("/" + this.currentRepo.getRemote_name() + "/" + localBranch));
            //}
        }
    }

    public void push() throws IOException {
        copyRTBtoRB();
        List<String> remoteRepoBranchList = getRemoteRepoBranchList();
        List<String> localRepoRemoteBranchList = getRemoteAvailableBranches("");
        for(String remoteBranchName: localRepoRemoteBranchList){
            copyBranchAndCommitsToRemoteRepo(remoteBranchName, remoteRepoBranchList);
        }
        Utils.copyFolderContent(this.currentRepo.OBJECTS_DIR_PATH,this.currentRepo.getRemote_path().concat("/.magit/Objects"));
    }


    public boolean isRemoteBehind(){
        String branchName = readBranchFile("HEAD");
        String localCommitSha1 = readBranchFile(branchName);
        List<String> remoteRepoBranchList = getRemoteRepoBranchList();
        if(!remoteRepoBranchList.contains(branchName)){
            return true;
        }
        String remoteCommitSha1 = readRemoteBranchFile(branchName);
        return !localCommitSha1.equals(remoteCommitSha1);
    }

    public boolean isLocalBehind(){
        List<String> localRepoRemoteBranchList = getRemoteAvailableBranches("");
        for(String remoteBranchName: localRepoRemoteBranchList){
            String remoteRepoHeadCommitSha1 = readRemoteRepoBranchFile(remoteBranchName);
            String remoteHeadCommitSha1 = readRemoteBranchFile(remoteBranchName);
            if(!remoteRepoHeadCommitSha1.equals(remoteHeadCommitSha1)){
                return true;
            }
        }
        return false;
    }
}