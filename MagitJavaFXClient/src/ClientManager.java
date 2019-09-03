import Models.XmlLoader;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        } catch (IOException | NullPointerException e) {
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
        } catch (IOException e) {
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
            } catch (IOException e) {
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
                    } catch (IOException ex) {
                        handleException(e);
                    }

                }
            });

        }
//        });
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
}
