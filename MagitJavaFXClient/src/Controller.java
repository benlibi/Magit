import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.stream.Collectors;

public class Controller {

    private ClientManager _clientManager = new ClientManager();

    @FXML
    public BorderPane BorderLayoutPane;
    @FXML
    public HBox leftHbox;
    @FXML
    public ScrollPane branchesViewPane;
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

        loadBranchesView();
    }

    private void loadBranchesView() {
        ListView<Button> list = new ListView<Button>();
        ObservableList<Button> items = FXCollections.observableArrayList(
                _clientManager.getAvailableBranches().stream()
                        .map(branch -> {
                            Button branchRepresentation = new Button(branch);
                            branchRepresentation.setPrefWidth(200);
                            branchRepresentation.setAlignment(Pos.CENTER_LEFT);
                            branchRepresentation.setBackground(Background.EMPTY);
                            branchRepresentation.setId(branch);
                            branchRepresentation.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    onBranchClick(event);
                                }
                            });

                            return branchRepresentation;
                        })
                        .collect(Collectors.toList())
        );

        list.setItems(items);
        branchesViewPane.setContent(list);
        branchesViewPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        branchesViewPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

    }

    public void loadRepositoryWithXML(ActionEvent actionEvent) {
        if (_clientManager.loadXMLRepository()) {
            initRepo();
        }
    }

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

    private void onBranchClick(ActionEvent actionEvent) {

        Button tempBranchBtn = (Button)actionEvent.getSource();
        tempBranchBtn.setText("Ben");
    }
}