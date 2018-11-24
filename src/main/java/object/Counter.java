package object;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.joda.time.DateTime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Counter extends StackPane {
    private Circle counterStatusCircle;
    private Tooltip tooltip;

    private int no;
    private boolean status;
    private boolean busying;
    private int type;
    private int totalServed;
    private DateTime totalServedTime;
    private ScheduledExecutorService tooltipUpdateExecutorService;
    private ScheduledFuture<?> tooltipUpdateTask;
    private ScheduledExecutorService timeCountService;
    private ScheduledFuture<?> timeCountTask;

    public Counter(int no, int type, boolean status) {
        this.no = no;
        this.status = status;
        this.type = type;
        totalServed = 0;
        totalServedTime = new DateTime().secondOfDay().setCopy(0);

        ImageView counterImageView = new ImageView();
        counterImageView.setFitHeight(150);
        counterImageView.setFitWidth(200);
        counterImageView.setPickOnBounds(true);
        counterImageView.setPreserveRatio(true);
        Image image = new Image(getClass().getResourceAsStream("/image/counter.png"));
        counterImageView.setImage(image);

        tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setStyle("-fx-font-weight: bold");
        setOnMouseMoved(mouseEvent -> {
            tooltip.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX() + 5, mouseEvent.getScreenY() + 15);
        });
        setOnMouseExited(mouseEvent -> {
            tooltip.hide();
        });
        tooltipUpdateExecutorService = Executors.newSingleThreadScheduledExecutor();
        timeCountService = Executors.newSingleThreadScheduledExecutor();
        initTooltipService(500);

        counterStatusCircle = new Circle();
        counterStatusCircle.setStrokeWidth(25.0);
        StackPane.setAlignment(counterStatusCircle, Pos.TOP_RIGHT);

        Label label = new Label();
        StackPane.setAlignment(label, Pos.TOP_LEFT);
        label.setStyle("-fx-font-style: italic;-fx-font-weight: bold;" +
                "-fx-font-size: 16");
        //;-fx-background-color: #00cf53b9
        if (type == Checkout.CheckoutChannelType.EXPRESSWAY) {
            label.setText("EXPRESSWAY");
        } else {
            label.setText(String.valueOf(no));
        }
        getChildren().addAll(counterImageView, counterStatusCircle, label);
        setBusying(false, 0);
    }

    public void setBusying(boolean busyingStatus, int playSpeedDivide) {
        if (busyingStatus == busying) {
            return;
        }
        busying = busyingStatus;
        if (busyingStatus) {
            counterStatusCircle.setStroke(Paint.valueOf("#eae600"));
            initTimeCountService(playSpeedDivide);
        } else {
            counterStatusCircle.setStroke(Paint.valueOf("limegreen"));
            initTimeCountService(0);
        }
    }

    public void initTimeCountService(int playSpeedDivide) {
        if (timeCountTask != null) {
            timeCountTask.cancel(false);
        }
        if (playSpeedDivide != 0) {
            int period = 1000000 / playSpeedDivide;
            timeCountTask = timeCountService.scheduleAtFixedRate(() -> {
                totalServedTime = totalServedTime.plusSeconds(1);
            }, period, period, TimeUnit.MICROSECONDS);
        }
    }

    public void initTooltipService(long period) {
        if (tooltipUpdateTask != null) {
            tooltipUpdateTask.cancel(false);
        }
        tooltipUpdateTask = tooltipUpdateExecutorService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            tooltip.setText("Checkout " + no + "\n\n" +
                    "status: " + (status ? "busying" : "idle") + "\n" +
                    "served customers: " + totalServed + "\n" +
                    "valid time: " + totalServedTime.getSecondOfDay() + "s\n" +
                    "type: " + (type == Checkout.CheckoutChannelType.NORMAL ? "normal" : "expressway"));
        }), 0, period, TimeUnit.MILLISECONDS);
    }

    public int getNo() {
        return no;
    }

    public boolean isStatus() {
        return status;
    }

    public int getType() {
        return type;
    }

    public Circle getCounterStatusCircle() {
        return counterStatusCircle;
    }

    public int updateTotalServed(int plus) {
        return totalServed += plus;
    }

}
