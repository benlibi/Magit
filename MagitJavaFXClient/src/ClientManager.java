import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

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

    private Optional<String> showDialogMsg(String textInputDialog, String headerText) {
        TextInputDialog td = new TextInputDialog(textInputDialog);
        td.setHeaderText(headerText);

        return td.showAndWait();
    }

    void changeUserName() {
        Optional<String> userName = showDialogMsg("Please enter user name", "Change User Name");
        userName.ifPresent(s -> this.magitManager.setCurrentUser(s));
    }

    void showWC() {

    }

    void createBranch() {
        Optional<String> branchName = showDialogMsg("Please enter branch name", "Create Branch");
        branchName.ifPresent(s -> {
            try {
                this.magitManager.createBranch(s);
            } catch (IOException e) {
                handleException(e);
            }
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
        Optional<String> branchName = showDialogMsg("Please enter branch name", "Checkout Branch");
        branchName.ifPresent(s -> {
            try {
                this.magitManager.checkoutBranch(s);
            } catch (IOException e) {
                handleException(e);
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
    }

    private void handleException(Exception e) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText(e.getMessage());
        errorAlert.show();
    }
}
