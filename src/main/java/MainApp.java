import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        setPrimaryStage(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/shell.fxml"));
        Parent root = fxmlLoader.load();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/icon.png")));

        Scene scene = new Scene(root);

        stage.setTitle("Checkout Simulator");
        stage.setScene(scene);
        stage.setMinWidth(root.minWidth(-1));
        stage.setMinHeight(root.minHeight(-1) + 50);
        stage.setHeight(root.prefHeight(-1));
        stage.setWidth(root.prefWidth(-1));
        stage.show();
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    private void setPrimaryStage(Stage p){
        primaryStage = p;
    }

}
