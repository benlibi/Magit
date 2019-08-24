import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private ClientManager clientManager = new ClientManager();

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("magitFxClient.fxml"));
        Scene scene = new Scene(root, 800, 675);

        primaryStage.setTitle("M.a.g.i.t");
        primaryStage.setScene(scene);
        primaryStage.show();
//
//        final Accordion accordion = new Accordion();
//
//
//
//        accordion.getPanes().addAll(gridTitlePane);
//        accordion.setExpandedPane(gridTitlePane);


//        clientManager.initBranchView(scene);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
