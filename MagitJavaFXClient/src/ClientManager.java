import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.util.ArrayList;

public class ClientManager {

    private MagitManager magitManager = new MagitManager();

    protected void createRepository() {
        try {
            TextInputDialog td = new TextInputDialog("enter any repo path");
            td.setHeaderText("Creating new repository");
            td.showAndWait();

            magitManager.createEmptyRepository(td.getEditor().getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected ArrayList<String> getAvailableBranches() {

        return magitManager.getAvailableBranches();
    }

    protected void loadRepository() {
        try {
            TextInputDialog td = new TextInputDialog("enter any repo path");
            td.setHeaderText("Creating new repository");
            td.showAndWait();

            magitManager.loadRepository(td.getEditor().getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
