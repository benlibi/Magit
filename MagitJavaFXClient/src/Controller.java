import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class Controller {

    private ClientManager _clientManager = new ClientManager();

    @FXML
    public TitledPane branchesPane;
    @FXML
    public GridPane branchesGridPane;
    @FXML
    private Button btn;
    @FXML
    private Accordion sideMenu;


    @FXML
    public void createNewRepository(ActionEvent actionEvent) {

        if (_clientManager.createRepository()) {
            initRepo();
        }
    }

    public void loadRepository(ActionEvent actionEvent) {

        if (_clientManager.loadRepository()) {

            initRepo();
        }
    }

    private void initRepo() {

        ArrayList<String> availableBranches = _clientManager.getAvailableBranches();
        for (int i = 0; i < availableBranches.size(); i++) {
            Label branchLabel = new Label(availableBranches.get(i));
            branchLabel.setId(availableBranches.get(i));
            branchesGridPane.add(branchLabel, 0, i);
        }

        branchesPane.setContent(branchesGridPane);
        sideMenu.getPanes().set(sideMenu.getPanes().indexOf(branchesPane), branchesPane);
        sideMenu.setExpandedPane(branchesPane);
    }
}
