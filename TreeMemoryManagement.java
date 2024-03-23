import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Author: Asif Qureshi, Cole Keller, Goldhuli, Sharjeel
 * Start the program and load the GUI
 */
public class TreeMemoryManagement extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UI.fxml"));
        Parent root = (Parent)loader.load();
        ((Controller) loader.getController()).setStage(primaryStage);

        primaryStage.setTitle("Tree Memory Management");
        primaryStage.setScene(new Scene(root, 950, 775));
        primaryStage.show();
    }
}
