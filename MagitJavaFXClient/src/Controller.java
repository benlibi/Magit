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
    public Button ChangeUserBtn;
    @FXML
    public Button createRepositoryBtn;
    @FXML
    public Button loadRepositoryBtn;
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

//    public void changeUserName(ActionEvent actionEvent) {
//        _clientManager.changeUserName();
//    }

    public void showWC(ActionEvent actionEvent) {
        _clientManager.showWC();
    }

    public void commit(ActionEvent actionEvent) {
        _clientManager.commit();
    }

    public void showCommit(ActionEvent actionEvent) {
        _clientManager.showCommit();
    }

    public void showCurrentBranch(ActionEvent actionEvent) {
        _clientManager.showCurrentBranch();
    }

    public void createBranch(ActionEvent actionEvent) {
        _clientManager.createBranch();
    }

    public void deleteBranch(ActionEvent actionEvent) {
        _clientManager.deleteBranch();
    }

    public void checkoutBranch(ActionEvent actionEvent) {
        _clientManager.checkoutBranch();
    }

    public void resetBranch(ActionEvent actionEvent) {
        _clientManager.resetBranch();

    }

    public void changeUserBtn(ActionEvent actionEvent) {
        _clientManager.changeUser();
    }
}