package Models;

import generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlLoader {

    private Unmarshaller jaxbUnmarshaller;

    private MagitBlobs blobs;
    private MagitBranches branches;
    private MagitFolders folders;
    private MagitCommits commits;
    private MagitRepository repository;

    private Map<String, MagitSingleFolder> foldersMap = new HashMap<String, MagitSingleFolder>();
    private Map<String, MagitSingleCommit> commitsMap = new HashMap<String, MagitSingleCommit>();
    private Map<String, MagitBlob> blobsMap = new HashMap<String, MagitBlob>();
    private Map<String, MagitSingleBranch> branchesMap = new HashMap<String, MagitSingleBranch>();

    private String xmlPropriety = "";
    private String remote_path="";
    private String remote_name="";
    private String _path;



    private String name;
    private String MAGIT_DIR_PATH;
    private String OBJECTS_DIR_PATH;
    private String BRANCHES_DIR_PATH;
    private String REMOTE_BRANCHES_DIR_PATH = "";
    private String HEAD_PATH;

    public String getName() {
        return name;
    }
    public String getRemote_name() {
        return remote_name;
    }
    public String getRemote_path() {
        return remote_path;
    }
    public Folder getCurrentRootFolder() {
        return currentRootFolder;
    }

    private Folder currentRootFolder;

    public Branch getHeadBranch() {
        return headBranch;
    }

    private Branch headBranch;

    public Commit getCurrentCommit() {
        return currentCommit;
    }

    private Commit currentCommit;

    public String get_path() {
        return _path;
    }
    public String getMAGIT_DIR_PATH() {
        return MAGIT_DIR_PATH;
    }

    public XmlLoader(InputStream inputStream, String repoPath) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(MagitRepository.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        repository = (MagitRepository) jaxbUnmarshaller.unmarshal(inputStream);
        repository.setLocation(repoPath.concat("/" + repository.getName()));
        blobs = repository.getMagitBlobs();
        branches = repository.getMagitBranches();
        folders = repository.getMagitFolders();
        commits = repository.getMagitCommits();
        name=repository.getName();
        this._path = repository.getLocation();
        MAGIT_DIR_PATH = this._path.concat("/.magit");
        OBJECTS_DIR_PATH = MAGIT_DIR_PATH.concat("/Objects");
        BRANCHES_DIR_PATH = MAGIT_DIR_PATH.concat("/Branches");
        HEAD_PATH = BRANCHES_DIR_PATH.concat("/HEAD");
        if(repository.getMagitRemoteReference().getLocation()!=null){
            remote_path = repository.getMagitRemoteReference().getLocation();
            remote_name = repository.getMagitRemoteReference().getName();
            REMOTE_BRANCHES_DIR_PATH = MAGIT_DIR_PATH.concat("/" + remote_name);
        }
    }

    public void createRepoRep() throws IOException {
        for (MagitSingleBranch magitBranch : branchesMap.values()) {
                MagitSingleCommit magitCommit = commitsMap.get(magitBranch.getPointedCommit().getId());
                MagitSingleFolder magitRootFolder = foldersMap.get(magitCommit.getRootFolder().getId());
                Folder rootFolder = createRepoTree(magitRootFolder, _path);
                List<String> previouseCommitSha1 = getPreviouseCommitSha1List(magitCommit, magitBranch);
                Commit pCommit = new Commit(magitCommit, rootFolder.getFolderSha1(), previouseCommitSha1);
            if(!magitBranch.getName().contains("\\")){
                pCommit.createCommitRepresentation(OBJECTS_DIR_PATH);
                Branch branch = new Branch(magitBranch, pCommit);
                branch.createBranchFile(BRANCHES_DIR_PATH);
                branch.setpCommit(pCommit);
                if (branch.getName().equals(branches.getHead())) {
                    headBranch = branch;
                    currentCommit = pCommit;
                    currentRootFolder = rootFolder;
                }
            }else{
                Branch branch = new Branch(magitBranch, pCommit);
                branch.createBranchFile(REMOTE_BRANCHES_DIR_PATH);
                branch.setpCommit(pCommit);
            }
        }
    }

    private List<String> getPreviouseCommitSha1List(MagitSingleCommit rootCommit, MagitSingleBranch magitBranch) {
        List<String> previouseCommitSha1List = new ArrayList<String>();
        if (rootCommit.getPrecedingCommits() !=null  && rootCommit.getPrecedingCommits().getPrecedingCommit() != null &&
                rootCommit.getPrecedingCommits().getPrecedingCommit().size() != 0) {
            List<PrecedingCommits.PrecedingCommit> precedingCommitList = rootCommit.getPrecedingCommits().getPrecedingCommit();
            for (PrecedingCommits.PrecedingCommit precedingCommit : precedingCommitList) {
                String previouseCommitSha1 = getPreviouseCommitSha1(commitsMap.get(precedingCommit.getId()), magitBranch);
                previouseCommitSha1List.add(previouseCommitSha1);
            }
        }
        return previouseCommitSha1List;
    }

    private String getPreviouseCommitSha1(MagitSingleCommit rootCommit, MagitSingleBranch magitBranch) {
        MagitSingleFolder magitRootFolder = foldersMap.get(rootCommit.getRootFolder().getId());
        Folder rootFolder = createRepoTree(magitRootFolder, _path);
        List<String> previouseCommitSha1List = new ArrayList<String>();
        if (rootCommit.getPrecedingCommits() == null || rootCommit.getPrecedingCommits().getPrecedingCommit() == null ||
                rootCommit.getPrecedingCommits().getPrecedingCommit().size() == 0) {
            Commit pCommit = new Commit(rootCommit, rootFolder.getFolderSha1(), previouseCommitSha1List);
            pCommit.createCommitRepresentation(OBJECTS_DIR_PATH);
            return pCommit.getCommitSha1();
        } else {
            List<PrecedingCommits.PrecedingCommit> precedingCommitList = rootCommit.getPrecedingCommits().getPrecedingCommit();
            for (PrecedingCommits.PrecedingCommit precedingCommit : precedingCommitList) {
                String previouseCommitSha1 = getPreviouseCommitSha1(commitsMap.get(precedingCommit.getId()), magitBranch);
                previouseCommitSha1List.add(previouseCommitSha1);
            }
            Commit pCommit = new Commit(rootCommit, rootFolder.getFolderSha1(), previouseCommitSha1List);
            pCommit.createCommitRepresentation(OBJECTS_DIR_PATH);
            return pCommit.getCommitSha1();
        }
    }

    private Folder createRepoTree(MagitSingleFolder rootFolder, String path) {
        List<Item> items = rootFolder.getItems().getItem();
        List<Blob> childBlobs = new ArrayList<>();
        List<Folder> childFolders = new ArrayList<>();
        for (Item item : items) {
            String itemId = item.getId();

            switch (item.getType()) {
                case "blob":
                    MagitBlob magitBlob = blobsMap.get(itemId);
                    Blob childBlob = new Blob(magitBlob, new File(path.concat("/" + magitBlob.getName())));
                    childBlob.createBlobRepresentation(OBJECTS_DIR_PATH);
                    childBlobs.add(childBlob);
                    break;

                case "folder":
                    MagitSingleFolder magitFolder = foldersMap.get(itemId);
                    if (!magitFolder.getItems().getItem().isEmpty()) {
                        childFolders.add(createRepoTree(magitFolder, path.concat("/" + magitFolder.getName())));
                    }
                    break;
            }
        }
        File folderFile = new File(path);
        Folder folder = new Folder(rootFolder, childBlobs, childFolders, folderFile);
        folder.createFolderRepresentation(OBJECTS_DIR_PATH);
        return folder;
    }

    public boolean checkXml() {
        return createObjectsMap() &&
                checkFolderPropriety() &&
                checkCommitPropriety() &&
                checkBranchPropriety();
    }

    public String getXmlPropriety() {
        return xmlPropriety;
    }

    private boolean checkBranchPropriety() {
        for (Map.Entry<String, MagitSingleBranch> branch : branchesMap.entrySet()) {
            String branchName = branch.getKey();
            MagitSingleBranch branchRep = branch.getValue();
            if (!commitsMap.keySet().contains(branchRep.getPointedCommit().getId())) {
                xmlPropriety = "for branch: " + branchName + "cannot find pointed commit " + branchRep.getPointedCommit().getId();
                return false;
            }
            if(branch.getValue().isTracking()){
                String tracked_branch = branch.getValue().getTrackingAfter();
                if(branchesMap.keySet().contains(tracked_branch)){
                    if(!branchesMap.get(tracked_branch).isIsRemote()){
                        xmlPropriety = "tracked branch: " + tracked_branch + "not remote";
                        return false;
                    }
                }else{
                    xmlPropriety = "for branch: " + branchName + "cannot find remoteBranch " + tracked_branch;
                    return false;
                }
            }
        }
        if (!branchesMap.keySet().contains(branches.getHead())) {
            xmlPropriety = "cannot find HEAD branch " + branches.getHead();
            return false;
        }
        return true;
    }

    private boolean checkCommitPropriety() {

        for (Map.Entry<String, MagitSingleCommit> commit : commitsMap.entrySet()) {
            MagitSingleCommit commitRep = commit.getValue();
            String commitId = commit.getKey();
            String commitRootFolderId = commitRep.getRootFolder().getId();
            if (!foldersMap.keySet().contains(commitRootFolderId)) {
                xmlPropriety = "for commit ID: " + commitId + "cannot find root dir with id" + commitRootFolderId;
                return false;
            }
            if (!foldersMap.get(commitRootFolderId).isIsRoot()) {
                xmlPropriety = "for commit ID: " + commitId + "the commit not pointing to root dir " + commitRootFolderId;
                return false;
            }
        }
        return true;
    }

    private boolean createObjectsMap() {
        List<MagitSingleFolder> folders = this.folders.getMagitSingleFolder();
        for (MagitSingleFolder folder : folders) {
            String folderId = folder.getId();
            if (!foldersMap.keySet().contains(folderId)) {
                foldersMap.put(folderId, folder);
            } else {
                xmlPropriety = "Duplicated folder id " + folderId;
                return false;
            }
        }
        List<MagitSingleCommit> commits = this.commits.getMagitSingleCommit();
        for (MagitSingleCommit commit : commits) {
            String commitId = commit.getId();
            if (!commitsMap.keySet().contains(commitId)) {
                commitsMap.put(commitId, commit);
            } else {
                xmlPropriety = "Duplicated commit id " + commitId;
                return false;
            }
        }
        List<MagitBlob> blobs = this.blobs.getMagitBlob();
        for (MagitBlob blob : blobs) {
            String blobId = blob.getId();
            if (!blobsMap.keySet().contains(blobId)) {
                blobsMap.put(blobId, blob);
            } else {
                xmlPropriety = "Duplicated blob id " + blobId;
                return false;
            }
        }
        List<MagitSingleBranch> branches = this.branches.getMagitSingleBranch();
        for (MagitSingleBranch branch : branches) {
            String branchName = branch.getName();
            if (!branchesMap.keySet().contains(branchName)) {
                branchesMap.put(branchName, branch);
            } else {
                xmlPropriety = "Duplicated branch name " + branchName;
                return false;
            }
        }
        return true;
    }

    private boolean checkFolderPropriety() {
        for (Map.Entry<String, MagitSingleFolder> folder : foldersMap.entrySet()) {
            String folderId = folder.getKey();
            MagitSingleFolder folderRep = folder.getValue();
            List<Item> items = folderRep.getItems().getItem();
            for (Item item : items) {
                String itemType = item.getType();
                String itemId = item.getId();
                if (itemType.equals("folder")) {
                    if (!foldersMap.keySet().contains(itemId)) {
                        xmlPropriety = "folder id " + itemId + " could not be found";
                        return false;
                    }
                    if (itemId.equals(folderId)) {
                        xmlPropriety = "folder " + folderRep.getName() + " pointing to itself";
                        return false;
                    }
                } else if (itemType.equals("blob")) {
                    if (!blobsMap.keySet().contains(itemId)) {
                        xmlPropriety = "blob id " + itemId + " could not be found";
                        return false;
                    }
                } else {
                    xmlPropriety = "item type is not a folder or a blob: " + itemType;
                    return false;
                }
            }
        }
        return true;
    }
}