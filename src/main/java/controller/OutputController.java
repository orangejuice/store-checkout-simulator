package controller;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Paint;
import object.Checkout;
import object.Customer;
import org.joda.time.format.DateTimeFormat;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class OutputController extends Controller {
    public ListView<Label> logListView;
    public ProgressBar processBar;

    public void initialize(URL location, ResourceBundle resources) {

    }

    public void storeInitEvent() {
        logListView.getItems().clear();
        processBar.setProgress(0);
        //todo init all charts
    }

    public void checkoutInitEvent(List<Checkout> checkouts) {
        long normal = checkouts.stream().filter(checkout -> checkout.getType() == Checkout.CheckoutType.NORMAL).count();
        long expressway = checkouts.stream().filter(checkout -> checkout.getType() == Checkout.CheckoutType.EXPRESSWAY).count();

        model.outputController.addLog("[store] ready", Level.CONFIG);
        model.outputController.addLog("[store] equipped with " + normal + " checkout", Level.CONFIG);
        if (expressway > 0) {
            model.outputController.addLog("[store] equipped with " + expressway + " expressway checkout", Level.CONFIG);
        }
    }

    public void customerComeEvent(Customer customer) {
        addLog("[customer] [new] customer" + customer.getNo() + " Goods:" + customer.getQuantityOfGoods() + ",temper:" +
                (customer.isCannotWait() ? "Bad, leave after " + customer.getWaitSec() + "s" : "Good"), Level.FINE);
    }

    public void customerCheckoutEvent(Checkout checkout, Customer customer) {
        String waitSecPeriod;
        if (customer.getWaitSecActual() <= 60) {
            waitSecPeriod = "<1min";
        } else if (customer.getWaitSecActual() <= 300) {
            waitSecPeriod = "1-5min";
        } else if (customer.getWaitSecActual() <= 600) {
            waitSecPeriod = "5-10min";
        } else if (customer.getWaitSecActual() <= 900) {
            waitSecPeriod = "10-15min";
        } else if (customer.getWaitSecActual() <= 1200) {
            waitSecPeriod = "15-20min";
        } else {
            waitSecPeriod = ">20min";
        }
        model.statisticsController.updateWaitTimeDistributionPieAddNewData(waitSecPeriod);
        addLog("[checkout" + checkout.getCounter().getNo() + "] served a customer", Level.INFO);
    }

    public void customerLeaveEvent(Customer customer) {
        model.statisticsController.updateWaitTimeDistributionPieAddNewData("leave");
        addLog("[customer] [leave] customer" + customer.getNo()
                + " leaved after waiting for " + customer.getWaitSec() + "s", Level.WARNING);
    }

    public void addLog(String text, Level level) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconLiteral("fas-plus-circle");
        fontIcon.setIconColor(Paint.valueOf("#00ad57"));

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
        Platform.runLater(() -> logListView.getItems().add(label));
        //todo follow when scrollbar show up
    }
}
