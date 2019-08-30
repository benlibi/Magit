import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
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
            this.magitManager.loadXml();

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

}
