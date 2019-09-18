import Enums.ConflictSections;
import Models.Conflict;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.PannableCanvas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {


    private Graph tree = new Graph();

    private ClientManager _clientManager = new ClientManager();

    @FXML
    public ScrollPane remoteBranchesViewPane;
    @FXML
    public Button pullBtn;
    @FXML
    public Button pushBtn;
    @FXML
    public Button fetchBtn;
    @FXML
    public Button cloneBtn;
    @FXML
    public Text repoStatusText;
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
    public ScrollPane scrollpaneContainer;

    @FXML
    public void createNewRepository(ActionEvent actionEvent) {

        if (_clientManager.createRepository((Stage) ((Node) actionEvent.getSource()).getScene().getWindow())) {
            initRepo();
        }
    }

    public void loadRepository(ActionEvent actionEvent) {

        if (_clientManager.loadRepository((Stage) ((Node) actionEvent.getSource()).getScene().getWindow())) {

            initRepo();
        }
    }

    private void initRepo() {
        loadBranchesView();
        loadRemoteBranchView();
        initGraph();
        repoStatusText.setText(_clientManager.getRepoStatus());
    }

    private void loadRemoteBranchView() {
        ListView<Button> list = new ListView<Button>();
        ObservableList<Button> items = FXCollections.observableArrayList(
                _clientManager.getAvailableRemoteBranches().stream()
                        .map(branch -> {
                            Button branchRepresentation = new Button(branch);
                            branchRepresentation.setPrefWidth(200);
                            branchRepresentation.setAlignment(Pos.CENTER_LEFT);
                            branchRepresentation.setBackground(Background.EMPTY);
                            branchRepresentation.setId(branch);
                            branchRepresentation.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    onRemoteBranchClick(event);
                                }
                            });

                            initBranchContextMenu(branchRepresentation);

                            return branchRepresentation;
                        })
                        .collect(Collectors.toList())
        );

        list.setItems(items.sorted());
        branchesViewPane.setContent(list);
        branchesViewPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        branchesViewPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

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

                            initBranchContextMenu(branchRepresentation);

                            return branchRepresentation;
                        })
                        .collect(Collectors.toList())
        );

        list.setItems(items.sorted());
        branchesViewPane.setContent(list);
        branchesViewPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        branchesViewPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

    }

    public void loadRepositoryWithXML(ActionEvent actionEvent) {
        if (_clientManager.loadXMLRepository((Stage) ((Node) actionEvent.getSource()).getScene().getWindow())) {
            initRepo();
        }
    }

    public void showWC(ActionEvent actionEvent) {
        _clientManager.showWC();
    }

    public void commit(ActionEvent actionEvent) {
        _clientManager.commit();
        initRepo();
    }

    public void showCommit(ActionEvent actionEvent) {
        initRepo();
    }

    public void createBranch(ActionEvent actionEvent) {
        _clientManager.createBranch();

        loadBranchesView();
    }

    public void deleteBranch(ActionEvent actionEvent) {
        _clientManager.deleteBranch();

        loadBranchesView();
    }

    public void resetBranch(ActionEvent actionEvent) {
        _clientManager.resetBranch();
    }

    public void changeUserBtn(ActionEvent actionEvent) {
        _clientManager.changeUser();
    }

    private void onBranchClick(ActionEvent actionEvent) {
        Button branchBtn = (Button) actionEvent.getSource();
        _clientManager.checkoutBranch(branchBtn.getId());

        loadBranchesView();
    }

    private void onRemoteBranchClick(ActionEvent actionEvent) {
        Button branchBtn = (Button) actionEvent.getSource();
        _clientManager.checkoutRemoteBranch(branchBtn.getId());

        loadRemoteBranchView();
    }


////////////////////////////CommitNodeController////////////////////////////////////////

    @FXML
    private Label commitTimeStampLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label committerLabel;
    @FXML
    private Circle CommitCircle;
    @FXML
    private Label branchLabel;
    @FXML
    public Label sha1Label;

    void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }

    void setCommitSha1(String sha1) {
        sha1Label.setText(sha1);
        sha1Label.setTooltip(new Tooltip(sha1));
    }

    void setBranchName(String branchName) {
        branchLabel.setText(branchName);
        branchLabel.setTooltip(new Tooltip(branchName));
    }

    void setCircleId(String sha1, String rootPath){
        CommitCircle.setId(sha1+ ";" + rootPath);

    }

    int getCircleRadius() {
        return (int) CommitCircle.getRadius();
    }


    private void initGraph() {
        tree = new Graph();
        tree.getUseNodeGestures().set(false);
        tree.getUseViewportGestures().set(false);
        tree.beginUpdate();
        _clientManager.createGraph(tree);

        PannableCanvas canvas = tree.getCanvas();

        scrollpaneContainer.setContent(canvas);

    }

    private void initBranchContextMenu(Button branch) {

        // create a menu
        ContextMenu contextMenu = new ContextMenu();

        // create menuitems
        MenuItem menuItem1 = new MenuItem("Merge Current Branch Onto Here");
        menuItem1.setUserData(branch.getId());
        MenuItem menuItem2 = new MenuItem("Delete Branch");
        menuItem2.setUserData(branch.getId());

        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MenuItem item = (MenuItem) event.getSource();
                try {
                    if (!item.getUserData().toString().contains("HEAD")) {
                        String branchName = item.getUserData().toString().replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(" HEAD",
                                "");
                        List<Conflict> conflicts = _clientManager.merge(branchName);
                        if (conflicts.size() > 0) {
                            conflicts.forEach(conflict -> handleConflict(conflict));
                        }
                        _clientManager.commit(branchName);
                        initRepo();
                    } else {
                        throw new IOException("cant merge HEAD branch into HEAD branch");
                    }
                } catch (IOException e) {
                    _clientManager.handleException(e);
                }
            }
        });

        menuItem2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteBranch(event);
            }
        });

        // add menu items to menu
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);

        // create a tilepane
        TilePane tilePane = new TilePane(branch);

        // setContextMenu to label
        branch.setContextMenu(contextMenu);
    }

    private void handleConflict(Conflict conflict) {

        ScrollPane conflictPane = new ScrollPane();
        conflictPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        conflictPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        conflictPane.setFitToHeight(true);
        conflictPane.setFitToWidth(true);
        conflictPane.setPrefWidth(1200);

        GridPane gridpane = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        col3.setHgrow(Priority.ALWAYS);

        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        col4.setHgrow(Priority.ALWAYS);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(10);
        row1.setValignment(VPos.CENTER);
        gridpane.getRowConstraints().add(0, row1);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(90);
        row2.setValignment(VPos.TOP);
        gridpane.getRowConstraints().add(1, row2);

        gridpane.getColumnConstraints().addAll(col1, col2, col3, col4);
        gridpane.setPrefHeight(800);
        gridpane.setPrefWidth(1200);
        gridpane.setAlignment(Pos.TOP_CENTER);
        gridpane.setGridLinesVisible(true);


//      YOUR VERSION
//      ----------------------------
        ScrollPane blobPane = new ScrollPane();
        blobPane.setFitToHeight(false);
        blobPane.setFitToWidth(false);
        blobPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        blobPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        blobPane.setContent(new Text(conflict.getRelatedBlobs().get(ConflictSections.YOUR_VERSION)));

        if (conflict.getRelatedBlobs().get(ConflictSections.YOUR_VERSION).isEmpty()) {
            blobPane.setContent(new Text("File Was Removed..."));
            blobPane.setStyle("-fx-background: rgb(255, 143, 143);");
        }

        gridpane.add(new Label(ConflictSections.YOUR_VERSION.name()), 0, 0);
        gridpane.add(blobPane, 0, 1);

//      THEIR VERSION
//      ----------------------------
        blobPane = new ScrollPane();
        blobPane.setFitToHeight(false);
        blobPane.setFitToWidth(false);
        blobPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        blobPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        blobPane.setContent(new Text(conflict.getRelatedBlobs().get(ConflictSections.THEIR_VERSION)));

        if (conflict.getRelatedBlobs().get(ConflictSections.THEIR_VERSION).isEmpty()) {
            blobPane.setContent(new Text("File Was Removed..."));
            blobPane.setStyle("-fx-background: rgb(255, 143, 143);");
        }

        gridpane.add(new Label(ConflictSections.THEIR_VERSION.name()), 1, 0);
        gridpane.add(blobPane, 1, 1);

//      ORIGIN
//      ----------------------------
        blobPane = new ScrollPane();
        blobPane.setFitToHeight(false);
        blobPane.setFitToWidth(false);
        blobPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        blobPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        blobPane.setContent(new Text(conflict.getRelatedBlobs().get(ConflictSections.ORIGIN)));

        if (conflict.getRelatedBlobs().get(ConflictSections.ORIGIN).isEmpty()) {
            blobPane.setContent(new Text("File Was Removed..."));
            blobPane.setStyle("-fx-background: rgb(255, 143, 143);");
        }

        gridpane.add(new Label(ConflictSections.ORIGIN.name()), 2, 0);
        gridpane.add(blobPane, 2, 1);

//      FINAL VERSION
//      ----------------------------
        blobPane = new ScrollPane();
        blobPane.setFitToHeight(false);
        blobPane.setFitToWidth(false);
        blobPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        blobPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        TextField blobFinalImage = new TextField();
//        blobFinalImage.setId(conflict.getFileSha1());
        blobFinalImage.setPrefHeight(800);
        blobFinalImage.setPrefWidth(600);
        blobFinalImage.setAlignment(Pos.TOP_LEFT);
        blobFinalImage.setText(conflict.getRelatedBlobs().get(ConflictSections.YOUR_VERSION));
        blobPane.setContent(blobFinalImage);
        gridpane.add(new Label("Final"), 3, 0);
        gridpane.add(blobPane, 3, 1);

        conflictPane.setContent(gridpane);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Please Resolve The Following Conflict");
        alert.setHeaderText(conflict.getFilePath() + "Found conflicts in this path, please resolve them and press OK");
        alert.getDialogPane().setContent(conflictPane);

        alert.setOnCloseRequest(e -> {
            if (blobFinalImage.getText().isEmpty()) {

                Alert emptyVersionAlert = new Alert(Alert.AlertType.CONFIRMATION);
                emptyVersionAlert.setTitle("Resolve Conflict");
                emptyVersionAlert.setContentText("You didn't enter any content for this file, do you want to remove it ?");
                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                emptyVersionAlert.getButtonTypes().setAll(okButton, noButton);
                emptyVersionAlert.showAndWait().ifPresent(type -> {
                    if (type == okButton) {
                        try {
                            _clientManager.deleteFile(conflict.getFilePath());
                        } catch (IOException ex) {
                            _clientManager.handleException(ex);
                        }
                    } else if (type == noButton) {
                        _clientManager.handleException(new FileSystemException("You Must Insert at least 1 char"));
                        e.consume();
                    }
                });
            }else{
                _clientManager.saveFile(blobFinalImage.getText(), conflict.getFilePath());
            }
        });
        alert.showAndWait();


        /*
        Map<ConflictSections, String> map = new HashMap<>();
        map.put(ConflictSections.ORIGIN,"bla1oiqheoiqwehqiowehoiqwheioqweoqwoeiqwoehqwiehqwoiehqwoeihqwoiehqwioehajsdnkasjdnomqwioheoqwuheoqiwehoqwuehoqwihejoqwiehoajsdnlaskdnqowehoqwiehoqwiehoqwiehoqwie");
        map.put(ConflictSections.THEIR_VERSION, "bla2");
        map.put(ConflictSections.YOUR_VERSION, "bla3");

        Conflict conflict = new Conflict("ben/path", "123123", map);
        handleConflict(conflict);
        */
    }

    public void pull(ActionEvent actionEvent) {
        _clientManager.pull();
    }

    public void push(ActionEvent actionEvent) {
        _clientManager.push();
    }

    public void fetch(ActionEvent actionEvent) {
        _clientManager.fetch();
    }

    public void clone(ActionEvent actionEvent) {
        _clientManager.gitClone();
    }


    public void showCommitChanges(String coomitSha1, String  rootrepo) {
        _clientManager.showChanges(coomitSha1,rootrepo);
    }

    public void showCommitStatus(String coomitSha1, String  rootrepo) {
        _clientManager.showStatus(coomitSha1,rootrepo);
    }

}