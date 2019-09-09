import Models.Commit;
import Models.ExtendedCommit;
import Models.XmlLoader;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.*;

class ClientManager {

    private MagitManager magitManager = new MagitManager();

    boolean createRepository(Stage stage) {
        try {
            Optional<String> path = this.getRepoDirPath(stage);
            if (path.isPresent()) {
                magitManager.createEmptyRepository(path.get());
                return true;
            }
            return false;
        } catch (IOException | RuntimeException e) {
            handleException(e);
            return false;
        }
    }

    ArrayList<String> getAvailableBranches() {

        return magitManager.getAvailableBranches();
    }

    boolean loadRepository(Stage stage) {
        try {
            Optional<String> repoPath = this.getRepoDirPath(stage);
            if (repoPath.isPresent()) {
                magitManager.loadRepository(repoPath.get());

                return true;
            }

            return false;
        } catch (IOException | RuntimeException e) {
            handleException(e);

            return false;
        }
    }

    boolean loadXMLRepository(Stage stage) {
        try {
            Optional<String> repoPath = this.getRepoFilePath(stage);

            if (repoPath.isPresent()) {
                XmlLoader xmlLoader = new XmlLoader(repoPath.get());
                checkXmlRepoPath(xmlLoader.get_path());
                this.magitManager.loadXml(xmlLoader);
                infoMessage("Repo was loaded Successfully", "Success");
                return true;
            }
        } catch (IOException | JAXBException e) {
            handleException(e);
        }
        infoMessage("Path not exist", "Blat");
        return false;
    }

    void changeUser() {
        Optional<String> userName = showDialogMsg("Please enter user name", "Change User Name");
        userName.ifPresent(s -> this.magitManager.setCurrentUser(s));
    }

    void showWC() {
        try {
            Map<String, List<String>> statusMap = magitManager.showStatus();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("WC");

            GridPane grid = new GridPane();

            int i = 0;
            for (String status : statusMap.keySet()) {
                if (statusMap.get(status).size() != 0) {

                    grid.addRow(i, new Label(status));
                    i++;
                    for (String changedFile : statusMap.get(status)) {
                        grid.addRow(i, new Label(changedFile));
                        i++;
                    }
                } else {
                    grid.addRow(i, new Label(status));
                }
                i++;
            }
            grid.setHgap(30);
            ColumnConstraints right = new ColumnConstraints();
            right.setHalignment(HPos.RIGHT);
            grid.getColumnConstraints().setAll(new ColumnConstraints(), right);

            ScrollPane sp = new ScrollPane(grid);
            alert.getDialogPane().setExpandableContent(sp);
            alert.getDialogPane().setExpanded(true);
            alert.setResizable(true);
            alert.initModality(Modality.WINDOW_MODAL);
            alert.showAndWait();


        } catch (IOException e) {
            handleException(e);
        }
    }

    void createBranch() {
        Optional<String> branchName = showDialogMsg("Please enter branch name", "Create Branch");
        branchName.ifPresent(s -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Checkout Your New Branch ?");
            alert.setContentText("Would You Like To Checkout ?");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
            alert.showAndWait().ifPresent(type -> {
                boolean checkout = false;
                if (type == okButton) {
                    checkout = true;
                } else if (type == cancelButton) {
                    return;
                }
                try {
                    this.magitManager.createBranch(s, checkout);
                } catch (IOException | RuntimeException e) {
                    handleException(e);
                }
            });
        });
    }

    void deleteBranch() {
        Optional<String> branchName = showDialogMsg("Please enter branch name", "Delete Branch");
        branchName.ifPresent(s -> {
            try {
                this.magitManager.deleteBranch(s);
            } catch (IOException | RuntimeException e) {
                handleException(e);
            }
        });
    }

    void checkoutBranch(String branchName) {
        boolean forceCheckout = true;

        try {
            this.magitManager.checkoutBranch(branchName, forceCheckout);
        } catch (IOException | RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(e.getMessage());
            alert.setContentText("force checkout?");
            ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
            alert.showAndWait().ifPresent(type -> {
                if (type == ButtonType.OK) {
                    try {
                        this.magitManager.checkoutBranch(branchName, true);
                    } catch (IOException | RuntimeException ex) {
                        handleException(e);
                    }

                }
            });

        }
//        });
    }

    private void createEdges(Model model, Map<String,ICell> commitRep, Map<String,List<Commit>> commitMap){
        for(String branchName: commitMap.keySet()){
            for(Commit commit: commitMap.get(branchName)){
                for(String parentCommit: commit.getCommitHistory()){
                    final Edge edge = new Edge(commitRep.get(commit.getCommitSha1()), commitRep.get(parentCommit));
                    model.addEdge(edge);
                }
            }
        }

    }

//    private Map<String,List<Commit>>createCommitParantMap(){
//        Map<String, List<Commit>> commitParentMap = new HashMap<String,List<Commit>>();
//        List<String> branchList = getAvailableBranches();
//        for(String branchName: branchList){
//            String branchCommitSha1 = magitManager.readBranchFile(branchName);
//            String commitRepresentation = Utils.getContentFromZip(magitManager.currentRepo.OBJECTS_DIR_PATH.concat("/" + branchCommitSha1),
//                    magitManager.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
//            Commit commit = new Commit(commitRepresentation.replace("\n", ""));
//            if(commit.getCommitHistory().size()==2){
//                for(String commitRepresentation:commit.getCommitHistory())
//                    magitManager.createCommitList1(commit, commitList);
//            }
//        }
//
//        return commitParentMap;
//    }

    public Map<String,List<Commit>> createCommitMap(){
        Map<String,List<Commit>> commitMap = new HashMap<String,List<Commit>>();
        List<String> branchList = getAvailableBranches();
        for (String branchName: branchList){
            branchName =branchName.replace(" (HEAD)", "");
            List<Commit> commitList = new ArrayList<>();
            String branchCommitSha1 = magitManager.readBranchFile(branchName);
            String commitRepresentation = Utils.getContentFromZip(magitManager.currentRepo.OBJECTS_DIR_PATH.concat("/" + branchCommitSha1),
                    magitManager.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            Commit commit = new Commit(commitRepresentation.replace("\n", ""));
            commitList.add(commit);
            magitManager.createCommitList(commit, commitList);
            commitMap.put(branchName, commitList);
        }
        return commitMap;
    }

    private void populateCommitChildrensForBranch(ExtendedCommit commit, Map<String, ExtendedCommit> extendedCommitList){
        List<String> commitChildSha1 = commit.getCommitHistory();
        for(String commitSha1: commitChildSha1){
            ExtendedCommit childcommit;
            if(!extendedCommitList.keySet().contains(commitSha1)) {
                String commitRepresentation = Utils.getContentFromZip(magitManager.currentRepo.OBJECTS_DIR_PATH.concat("/" + commitSha1),
                        magitManager.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
                childcommit = new ExtendedCommit(commitRepresentation.replace("\n", ""));
                extendedCommitList.put(childcommit.getCommitSha1(), childcommit);
            }else{
                childcommit = extendedCommitList.get(commitSha1);
            }
            childcommit.addToCommitListChildList(commit);
            populateCommitChildrensForBranch(childcommit, extendedCommitList);
        }
    }



    private Map<String, ExtendedCommit> populateCommitChildrens(Set<String> branchSet){
        Map<String, ExtendedCommit> extendedCommitList = new HashMap<>();
        for(String branchName: branchSet){
            String branchCommitSha1 = magitManager.readBranchFile(branchName);
            String commitRepresentation = Utils.getContentFromZip(magitManager.currentRepo.OBJECTS_DIR_PATH.concat("/" + branchCommitSha1),
                    magitManager.currentRepo.MAGIT_DIR_PATH.concat("temp/resources/branchCommitSha1"));
            ExtendedCommit commit = new ExtendedCommit(commitRepresentation.replace("\n", ""));
            commit.setBranchName(branchName);
            extendedCommitList.put(commit.getCommitSha1(), commit);
            populateCommitChildrensForBranch(commit, extendedCommitList);
        }
        return extendedCommitList;

    }

    private ExtendedCommit getRootCommit(Map<String, ExtendedCommit> extendedCommitList){
        for(ExtendedCommit commit: extendedCommitList.values()){
            if(commit.getCommitHistory().size()==0){
                commit.setBranchName("master");
                return commit;
            }
        }
        return null;
    }

    private void populateMasterBranchName(ExtendedCommit commit){
        if(commit.getCommitChildes().size()!=0){
            ExtendedCommit childCommit = commit.getOlderCommit();
            childCommit.setBranchName(commit.getBranchName());
            populateMasterBranchName(childCommit);
        }
    }

    private void populateBranchConmmit(Map<String, ExtendedCommit> extendedCommitList ,ExtendedCommit commit, String branchName){
        if(commit.getCommitHistory().size()!=0){
            for(String parentCommit: commit.getCommitHistory()){
                ExtendedCommit parentExtendedCommit = extendedCommitList.get(parentCommit);
                if(parentExtendedCommit.getBranchName()!=null){
                    parentExtendedCommit.setBranchName(branchName);
                    populateBranchConmmit(extendedCommitList, parentExtendedCommit, branchName);
                }
            }
        }

    }

    private void populateAllBranchesCommits(Map<String, ExtendedCommit> extendedCommitList, Set<String> branchSet){
        for(String branchName: branchSet){
            String branchCommitSha1 = magitManager.readBranchFile(branchName);
            ExtendedCommit headCommit = extendedCommitList.get(branchCommitSha1);
            headCommit.setBranchName(branchName);
            populateBranchConmmit(extendedCommitList, headCommit, branchName);
        }
    }

    private void populateleftovers(Map<String, ExtendedCommit> extendedCommitList){
        for(ExtendedCommit commit: extendedCommitList.values()){
            if(commit.getBranchName()==null){
                commit.setBranchName(commit.getOlderCommit().getBranchName());
            }
        }
    }

    public void createGraph(Graph graph){
        Map<String,List<Commit>> commitMap = createCommitMap();
        Map<String, ExtendedCommit> extendedCommitList= populateCommitChildrens(commitMap.keySet());
        ExtendedCommit rootCommit = getRootCommit(extendedCommitList);
        populateMasterBranchName(rootCommit);
        populateAllBranchesCommits(extendedCommitList, commitMap.keySet());
        populateleftovers(extendedCommitList);
        Map<String,ICell> commitRep = new HashMap<>();
        final Model model = graph.getModel();
        graph.beginUpdate();
        for(ExtendedCommit extendedCommit: extendedCommitList.values()){
            ICell c = new CommitNode(extendedCommit.getCommitDateString(), extendedCommit.getCommitter(), extendedCommit.getCommitMassage(),extendedCommit.getBranchName());
            model.addCell(c);
            commitRep.put(extendedCommit.getCommitSha1(), c);
        }
        createEdges(model,commitRep, commitMap);
        graph.endUpdate();
        List<String> branches = new ArrayList<>(commitMap.keySet());
        graph.layout(new CommitTreeLayout(branches));
    }

    void showCurrentBranch() {
    }

    void resetBranch() {
    }

    void showCommit() {
        List<String> commitDetails;
        try {
            commitDetails = magitManager.showCommit();
        } catch (RuntimeException e) {
            handleException(e);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Current Commit");

        GridPane grid = new GridPane();

        for (int i = 1; i < commitDetails.size(); i++) {
            Hyperlink tmpLabel = new Hyperlink(commitDetails.get(i));
            tmpLabel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    onFileClick(event);
                }
            });
            grid.addRow(i, tmpLabel);
        }

        grid.setHgap(30);
        ColumnConstraints right = new ColumnConstraints();
        right.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().setAll(new ColumnConstraints(), right);

        ScrollPane sp = new ScrollPane(grid);
        alert.getDialogPane().setExpandableContent(sp);
        alert.getDialogPane().setExpanded(true);
        alert.setResizable(true);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    void commit() {
        Optional<String> commitMsg = showDialogMsg("Please add Commit Message", "Commit Your Changes");
        commitMsg.ifPresent(s -> {
            try {
                magitManager.commit(s);
            } catch (IOException | RuntimeException e) {
                handleException(e);
            }
        });
    }

    private void handleException(Exception e) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText(e.getMessage());
        errorAlert.show();
    }

    private Optional<String> showDialogMsg(String textInputDialog, String headerText) {
        TextInputDialog td = new TextInputDialog(textInputDialog);
        td.setHeaderText(headerText);

        return td.showAndWait();
    }


    private void checkXmlRepoPath(String repoPath) throws IOException {
        File directory = new File(repoPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new IOException(repoPath + " Faild to be created");
            }
        } else {
            String[] repoFiles = directory.list();
            List<String> repoFilesList = new ArrayList<>(Arrays.asList(repoFiles));
            if (repoFilesList.size() != 0) {
                if (!repoFilesList.contains(".magit")) {
                    throw new IOException(repoPath + " Not empy\nAborting repo creation");
                } else {
                    String userInput = xmlAnswer();
                    if (userInput.equals("l")) {
                        this.magitManager.loadRepository(repoPath);
                    } else {
                        this.magitManager.deleteRepo(repoPath);
                    }
                }
            }
        }
    }

    private String xmlAnswer() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Repo Found In Destination");
        alert.setHeaderText(".magit folder found in destination");
        alert.setContentText("Choose your option.");

        ButtonType buttonTypeOne = new ButtonType("Load Existing Repo");
        ButtonType buttonTypeTwo = new ButtonType("Delete Existing Repo and Load Xml");
        ButtonType buttonTypeCancel = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            return "l";
        } else if (result.get() == buttonTypeTwo) {
            return "x";
        } else {
            throw new IOException("Operation canceld");
        }
    }

    private void infoMessage(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private Optional<String> getRepoDirPath(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory == null) {
            return Optional.empty();
        } else {
            return Optional.of(selectedDirectory.getAbsolutePath());
        }
    }

    private Optional<String> getRepoFilePath(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return Optional.empty();
        } else {
            return Optional.of(selectedFile.getAbsolutePath());
        }
    }

    private void onFileClick(ActionEvent actionEvent) {
        Hyperlink file = (Hyperlink) actionEvent.getSource();
        String[] fileAttr = file.getText().split(",");
        String fileContent = magitManager.getFileContent(fileAttr[1]);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(fileAttr[0]);
        alert.setHeaderText(fileAttr[0]);

        GridPane grid = new GridPane();
        Label fileLabel = new Label(fileContent);
        grid.addRow(0, fileLabel);
        ScrollPane sp = new ScrollPane(grid);
        alert.getDialogPane().setExpandableContent(sp);
        alert.getDialogPane().setExpanded(true);
        alert.setResizable(true);
        alert.showAndWait();
    }
}
