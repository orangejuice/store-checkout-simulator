package object;

import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

public class Customer extends StackPane {
    private ImageView customerImageView;
    private Arc arc;

    private int no;
    private int quantityOfGoods;
    private int quantityWaitForCheckout;
    private boolean isBeingServed;
    private boolean cannotWait;
    private int waitSec;
    private int waitSecRemaining;

    public Customer(int no, int quantityOfGoods, boolean cannotWait, int waitSec) {
        this.no = no;
        this.quantityOfGoods = quantityOfGoods;
        this.quantityWaitForCheckout = quantityOfGoods;
        this.cannotWait = cannotWait;
        this.waitSecRemaining = waitSec;
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

        customerImageView.setOnMouseEntered(mouseEvent -> {
            Tooltip tooltip = new Tooltip("Customer " + no + "\n\n" +
                    "quantity of goods:" + quantityOfGoods + "\n" +
                    "quantity of goods waiting for serve:" + quantityWaitForCheckout + "\n" +
                    "temper:" + (cannotWait ? "bad :(" : "good :)") +
                    (cannotWait ? "\nwill leave after:" + waitSecRemaining : "")
            );
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setStyle("-fx-font-weight: bold");
            Tooltip.install(customerImageView, tooltip);
        });

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

    public int getWaitSec() {
        return waitSec;
    }

    public int getWaitSecRemaining() {
        return waitSecRemaining;
    }

    public void setWaitSecRemaining(int waitSecRemaining) {
        this.waitSecRemaining = waitSecRemaining;
    }

    public boolean isBeingServed() {
        return isBeingServed;
    }

    public void setBeingServed(boolean beingServed) {
        isBeingServed = beingServed;
    }
}
