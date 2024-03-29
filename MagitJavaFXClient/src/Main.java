import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private ClientManager clientManager = new ClientManager();

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("magitFxClient.fxml"));
        Scene scene = new Scene(root, 800, 750);
        scene.getStylesheets().add("styles.css");



        primaryStage.setTitle("M.a.g.i.t");
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.runLater(() -> {
            Controller.getTree().getUseViewportGestures().set(false);
            Controller.getTree().getUseNodeGestures().set(false);
        });

    }


    public static void main(String[] args) {
        launch(args);
    }
}
