package object;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;
import model.MainModel;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CustomerQueue extends HBox {
    private static Image trolley;
    public Label quantityLabel;
    private Tooltip tooltip;
    private Arc arc;
    private Customer nowCustomer;
    private ScheduledFuture<?> tooltipUpdateTask;

    public CustomerQueue() {
        ImageView customerImageView = new ImageView();
        customerImageView.setFitHeight(150);
        customerImageView.setFitWidth(200);
        customerImageView.setPickOnBounds(true);
        customerImageView.setPreserveRatio(true);
        if (trolley == null) {
            trolley = new Image(getClass().getResourceAsStream("/image/trolley.png"));
        }
        customerImageView.setImage(trolley);

        tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setStyle("-fx-font-weight: bold");

        arc = new Arc(30, 30, 25, 25, 90, 0);
        arc.setFill(Paint.valueOf("#2197ff80"));
        arc.setStroke(Paint.valueOf("black"));
        arc.setStrokeType(StrokeType.INSIDE);
        arc.setType(ArcType.ROUND);
        Rectangle fill = new Rectangle(60, 60, Color.TRANSPARENT);
        Group group = new Group(arc, fill);
        StackPane.setAlignment(group, Pos.CENTER);

        StackPane firstCustomer = new StackPane(customerImageView, group);
        firstCustomer.setOnMouseMoved(mouseEvent -> {
            initTooltipService(500);
            tooltip.show((Node) mouseEvent.getSource(),
                    mouseEvent.getScreenX() + 10, mouseEvent.getScreenY() + 15);
        });
        firstCustomer.setOnMouseExited(mouseEvent -> {
            initTooltipService(0);
            tooltip.hide();
        });
        quantityLabel = new Label();
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconLiteral("fas-ellipsis-h");
        fontIcon.setIconColor(Color.rgb(0, 0, 0, 0.4));
        quantityLabel.setGraphic(fontIcon);
        quantityLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: rgba(0, 0, 0, 0.4)");
        setAlignment(Pos.BOTTOM_LEFT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        getChildren().addAll(firstCustomer, quantityLabel);
        setVisible(false);
    }

    public void initTooltipService(long period) {
        if (tooltipUpdateTask != null) {
            tooltipUpdateTask.cancel(false);
        }
        if (period == 0) {
            return;
        }
        tooltipUpdateTask = MainModel.getInstance().getThreadPoolExecutor().scheduleAtFixedRate(() -> Platform.runLater(() -> {
            tooltip.setText("Customer " + nowCustomer.getNo() + "\n\n" +
                    "quantity of goods: " + nowCustomer.getQuantityOfGoods() + "\n" +
                    "waiting for served: " + nowCustomer.getQuantityWaitForCheckout() + "\n" +
                    "waiting time: " + nowCustomer.getWaitSecActual() + "s\n" +
                    "temper: " + (nowCustomer.isCannotWait() ? "bad :(" : "good :)") +
                    (nowCustomer.isCannotWait() ? "\nwill leave after:" + (nowCustomer.getWaitSec() - nowCustomer.getWaitSecActual()) + "s" : ""));
        }), 0, period, TimeUnit.MILLISECONDS);
    }

    public void updateQuantity(int quantity) {
        if (quantity > 0) {
            Platform.runLater(() -> {
                setVisible(true);
                quantityLabel.setText(String.valueOf(quantity));
            });
        } else {
            Platform.runLater(() -> setVisible(false));
        }
    }

    public void updateArc(double v) {
        synchronized (MainModel.getInstance()) {
            arc.lengthProperty().setValue(v);
        }
    }

    public void setNowCustomer(Customer nowCustomer) {
        this.nowCustomer = nowCustomer;
    }
}
