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
    public Button createRepositoryBtn;
    @FXML
    public Button loadRepositoryBtn;
    @FXML
    public Button changeUserNameBtn;
    @FXML
    public Button showWcBtn;
    @FXML
    public Button commitBtn;
    @FXML
    public Button showCommitBtn;
    @FXML
    public Button showCurrentBranchBtn;
    @FXML
    public Button createBranchBtn;
    @FXML
    public Button deleteBranchBtn;
    @FXML
    public Button checkoutBranchBtn;
    @FXML
    public Button resetBranchBtn;
    @FXML
    public Button loadXmlBtn;
    @FXML
    public TitledPane branchesPane;
    @FXML
    public GridPane branchesGridPane;
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

    public void loadRepositoryWithXML(ActionEvent actionEvent) {
    }

    public void changeUserName(ActionEvent actionEvent) {
    }

    public void showWC(ActionEvent actionEvent) {
    }

    public void commit(ActionEvent actionEvent) {
    }

    public void showCommit(ActionEvent actionEvent) {
    }

    public void showCurrentBranch(ActionEvent actionEvent) {
    }

    public void createBranch(ActionEvent actionEvent) {
    }

    public void deleteBranch(ActionEvent actionEvent) {
    }

    public void checkoutBranch(ActionEvent actionEvent) {
    }

    public void resetBranch(ActionEvent actionEvent) {
    }
}
