package controller;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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

    private int playSpeedDivide;
    private boolean businessStatus;
    private boolean pauseStatus;
    private Properties props;
    private int customerNo;
    private DateTime simulateTime;
    private ScheduledFuture<?> customerComingTask;
    private ScheduledFuture<?> timeCountTask;
    private ScheduledFuture<?> finishSimulationTask;

    //todo bug of "JavaFX Application Thread" java.lang.IndexOutOfBoundsException: Index -1 out of bounds for length 2
    //todo shield element operation in fxml is helpful.
    public void initialize(URL location, ResourceBundle resources) {
        setMarketBackgroundAutoFit();
        props = PropertiesTool.getProps();
    }

    public void initSimulator() {
        initBtns();
        initBtnsEvent();

        businessStatus = false;
        pauseStatus = false;
        customerNo = 0;
        playSpeed.valueProperty().set(0);
        playSpeedDivide = 1;
        simulateTime = new DateTime().secondOfDay().setCopy(0);
        model.checkouts = new LinkedList<>();
        model.leftCustomers = new LinkedList<>();

        initCheckout();
        initCustomerComingService();
        initTimeCountService();
        if (finishSimulationTask != null) {
            finishSimulationTask.cancel(false);
        }
    }

    private void initTimeCountService() {
        int period = 1000000 / playSpeedDivide;

        if (timeCountTask != null) {
            timeCountTask.cancel(false);
        }
        timeCountTask = model.getThreadPoolExecutor().scheduleAtFixedRate(() -> {
            if (businessStatus && !pauseStatus) {
                simulateTime = simulateTime.plusSeconds(1);
                if (simulateTime.getHourOfDay() >= 1) {
                    Platform.runLater(this::finishSimulation);
                }
                model.outputController.processBar.setProgress(simulateTime.getSecondOfDay() / 3600.0);
            }
        }, period, period, TimeUnit.MICROSECONDS);
    }

    private void initBtns() {
        startButton.setDisable(false);
        shutButton.setDisable(true);
        resetButton.setDisable(false);

        startButton.setGraphic(new FontIcon("fas-play"));
        startButton.setText("open door");
        shutButton.setGraphic(new FontIcon("fas-stop"));
        shutButton.setText("stop");
        resetButton.setGraphic(new FontIcon("fas-reply"));
        resetButton.setText("start a new life");
    }

    private void initCheckout() {
        market.getChildren().removeIf(node -> node.getClass() != HBox.class);

        Integer quantityOfCheckout = Integer.valueOf(props.getProperty(model.preferenceController.prefQuantityOfCheckouts.getId()));
        int no = 0;
        for (int i = 0; i < quantityOfCheckout; no++, i++) {
            Checkout channel = new Checkout(no + 1, Checkout.CheckoutType.NORMAL);
            market.getChildren().add(channel);
            model.checkouts.add(channel);
        }
        Integer quantityOfExpresswayCheckout = Integer.valueOf(props.getProperty(model.preferenceController.prefQuantityOfExpresswayCheckouts.getId()));
        for (int i = 0; i < quantityOfExpresswayCheckout; no++, i++) {
            Checkout channel = new Checkout(no + 1, Checkout.CheckoutType.EXPRESSWAY);
            market.getChildren().add(channel);
            model.checkouts.add(channel);
        }
        model.outputController.storeInitEvent();
    }

    private Checkout getBestCheckout(boolean isExpresswayAccessible) {
        Integer min = model.checkouts.stream()
                .filter(c -> isExpresswayAccessible || (c.getType() == Checkout.CheckoutType.NORMAL))
                .mapToInt(v -> v.getCustomers().size()).min().getAsInt();
        List<Checkout> chooseFromList = model.checkouts.stream()
                .filter(c -> isExpresswayAccessible || (c.getType() == Checkout.CheckoutType.NORMAL))
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
                shutButton.setDisable(false);
                resetButton.setDisable(true);
                model.outputController.addLog("[store] open door", Level.CONFIG);
                model.statisticsController.initStatisticsTask();
                return;
            }
            if (pauseStatus) {
                // to continue
                startButton.setGraphic(new FontIcon("fas-pause"));
                startButton.setText("pause");
                model.outputController.addLog("[store] continue", Level.CONFIG);
            } else {
                startButton.setGraphic(new FontIcon("fas-play"));
                startButton.setText("continue");
                model.outputController.addLog("[store] pause", Level.CONFIG);
            }
            pauseStatus = !pauseStatus;
        });

        shutButton.setOnAction(actionEvent -> {
            if (businessStatus) {
                finishSimulation();
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
            initCustomerComingService();
            initTimeCountService();
        });
    }

    private void finishSimulation() {
        startButton.setDisable(true);
        startButton.setGraphic(new FontIcon("fas-play"));
        startButton.setText("Open door");
        shutButton.setDisable(true);
        resetButton.setDisable(false);
        model.outputController.addLog("[store] close entry, no new customers from now.", Level.CONFIG);
        model.outputController.addLog("[store] for whole report please wait util all customers have left!", Level.CONFIG);
        finishSimulationTask = model.getThreadPoolExecutor().scheduleAtFixedRate(() -> {
            if (model.checkouts.stream().noneMatch(checkout -> checkout.getCounter().isBusying())) {
                finishSimulationTask.cancel(false);
                model.statisticsController.cancelStatisticsTask();
                model.outputController.addLog("[store] all customers have gone.", Level.CONFIG);
                model.outputController.addLog("[store] door closed.", Level.CONFIG);
                model.outputController.addLog("[store] simulation finish. Thank you.", Level.CONFIG);
            }
        }, 0, 1, TimeUnit.SECONDS);
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
        customerComingTask = model.getThreadPoolExecutor().scheduleAtFixedRate(() -> {
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
        //choose the best checkout
        Integer lessThan = Integer.valueOf(props.getProperty(model.preferenceController.prefExpresswayCheckoutsForProductsLessThan.getId()));
        Checkout bestCheckout;

        bestCheckout = getBestCheckout(customer.getQuantityOfGoods() <= lessThan);
        customer.parent = bestCheckout;

        model.outputController.customerComeEvent(customer);
        bestCheckout.getCustomers().offer(customer);
        //todo Platform.runLater(() -> bestCheckout.getChildren().add(customer));
    }

    private void setMarketBackgroundAutoFit() {
        background.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            shelf1.setFitWidth(newValue.getWidth() * 100.0 / 381);
            entry.setFitWidth(newValue.getWidth() * 181.0 / 381);
            shelf2.setFitWidth(newValue.getWidth() * 100.0 / 381);
        });
    }

    public int getPlaySpeedDivide() {
        return playSpeedDivide;
    }

    public DateTime getSimulateTime() {
        return simulateTime;
    }
}
