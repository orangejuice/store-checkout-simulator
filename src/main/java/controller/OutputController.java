package controller;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Paint;
import org.joda.time.format.DateTimeFormat;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class OutputController extends Controller {
    public ListView<Label> logListView;
    public ProgressBar processBar;

    public void initialize(URL location, ResourceBundle resources) {

    }

    public void clearLogList() {
        logListView.getItems().clear();
    }

    public void addLog(String text, Level level) {
        FontIcon fontIcon = new FontIcon();
        if (level == Level.WARNING) {
            fontIcon.setIconLiteral("fas-exclamation-circle");
            fontIcon.setIconColor(Paint.valueOf("#e00000"));
        } else if (level == Level.CONFIG) {
            fontIcon.setIconLiteral("fas-info-circle");
            fontIcon.setIconColor(Paint.valueOf("#0070e0"));
        } else if (level == Level.FINE) {
            fontIcon.setIconLiteral("fas-plus-circle");
            fontIcon.setIconColor(Paint.valueOf("#00ad57"));
        } else {
            fontIcon.setIconLiteral("fas-check-circle");
            fontIcon.setIconColor(Paint.valueOf("#00ad57"));
        }
        Label label = new Label("[" + DateTimeFormat.forPattern("HH:mm:ss")
                .print(model.simulatorController.getSimulateTime()) + "] " + text, fontIcon);
        logListView.getItems().add(label);
        //todo follow when scrollbar show up
    }
}
