package controller;


import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import model.MainModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class Controller implements Initializable {

    protected MainModel model;

    public abstract void initialize(URL location, ResourceBundle resources);

    public void setModel(MainModel model) {
        this.model = model;
    }

    protected Pane loadView(String viewName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/" + viewName));
        Pane newPane = fxmlLoader.load();
        Controller ctrl = fxmlLoader.getController();
        ctrl.setModel(model);

        return newPane;
    }

    protected void loadView(String viewName, Pane container) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/" + viewName));
        Node newPane = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();

        //System.out.println(viewName + ":" + container + ":" + model);
        if (controller != null) {
            if (controller.getClass() == OutputController.class) {
                model.outputController = (OutputController) controller;
            } else if (controller.getClass() == PreferenceController.class) {
                model.preferenceController = (PreferenceController) controller;
            } else if (controller.getClass() == SimulatorController.class) {
                model.simulatorController = (SimulatorController) controller;
            } else if (controller.getClass() == StatisticsController.class) {
                model.statisticsController = (StatisticsController) controller;
            }
            controller.setModel(model);
        }

        container.getChildren().setAll(newPane);
    }
}
