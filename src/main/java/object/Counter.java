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
import model.MainModel;
import org.joda.time.DateTime;
import util.PropertiesTool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Counter extends StackPane {
    private Circle counterStatusCircle;
    private Tooltip tooltip;

    private int no;
    private boolean status;
    private boolean busying;
    private int type;
    private int totalServedCustomers;
    private Map<Integer, Integer> totalServedProducts;
    private DateTime totalServedTime;
    private ScheduledFuture<?> tooltipUpdateTask;
    private ScheduledFuture<?> timeCountTask;
    private ScheduledFuture<?> scanTask;

    public Counter(int no, int type, boolean status) {
        this.no = no;
        this.status = status;
        this.type = type;
        totalServedCustomers = 0;

        totalServedTime = new DateTime().secondOfDay().setCopy(0);
        totalServedProducts = new HashMap<>();
        initTooltipService(500);
        initScanService(1000000);

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

        counterStatusCircle = new Circle();
        counterStatusCircle.setStrokeWidth(25.0);
        counterStatusCircle.setStroke(Paint.valueOf("limegreen"));
        StackPane.setAlignment(counterStatusCircle, Pos.TOP_RIGHT);

        Label label = new Label();
        StackPane.setAlignment(label, Pos.TOP_LEFT);
        label.setStyle("-fx-font-style: italic;-fx-font-weight: bold;" +
                "-fx-font-size: 16");
        //;-fx-background-color: #00cf53b9
        if (type == Checkout.CheckoutType.EXPRESSWAY) {
            label.setText("EXPRESSWAY");
        } else {
            label.setText(String.valueOf(no));
        }
        getChildren().addAll(counterImageView, counterStatusCircle, label);
    }

    private void initScanService(int scanItemInterval) {
        if (scanTask != null) {
            scanTask.cancel(false);
        }

        int playSpeedDivide = MainModel.getInstance().simulatorController.getPlaySpeedDivide();
        int period = scanItemInterval / playSpeedDivide;
        scanTask = MainModel.getInstance().getThreadPoolExecutor().scheduleAtFixedRate(() -> {
            if (!MainModel.getInstance().pauseStatus) {
                Checkout checkout = ((Checkout) getParent());
                if (checkout.getCustomers().size() > 0) {
                    setBusying(true, playSpeedDivide);
                    // offer poll/peek for queue
                    Customer nowCustomer = checkout.getCustomers().peek();
                    nowCustomer.setBeingServed(true);

                    int total = nowCustomer.getQuantityOfGoods();
                    int waitFor = nowCustomer.getQuantityWaitForCheckout();

                    // scan goods
                    Double from = Double.valueOf(PropertiesTool.getProps().getProperty(MainModel.getInstance().preferenceController.prefRangeOfEachProductScanTimeFrom.getId()));
                    Double to = Double.valueOf(PropertiesTool.getProps().getProperty(MainModel.getInstance().preferenceController.prefRangeOfEachProductScanTimeTo.getId()));
                    double v = ThreadLocalRandom.current().nextDouble(from, to);
                    initScanService((int) (v * 1000000));

                    int minute = MainModel.getInstance().simulatorController.getSimulateTime().getMinuteOfDay() + 1;
                    if (totalServedProducts.containsKey(minute)) {
                        totalServedProducts.compute(minute, (integer, integer2) -> integer2 + 1);
                    } else {
                        totalServedProducts.put(minute, 1);
                    }
                    nowCustomer.setQuantityWaitForCheckout(--waitFor);

                    // change the arc
                    synchronized (MainModel.getInstance()) {
                        checkout.getCustomerQueue().getArc().setLength(360.0 * waitFor / total);
                    }

                    // if 0, delete
                    if (waitFor == 0) {
                        totalServedCustomers += 1;
                        Customer customer = checkout.getCustomers().poll();
                        MainModel.getInstance().leftCustomers.add(customer);
                        MainModel.getInstance().outputController.customerCheckoutEvent(checkout, customer);
                        //todo Platform.runLater(() -> channel.getChildren().remove(nowCustomer));
                        customer.leave();
                    }
                } else {
                    setBusying(false, 0);
                    initScanService(1000000);
                }
            }
        }, period, period, TimeUnit.MICROSECONDS);
    }

    public void setBusying(boolean busyingStatus, int playSpeedDivide) {
        if (busyingStatus == busying) {
            return;
        }
        busying = busyingStatus;
        if (busyingStatus) {
            counterStatusCircle.setStroke(Paint.valueOf("#eae600"));
            initTimeCountService();
        } else {
            counterStatusCircle.setStroke(Paint.valueOf("limegreen"));
        }
    }

    public void initTimeCountService() {
        if (timeCountTask != null) {
            timeCountTask.cancel(false);
        }
        if (!busying) {
            return;
        }
        int playSpeedDivide = MainModel.getInstance().simulatorController.getPlaySpeedDivide();
        if (playSpeedDivide != 0) {
            int period = 1000000 / playSpeedDivide;
            timeCountTask = MainModel.getInstance().getThreadPoolExecutor().scheduleAtFixedRate(() -> {
                initTimeCountService();
                totalServedTime = totalServedTime.plusSeconds(1);
            }, period, period, TimeUnit.MICROSECONDS);
        }
    }

    public void initTooltipService(long period) {
        if (tooltipUpdateTask != null) {
            tooltipUpdateTask.cancel(false);
        }
        tooltipUpdateTask = MainModel.getInstance().getThreadPoolExecutor().scheduleAtFixedRate(() -> Platform.runLater(() -> {
            tooltip.setText("Checkout " + no + "\n\n" +
                    "status: " + (status ? "busying" : "idle") + "\n" +
                    "served customers: " + totalServedCustomers + "\n" +
                    "valid time: " + totalServedTime.getSecondOfDay() + "s\n" +
                    "type: " + (type == Checkout.CheckoutType.NORMAL ? "normal" : "expressway"));
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

    public boolean isBusying() {
        return busying;
    }

    public Circle getCounterStatusCircle() {
        return counterStatusCircle;
    }

    public DateTime getTotalServedTime() {
        return totalServedTime;
    }

    public int getTotalServedCustomers() {
        return totalServedCustomers;
    }

    public Map<Integer, Integer> getTotalServedProducts() {
        return totalServedProducts;
    }
}
