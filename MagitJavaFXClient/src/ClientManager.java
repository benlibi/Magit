import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class ClientManager {

    private MagitManager magitManager = new MagitManager();

    protected boolean createRepository() {
        try {
            TextInputDialog td = new TextInputDialog("enter any repo path");
            td.setHeaderText("Creating new repository");
            Optional<String> path = td.showAndWait();
            if (path.isPresent()) {
                magitManager.createEmptyRepository(td.getEditor().getText());
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected ArrayList<String> getAvailableBranches() {

        return magitManager.getAvailableBranches();
    }

    protected boolean loadRepository() {
        try {
            TextInputDialog td = new TextInputDialog("enter any repo path");
            td.setHeaderText("Creating new repository");
            td.showAndWait();

            magitManager.loadRepository(td.getEditor().getText());

            return true;
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText(e.getMessage());
            errorAlert.show();

            return false;
        }
    }
}
