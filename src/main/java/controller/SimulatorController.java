package controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import object.CheckoutChannel;
import object.Customer;
import util.PropertiesTool;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SimulatorController extends Controller {
    public ImageView shelf1;
    public ImageView entry;
    public ImageView shelf2;
    public HBox background;
    public VBox market;
    public Button startButton;
    public Button shutButton;
    public Button resetButton;
    public Slider playSpeed;
    public Label playSpeedDesc;

    private List<CheckoutChannel> checkoutChannels;
    private int playSpeedDivide;
    private boolean businessStatus;
    private boolean pauseStatus;
    private Properties props;
    private int customerNo;

    public void initialize(URL location, ResourceBundle resources) {
        setMarketBackgroundAutoFit();
        props = PropertiesTool.getProps();
    }

    public void initSimulator() {
        initBtnsEvent();
        model.outputController.clearLogList();

        startButton.setDisable(false);
        shutButton.setDisable(true);
        resetButton.setDisable(true);
        businessStatus = false;
        pauseStatus = false;
        customerNo = 0;
        playSpeed.valueProperty().set(0);
        playSpeedDivide = 1;
        checkoutChannels = new LinkedList<>();

        initCheckout();
        initCustomerComingExecutorService();
    }

    private void initCheckout() {
        market.getChildren().forEach(node -> {
            if (node.getClass() == Customer.class) {
                market.getChildren().remove(node);
            }
        });

        Integer quantityOfCheckout = Integer.valueOf(props.getProperty(model.preferenceController.prefQuantityOfCheckouts.getId()));
        int i = 0;
        for (int j = 0; j < quantityOfCheckout; i++, j++) {
            addCheckout(i + 1, CheckoutChannel.CheckoutChannelType.NORMAL);
        }
        Integer quantityOfExpresswayCheckout = Integer.valueOf(props.getProperty(model.preferenceController.prefQuantityOfExpresswayCheckouts.getId()));
        for (int j = 0; j < quantityOfExpresswayCheckout; i++, j++) {
            addCheckout(i + 1, CheckoutChannel.CheckoutChannelType.EXPRESSWAY);
        }

        log("[store] ready");
        log("[store] equipped with " + quantityOfCheckout + " checkout");
        if (quantityOfExpresswayCheckout > 0) {
            log("[store] equipped with " + quantityOfExpresswayCheckout + " expressway checkout");
        }
    }

    private CheckoutChannel getBestChannel(boolean isExpresswayAccessible) {
        Integer min = checkoutChannels.stream()
                .filter(c -> isExpresswayAccessible || (c.getType() == CheckoutChannel.CheckoutChannelType.NORMAL))
                .mapToInt(v -> v.getCustomers().size()).min().getAsInt();
        List<CheckoutChannel> chooseFromList = checkoutChannels.stream()
                .filter(c -> isExpresswayAccessible || (c.getType() == CheckoutChannel.CheckoutChannelType.NORMAL))
                .filter(c -> c.getCustomers().size() == min)
                .collect(Collectors.toList());
        int num = ThreadLocalRandom.current().nextInt(0, chooseFromList.size());

        return chooseFromList.get(num);
    }

    private void initBtnsEvent() {
        startButton.setOnAction(actionEvent -> {
            if (!businessStatus) {
                businessStatus = true;
                startButton.setText("▐ ▌ Pause");
                resetButton.setDisable(true);
                shutButton.setDisable(false);
                resetButton.setDisable(true);
                //model.outputController.clearLogList();
                log("[store] open door");
                return;
            }
            if (pauseStatus) {
                startButton.setText("▶ Open door");
                log("[store] pause");
            } else {
                startButton.setText("▐ ▌ Pause");
                //model.outputController.clearLogList();
                log("[store] continue");
            }
            pauseStatus = !pauseStatus;
        });

        shutButton.setOnAction(actionEvent -> {
            if (businessStatus) {
                log("[store] close door");
                startButton.setDisable(true);
                startButton.setText("▶ Open door");
                shutButton.setDisable(true);
                resetButton.setDisable(false);
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
            initCustomerComingExecutorService();
        });
    }

    private void initCustomerComingExecutorService() {
        //very idle 5 comes per min  / period 12s
        //idle 10 comes per min  / period 6s
        //normal 20 comes per min  / period 3s
        //busy 40 comes per min  / period 1.5s
        //very busy 80 comes per min  / period 0.75s
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
        ScheduledExecutorService customerComingExecutorService = Executors.newSingleThreadScheduledExecutor();
        customerComingExecutorService.scheduleAtFixedRate(() -> {
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

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), customer);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();

                    //choose the best checkout
                    Integer lessThan = Integer.valueOf(props.getProperty(model.preferenceController.prefExpresswayCheckoutsForProductsLessThan.getId()));
                    CheckoutChannel bestChannel;
                    if (quantity <= lessThan) {
                        bestChannel = getBestChannel(true);
                    } else {
                        bestChannel = getBestChannel(false);
                    }

                    Platform.runLater(() -> {
                        log("[customer] [new] customer" + customerNo + " Goods:" + quantity + ",temper:" +
                                (cannotWait ? "Bad, leave after " + minsText : "Good"));
                        bestChannel.getChildren().add(customer);
                        bestChannel.getCustomers().offer(customer);
                    });

                    if (cannotWait) {
                        new Thread(() -> {
                            while (true) {
                                if (!customer.isBeingServed()) {
                                    try {
                                        TimeUnit.MICROSECONDS.sleep(1000000 / playSpeedDivide);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    customer.setWaitSecRemaining(customer.getWaitSecRemaining() - 1);
                                    //System.out.println("Customer" + customer.getNo() + " leave remaining: " + customer.getWaitSecRemaining());
                                    if (customer.getWaitSecRemaining() == 0) {
                                        bestChannel.getChildren().remove(customer);
                                        bestChannel.getCustomers().remove(customer);
                                        log("[customer] [leave] customer" + customerNo
                                                + " leaved after waiting for " + customer.getWaitSec() + "s");
                                    }
                                } else {
                                    break;
                                }
                            }
                        }).start();
                    }
                } else {
                    TimeUnit.MICROSECONDS.sleep(1000000 / playSpeedDivide);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, period, period, TimeUnit.MICROSECONDS);
    }

    //todo bug of "JavaFX Application Thread" java.lang.IndexOutOfBoundsException: Index -1 out of bounds for length 2
    public void addCheckout(int no, int type) {
        CheckoutChannel channel = new CheckoutChannel(no, type);
        market.getChildren().add(channel);
        checkoutChannels.add(channel);

        new Thread(() -> {
            while (true) {
                try {
                    if (channel.getCustomers().size() > 0) {
                        channel.getCounter().getCounterStatusCircle().setStroke(Paint.valueOf("#eae600"));

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
                            Platform.runLater(() -> {
                                channel.getChildren().remove(nowCustomer);
                                channel.getCustomers().poll();
                                log("[checkout] " + channel.getCounter().getNo() + " served a customer");
                            });
                        }
                    } else {
                        channel.getCounter().getCounterStatusCircle().setStroke(Paint.valueOf("limegreen"));
                        TimeUnit.MICROSECONDS.sleep(1000000 / playSpeedDivide);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setMarketBackgroundAutoFit() {
        background.widthProperty().addListener((observable, oldValue, newValue) -> {
            shelf1.setFitWidth(newValue.doubleValue() * 100.0 / 381);
            entry.setFitWidth(newValue.doubleValue() * 181.0 / 381);
            shelf2.setFitWidth(newValue.doubleValue() * 100.0 / 381);
        });
    }

    public void log(String text) {
        model.outputController.addLog(text);
    }
}
