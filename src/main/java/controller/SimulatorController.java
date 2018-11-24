package controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import object.Checkout;
import object.Customer;
import org.joda.time.DateTime;
import org.kordamp.ikonli.javafx.FontIcon;
import util.PropertiesTool;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SimulatorController extends Controller {
    public ImageView shelf1;
    public ImageView entry;
    public ImageView shelf2;
    public ScrollPane background;
    public VBox market;
    public Button startButton;
    public Button shutButton;
    public Button resetButton;
    public Slider playSpeed;
    public Label playSpeedDesc;

    private List<Checkout> checkouts;
    private int playSpeedDivide;
    private boolean businessStatus;
    private boolean pauseStatus;
    private Properties props;
    private int customerNo;
    private DateTime simulateTime;
    private ScheduledExecutorService customerComingExecutorService;
    private ScheduledExecutorService timeCountService;
    private ScheduledFuture<?> customerComingTask;
    private ScheduledFuture<?> timeCountTask;

    //todo bug of "JavaFX Application Thread" java.lang.IndexOutOfBoundsException: Index -1 out of bounds for length 2
    //todo shield element operation in fxml is helpful.
    public void initialize(URL location, ResourceBundle resources) {
        setMarketBackgroundAutoFit();
        props = PropertiesTool.getProps();

        customerComingExecutorService = Executors.newSingleThreadScheduledExecutor();
        timeCountService = Executors.newSingleThreadScheduledExecutor();
    }

    public void initSimulator() {
        initBtns();
        initBtnsEvent();
        model.outputController.clearLogList();
        model.outputController.processBar.setProgress(0);

        businessStatus = false;
        pauseStatus = false;
        customerNo = 0;
        playSpeed.valueProperty().set(0);
        playSpeedDivide = 1;
        simulateTime = new DateTime().secondOfDay().setCopy(0);

        initCheckout();
        initCustomerComingService();
        initTimeCountService();
    }

    //todo forbidden while simulating
    private void initTimeCountService() {
        int period = 1000000 / playSpeedDivide;

        if (timeCountTask != null) {
            timeCountTask.cancel(false);
        }
        timeCountTask = timeCountService.scheduleAtFixedRate(() -> {
            if (businessStatus && !pauseStatus) {
                simulateTime = simulateTime.plusSeconds(1);
                model.outputController.processBar.setProgress(simulateTime.getSecondOfDay() / 3600.0);
            }
        }, period, period, TimeUnit.MICROSECONDS);
    }

    private void initBtns() {
        startButton.setDisable(false);
        shutButton.setDisable(true);
        resetButton.setDisable(true);

        startButton.setGraphic(new FontIcon("fas-play"));
        startButton.setText("open door");
        shutButton.setGraphic(new FontIcon("fas-stop"));
        shutButton.setText("stop");
        resetButton.setGraphic(new FontIcon("fas-reply"));
        resetButton.setText("start a new life");
    }

    private void initCheckout() {
        market.getChildren().removeIf(node -> node.getClass() != HBox.class);
        checkouts = new LinkedList<>();

        Integer quantityOfCheckout = Integer.valueOf(props.getProperty(model.preferenceController.prefQuantityOfCheckouts.getId()));
        int no = 0;
        for (int i = 0; i < quantityOfCheckout; no++, i++) {
            Checkout channel = new Checkout(no + 1, Checkout.CheckoutChannelType.NORMAL);
            addCheckout(channel);
        }
        Integer quantityOfExpresswayCheckout = Integer.valueOf(props.getProperty(model.preferenceController.prefQuantityOfExpresswayCheckouts.getId()));
        for (int i = 0; i < quantityOfExpresswayCheckout; no++, i++) {
            Checkout channel = new Checkout(no + 1, Checkout.CheckoutChannelType.EXPRESSWAY);
            addCheckout(channel);
        }

        log("[store] ready", Level.CONFIG);
        log("[store] equipped with " + quantityOfCheckout + " checkout", Level.CONFIG);
        if (quantityOfExpresswayCheckout > 0) {
            log("[store] equipped with " + quantityOfExpresswayCheckout + " expressway checkout", Level.CONFIG);
        }
    }

    private Checkout getBestChannel(boolean isExpresswayAccessible) {
        Integer min = checkouts.stream()
                .filter(c -> isExpresswayAccessible || (c.getType() == Checkout.CheckoutChannelType.NORMAL))
                .mapToInt(v -> v.getCustomers().size()).min().getAsInt();
        List<Checkout> chooseFromList = checkouts.stream()
                .filter(c -> isExpresswayAccessible || (c.getType() == Checkout.CheckoutChannelType.NORMAL))
                .filter(c -> c.getCustomers().size() == min)
                .collect(Collectors.toList());
        int num = ThreadLocalRandom.current().nextInt(0, chooseFromList.size());

        return chooseFromList.get(num);
    }

    private void initBtnsEvent() {
        startButton.setOnAction(actionEvent -> {
            if (!businessStatus) {
                businessStatus = true;
                startButton.setGraphic(new FontIcon("fas-pause"));
                startButton.setText("pause");
                resetButton.setDisable(true);
                shutButton.setDisable(false);
                resetButton.setDisable(true);
                log("[store] open door", Level.CONFIG);
                return;
            }
            if (pauseStatus) {
                // to continue
                startButton.setGraphic(new FontIcon("fas-pause"));
                startButton.setText("pause");
                log("[store] continue", Level.CONFIG);
            } else {
                startButton.setGraphic(new FontIcon("fas-play"));
                startButton.setText("continue");
                log("[store] pause", Level.CONFIG);
            }
            pauseStatus = !pauseStatus;
        });

        shutButton.setOnAction(actionEvent -> {
            if (businessStatus) {
                startButton.setDisable(true);
                startButton.setGraphic(new FontIcon("fas-play"));
                startButton.setText("Open door");
                shutButton.setDisable(true);
                resetButton.setDisable(false);
                log("[store] close door", Level.CONFIG);
            }
            businessStatus = !businessStatus;
        });

        resetButton.setOnAction(actionEvent -> {
            model.shellController.setStep(0);
            model.shellController.stepTabPane.getSelectionModel().select(model.shellController.preferencesTab);
        });

        playSpeed.valueProperty().addListener((observableValue, number, newValue) -> {
            if (newValue.doubleValue() == 0) {
                playSpeedDivide = 1;
                playSpeedDesc.setText("1x");
            } else if (newValue.doubleValue() == 25) {
                playSpeedDivide = 2;
                playSpeedDesc.setText("2x");
            } else if (newValue.doubleValue() == 50) {
                playSpeedDivide = 4;
                playSpeedDesc.setText("4x");
            } else if (newValue.doubleValue() == 75) {
                playSpeedDivide = 8;
                playSpeedDesc.setText("8x");
            } else if (newValue.doubleValue() == 100) {
                playSpeedDivide = 16;
                playSpeedDesc.setText("16x");
            } else {
                return;
            }
            initCustomerComingService();
            initTimeCountService();
            checkouts.forEach(checkout -> checkout.getCounter().initTimeCountService(playSpeedDivide));
        });
    }

    private void initCustomerComingService() {
        int busyDegree = Double.valueOf(props.getProperty(model.preferenceController.prefBusyDegree.getId())).intValue();
        int period;
        if (busyDegree == 0) {
            period = 12000000 / playSpeedDivide;
        } else if (busyDegree == 25) {
            period = 6000000 / playSpeedDivide;
        } else if (busyDegree == 50) {
            period = 3000000 / playSpeedDivide;
        } else if (busyDegree == 75) {
            period = 1500000 / playSpeedDivide;
        } else if (busyDegree == 100) {
            period = 750000 / playSpeedDivide;
        } else {
            period = 3000000 / playSpeedDivide;
        }
        if (customerComingTask != null) {
            customerComingTask.cancel(false);
        }
        customerComingTask = customerComingExecutorService.scheduleAtFixedRate(() -> {
            try {
                if (businessStatus && !pauseStatus) {
                    //quantity of goods
                    Integer quantityFrom = Integer.valueOf(props.getProperty(model.preferenceController.prefRangeOfGoodsQuantityPerCustomerFrom.getId()));
                    Integer quantityTo = Integer.valueOf(props.getProperty(model.preferenceController.prefRangeOfGoodsQuantityPerCustomerTo.getId()));
                    int quantity = ThreadLocalRandom.current().nextInt(quantityFrom, quantityTo + 1);

                    //temper
                    double temper = Math.random();
                    double temperDivide = 0;
                    try {
                        temperDivide = new DecimalFormat("0.0#%").parse(props.getProperty(model.preferenceController.prefPercentageOfACustomerWhoCantWait.getId())).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    boolean cannotWait = temper < temperDivide;
                    String minsText = props.getProperty(model.preferenceController.prefCustomerWillLeaveAfterWaitingFor.getId());
                    String mins = minsText.replaceAll("mins", "");
                    int waitSec = Integer.valueOf(mins) * 60;

                    Customer customer = new Customer(++customerNo, quantity, cannotWait, waitSec);
                    addCustomer(customer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, period, period, TimeUnit.MICROSECONDS);
    }

    private void addCustomer(Customer customer) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), customer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        //choose the best checkout
        Integer lessThan = Integer.valueOf(props.getProperty(model.preferenceController.prefExpresswayCheckoutsForProductsLessThan.getId()));
        Checkout bestChannel;

        bestChannel = getBestChannel(customer.getQuantityOfGoods() <= lessThan);

        Platform.runLater(() -> {
            log("[customer] [new] customer" + customerNo + " Goods:" + customer.getQuantityOfGoods() + ",temper:" +
                    (customer.isCannotWait() ? "Bad, leave after " + customer.getWaitSec() + "s" : "Good"), Level.FINE);
//            bestChannel.getChildren().add(customer);
            bestChannel.getCustomers().offer(customer);
        });

        new Thread(() -> {
            while (true) {
                if (!customer.isBeingServed()) {
                    try {
                        TimeUnit.MICROSECONDS.sleep(1000000 / playSpeedDivide);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int waitSecActual = customer.updateWaitSecActual(1);
                    if (customer.isCannotWait() && waitSecActual >= customer.getWaitSec()) {
                        Platform.runLater(() -> {
                            bestChannel.getCustomers().remove(customer);
//                            bestChannel.getChildren().remove(customer);
                            log("[customer] [leave] customer" + customerNo
                                    + " leaved after waiting for " + customer.getWaitSec() + "s", Level.WARNING);
                        });
                    }
                } else {
                    break;
                }
            }
        }).start();
    }

    private void addCheckout(Checkout channel) {
        market.getChildren().add(channel);
        checkouts.add(channel);

        new Thread(() -> {
            while (true) {
                try {
                    if (channel.getCustomers().size() > 0) {
                        channel.getCounter().setBusying(true, playSpeedDivide);
                        // offer poll/peek for queue
                        Customer nowCustomer = channel.getCustomers().peek();
                        nowCustomer.setBeingServed(true);

                        int total = nowCustomer.getQuantityOfGoods();
                        int waitFor = nowCustomer.getQuantityWaitForCheckout();

                        // scan goods
                        Double from = Double.valueOf(props.getProperty(model.preferenceController.prefRangeOfEachProductScanTimeFrom.getId()));
                        Double to = Double.valueOf(props.getProperty(model.preferenceController.prefRangeOfEachProductScanTimeTo.getId()));
                        double v = ThreadLocalRandom.current().nextDouble(from, to);
                        TimeUnit.MICROSECONDS.sleep((long) (v * 1000000) / playSpeedDivide);
                        nowCustomer.setQuantityWaitForCheckout(--waitFor);

                        // change the arc
                        //TODO arc centralize
                        nowCustomer.getArc().setLength(360.0 * waitFor / total);

                        // if 0, delete
                        if (waitFor == 0) {
                            channel.getCounter().updateTotalServed(1);
                            Platform.runLater(() -> {
//                                channel.getChildren().remove(nowCustomer);
                                channel.getCustomers().poll();
                                log("[checkout] " + channel.getCounter().getNo() + " served a customer", Level.INFO);
                            });
                        }
                    } else {
                        channel.getCounter().setBusying(false, 0);
                        TimeUnit.MICROSECONDS.sleep(1000000 / playSpeedDivide);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setMarketBackgroundAutoFit() {
        background.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            shelf1.setFitWidth(newValue.getWidth() * 100.0 / 381);
            entry.setFitWidth(newValue.getWidth() * 181.0 / 381);
            shelf2.setFitWidth(newValue.getWidth() * 100.0 / 381);
        });
    }

    private void log(String text, Level level) {
        model.outputController.addLog(text, level);
    }

    public DateTime getSimulateTime() {
        return simulateTime;
    }
}
