package controller;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class OutputController extends Controller {
    public ListView<Label> logListView;

    public void initialize(URL location, ResourceBundle resources) {

    }

    public void clearLogList() {
        logListView.getItems().clear();
    }

    public void addLog(String text, Level level) {
        Date now = new Date();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

        FontIcon fontIcon = new FontIcon();
        if (level == Level.WARNING) {
            fontIcon.setIconLiteral("fas-exclamation-circle");
            fontIcon.setIconColor(Paint.valueOf("#e00000"));
        } else if (level == Level.CONFIG) {
            fontIcon.setIconLiteral("fas-check-circle");
            fontIcon.setIconColor(Paint.valueOf("#00e070"));
        } else {
            fontIcon.setIconLiteral("fas-info-circle");
            fontIcon.setIconColor(Paint.valueOf("#0070e0"));
        }
        Label label = new Label("[" + dateFormatter.format(now) + "] " + text, fontIcon);
        logListView.getItems().add(label);
    }
}
