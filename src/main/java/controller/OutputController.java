package controller;

import javafx.scene.control.ListView;
import model.LogModel;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class OutputController extends Controller {
    public ListView<String> logListView;

    public List<LogModel> logModelList;

    public void initialize(URL location, ResourceBundle resources) {
        logModelList = new LinkedList<>();

        //TODO add icon in listview
//        logListView.setCellFactory(stringListView -> new ListCell<>() {
//            //            private ImageView imageView = new ImageView();
//            private Tooltip tooltip = new Tooltip();
//
//            @Override
//            public void updateItem(String item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty) {
//                    setTooltip(null);
////                    setGraphic(null);
//                } else {
//                    tooltip.setText(item);
//                    setTooltip(tooltip);
//                // true makes this load in background
//                // see other constructors if you want to control the size, etc
////                    Image image = new Image(item, true) ;
////                    imageView.setImage(image);
////                    setGraphic(imageView);
//                }
//            }
//        });
    }

    public void clearLogList() {
        logListView.getItems().clear();
        logModelList.clear();
    }

    public void addLog(String text) {
        Date now = new Date();
        logModelList.add(new LogModel(text, now));

        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
        logListView.getItems().add("[" + dateFormatter.format(now) + "] " + text);
    }
}
