package application;

import controller.ShellController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        setPrimaryStage(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/shell.fxml"));
        Parent root = fxmlLoader.load();
        ShellController shellController = fxmlLoader.getController();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/icon.png")));

        Scene scene = new Scene(root);

        stage.setTitle("Checkout Simulator");
        stage.setScene(scene);
        stage.setMinWidth(root.prefHeight(Parent.BASELINE_OFFSET_SAME_AS_HEIGHT));
        stage.setMinHeight(root.prefHeight(Parent.BASELINE_OFFSET_SAME_AS_HEIGHT));
        stage.setHeight(root.prefHeight(Parent.BASELINE_OFFSET_SAME_AS_HEIGHT));
        stage.setWidth(root.prefWidth(Parent.BASELINE_OFFSET_SAME_AS_HEIGHT));
        stage.show();

        primaryStage.setOnCloseRequest(windowEvent -> {
            shellController.exit();
            System.exit(0);
        });
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    private void setPrimaryStage(Stage p){
        primaryStage = p;
    }

}
