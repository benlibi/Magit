import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    @FXML
    public TitledPane branchesPane;
    @FXML
    public GridPane branchesGridPane;
    @FXML
    private Button btn;
    @FXML
    private Accordion sideMenu;
    private ClientManager _clientManager = new ClientManager();


    public void clicked(ActionEvent actionEvent) throws IOException {

    }

    @FXML
    public void createNewRepository(ActionEvent actionEvent) {
        _clientManager.createRepository();


        ArrayList<String> availableBranches = _clientManager.getAvailableBranches();
        for (int i = 0; i < availableBranches.size(); i++) {
            Label branchLabel = new Label(availableBranches.get(i));
            branchLabel.setId(availableBranches.get(i));
            branchesGridPane.add(branchLabel, 0, i);
        }

//        branchesPane.setText("Branches");
        branchesPane.setContent(branchesGridPane);

        sideMenu.getPanes().set(sideMenu.getPanes().indexOf(branchesPane), branchesPane);
        sideMenu.setExpandedPane(branchesPane);
    }

//    @FXML
//    public void createNewRepository(ActionEvent actionEvent) {
//        _clientManager.createRepository();
//
//        ArrayList<String> availableBranches = _clientManager.getAvailableBranches();
//
//        TitledPane gridTitlePane = new TitledPane();
//        GridPane grid = new GridPane();
//        grid.setVgap(4);
//        grid.setPadding(new Insets(5, 5, 5, 5));
//
//        availableBranches.forEach(branch -> {
//            Button button = new Button(branch);
//            button.setId(branch);
//            grid.add(button, 1, 0);
//        });
//
//        gridTitlePane.setText("Grid");
//        gridTitlePane.setContent(grid);
//
//        sideMenu.getPanes().add(gridTitlePane);
//        sideMenu.setExpandedPane(gridTitlePane);
//
//    }
}
