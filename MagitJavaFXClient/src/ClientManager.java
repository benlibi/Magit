import Models.XmlLoader;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class ClientManager {

    private MagitManager magitManager = new MagitManager();

    boolean createRepository() {
        try {
            Optional<String> path = showDialogMsg("enter any repo path", "Creating new repository");

            if (path.isPresent()) {
                magitManager.createEmptyRepository(path.get());
                return true;
            }
            return false;
        } catch (IOException | NullPointerException e) {
            handleException(e);
            return false;
        }
    }

    ArrayList<String> getAvailableBranches() {

        return magitManager.getAvailableBranches();
    }

    boolean loadRepository() {
        try {
            Optional<String> repoPath = showDialogMsg("enter any repo path", "Load repository");

            if (repoPath.isPresent()) {
                magitManager.loadRepository(repoPath.get());

                return true;
            }

            return false;
        } catch (IOException e) {
            handleException(e);

            return false;
        }
    }

    boolean loadXMLRepository() {
        try {
            Optional<String> repoPath = showDialogMsg("enter any repo path", "Load repository");

            if (repoPath.isPresent()) {
                XmlLoader xmlLoader = new XmlLoader(repoPath.get());
                checkXmlRepoPath(xmlLoader.get_path());
                this.magitManager.loadXml(xmlLoader);

                return true;
            }
            infoMessage("Repo was loaded Successfully", "Success");
            return true;
        } catch (IOException | JAXBException e) {
            handleException(e);
        }

        return false;
    }

    void changeUser() {
        Optional<String> userName = showDialogMsg("Please enter user name", "Change User Name");
        userName.ifPresent(s -> this.magitManager.setCurrentUser(s));
    }

    void showWC() {

    }

    void createBranch() {
        Optional<String> branchName = showDialogMsg("Please enter branch name", "Create Branch");
        branchName.ifPresent(s -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Checkout Your New Branch ?");
            alert.setContentText("Would You Like To Checkout ?");
            ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Yes", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
            alert.showAndWait().ifPresent(type -> {
                boolean checkout = false;
                if (type == ButtonType.OK) {
                    checkout = true;
                } else if (type == ButtonType.CANCEL) {
                    return;
                }
                try {
                    this.magitManager.createBranch(s, checkout);
                } catch (IOException e) {
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
            } catch (IOException e) {
                handleException(e);
            }
        });
    }

    void checkoutBranch() {
        boolean forceCheckout = false;
        Optional<String> branchName = showDialogMsg("Please enter branch name", "Checkout Branch");
        branchName.ifPresent(s -> {
            try {
                this.magitManager.checkoutBranch(s, forceCheckout);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(e.getMessage());
                alert.setContentText("force checkout?");
                ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("Yes", ButtonBar.ButtonData.NO);
                ButtonType cancelButton = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
                alert.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.OK) {
                        try {
                            this.magitManager.checkoutBranch(s, true);
                        } catch (IOException ex) {
                            handleException(e);
                        }

                    }
                });

            }
        });
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
        for (int i = 0; i < commitDetails.size(); i++) {
            grid.addRow(i, new Label(commitDetails.get(i)));
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
            } catch (IOException e) {
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
                throw new IOException (repoPath + " Faild to be created");
            }
        } else {
            String[] repoFiles = directory.list();
            List<String> repoFilesList = new ArrayList<>(Arrays.asList(repoFiles));
            if (repoFilesList.size() != 0) {
                if (!repoFilesList.contains(".magit")) {
                    throw new IOException (repoPath + " Not empy\nAborting repo creation");
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
        if (result.get() == buttonTypeOne){
            return "l";
        } else if (result.get() == buttonTypeTwo) {
            return "x";
        } else {
            throw new IOException ("Operation canceld");        }
    }

    private void infoMessage(String message, String title){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

}
