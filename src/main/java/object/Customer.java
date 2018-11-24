package object;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Customer extends StackPane {
    private ImageView customerImageView;
    private Tooltip tooltip;
    private Arc arc;

    private int no;
    private int quantityOfGoods;
    private int quantityWaitForCheckout;
    private boolean isBeingServed;
    private boolean cannotWait;
    private int waitSec;
    private int waitSecActual;
    private ScheduledExecutorService tooltipUpdateExecutorService;
    private ScheduledFuture<?> tooltipUpdateTask;

    public Customer(int no, int quantityOfGoods, boolean cannotWait, int waitSec) {
        this.no = no;
        this.quantityOfGoods = quantityOfGoods;
        this.quantityWaitForCheckout = quantityOfGoods;
        this.cannotWait = cannotWait;
        this.waitSecActual = 0;
        this.waitSec = waitSec;
        this.isBeingServed = false;

        customerImageView = new ImageView();
        customerImageView.setFitHeight(150);
        customerImageView.setFitWidth(200);
        customerImageView.setPickOnBounds(true);
        customerImageView.setPreserveRatio(true);
        customerImageView.setId("Customer");
        Image image = new Image(getClass().getResourceAsStream("/image/trolley.png"));
        customerImageView.setImage(image);

        tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setStyle("-fx-font-weight: bold");
        tooltip.setWrapText(true);
        setOnMouseMoved(mouseEvent -> {
            tooltip.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX() + 5, mouseEvent.getScreenY() + 15);
        });
        setOnMouseExited(mouseEvent -> {
            tooltip.hide();
        });
        tooltipUpdateExecutorService = Executors.newSingleThreadScheduledExecutor();
        initTooltipService(500);

        arc = new Arc();
        arc.setFill(Paint.valueOf("#2197ff80"));
        arc.setLength(0);
        arc.setRadiusX(25);
        arc.setRadiusY(25);
        arc.setStartAngle(90);
        arc.setStroke(Paint.valueOf("black"));
        arc.setStrokeType(StrokeType.INSIDE);
        arc.setType(ArcType.ROUND);
        StackPane.setAlignment(arc, Pos.CENTER);

        getChildren().addAll(customerImageView, arc);
    }

    public void initTooltipService(long period) {
        if (tooltipUpdateTask != null) {
            tooltipUpdateTask.cancel(false);
        }
        tooltipUpdateTask = tooltipUpdateExecutorService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            tooltip.setText("Customer " + no + "\n\n" +
                    "quantity of goods: " + quantityOfGoods + "\n" +
                    "waiting for served: " + quantityWaitForCheckout + "\n" +
                    "waiting time: " + waitSecActual + "s\n" +
                    "temper: " + (cannotWait ? "bad :(" : "good :)") +
                    (cannotWait ? "\nwill leave after:\t\t" + (waitSec - waitSecActual) : ""));
        }), 0, period, TimeUnit.MILLISECONDS);
    }

    public Arc getArc() {
        return arc;
    }

    public int getNo() {
        return no;
    }

    public int getQuantityOfGoods() {
        return quantityOfGoods;
    }

    public int getQuantityWaitForCheckout() {
        return quantityWaitForCheckout;
    }

    public void setQuantityWaitForCheckout(int quantityWaitForCheckout) {
        this.quantityWaitForCheckout = quantityWaitForCheckout;
    }

    public boolean isCannotWait() {
        return cannotWait;
    }

    public int getWaitSec() {
        return waitSec;
    }

    public boolean isBeingServed() {
        return isBeingServed;
    }

    public void setBeingServed(boolean beingServed) {
        isBeingServed = beingServed;
    }

    public int updateWaitSecActual(int plusSec) {
        return waitSecActual += plusSec;
    }
}
