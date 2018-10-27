package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ShellController extends Controller {
    public AnchorPane mainContainer;
    public AnchorPane sideContainer;
    public Menu menuOption;

    public void initialize(URL location, ResourceBundle resources) {

        try {
            loadView("simulator.fxml", mainContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            loadView("output.fxml", sideContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadView(String viewName, Pane container) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Node newPane = fxmlLoader.load(getClass().getResource("/view/" + viewName));

        Controller controller = fxmlLoader.getController();

        if(controller != null)
        {
            if ( controller.getClass() == OutputController.class ){
                model.outputController = (OutputController) controller;
            }
            controller.setModel(model);
        }

        container.getChildren().setAll(newPane);
    }
}
