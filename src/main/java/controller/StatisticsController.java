package controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import object.Customer;
import object.TextAreaExpandable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import util.PropertiesTool;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StatisticsController extends Controller {
    public PieChart waitTimeDistributionPie;
    public ScatterChart waitTimeEachCustomerScatter;
    public BarChart utilizationEachCheckoutBar;
    public ScrollPane ReportPane;
    public TextField extendOfBusy;
    public TextField rangeOfGoodsQuantity;
    public TextField percentageOfACustomerCanNotWait;
    public TextField CustomerWillLeaveAfter;
    public TextField quantityOfNormalCheckouts;
    public TextField quantityOfExpresswayCheckouts;
    public TextField ExpresswayCheckoutsFor;
    public TextField rangeOfScanTime;
    public TextField date;
    public LineChart totalProductionProcessedLine;
    public TextAreaExpandable recordDetail;
    public VBox VBox;

    private Properties props = PropertiesTool.getProps();
    private ObservableList<PieChart.Data> waitTimeDistributionPieData;
    private Map<String, Integer> waitTimeDistributionMap;
    private ScheduledFuture<?> timeTask;
    private int hasProcessedCustomers;
    private Map<String, Integer> hasProcessedProductsMinute;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        waitTimeDistributionPie.setTitle("the distribution of wait time period");
        waitTimeDistributionPie.setLegendSide(Side.RIGHT);
        waitTimeDistributionPieData = waitTimeDistributionPie.getData();
        waitTimeDistributionMap = new HashMap<>();

        waitTimeEachCustomerScatter.setTitle("each customer wait time");
        waitTimeEachCustomerScatter.getXAxis().setLabel("customer(no)");
        waitTimeEachCustomerScatter.getYAxis().setLabel("wait time(s)");
        waitTimeEachCustomerScatter.setLegendSide(Side.RIGHT);

        utilizationEachCheckoutBar.setTitle("utilization of each checkout");
        utilizationEachCheckoutBar.getXAxis().setLabel("checkout(no)");
        utilizationEachCheckoutBar.getYAxis().setLabel("utilization(%)");
        utilizationEachCheckoutBar.setLegendSide(Side.RIGHT);

        totalProductionProcessedLine.setTitle("products processed per minute");
        totalProductionProcessedLine.getXAxis().setLabel("minute");
        totalProductionProcessedLine.getYAxis().setLabel("products(quantity)");
        totalProductionProcessedLine.setLegendSide(Side.RIGHT);

        recordDetail = new TextAreaExpandable();
        VBox.getChildren().add(recordDetail);
    }

    public void initStatistics(List<String> names) {
        String extend = props.getProperty(model.preferenceController.prefBusyDegree.getId());
        String leaveAfter = props.getProperty(model.preferenceController.prefCustomerWillLeaveAfterWaitingFor.getId());
        String expresswayForLessThan = props.getProperty(model.preferenceController.prefExpresswayCheckoutsForProductsLessThan.getId());
        String cantWait = props.getProperty(model.preferenceController.prefPercentageOfACustomerWhoCantWait.getId());
        String checkouts = props.getProperty(model.preferenceController.prefQuantityOfCheckouts.getId());
        String expresswayCheckouts = props.getProperty(model.preferenceController.prefQuantityOfExpresswayCheckouts.getId());
        String scanFrom = props.getProperty(model.preferenceController.prefRangeOfEachProductScanTimeFrom.getId());
        String scanTo = props.getProperty(model.preferenceController.prefRangeOfEachProductScanTimeTo.getId());
        String goodsFrom = props.getProperty(model.preferenceController.prefRangeOfGoodsQuantityPerCustomerFrom.getId());
        String goodsTo = props.getProperty(model.preferenceController.prefRangeOfGoodsQuantityPerCustomerTo.getId());
        extendOfBusy.setText(MessageFormat.format("extend of busy: {0}", extend));
        rangeOfGoodsQuantity.setText(MessageFormat.format("range of goods quantity: from {0} to {1}", goodsFrom, goodsTo));
        percentageOfACustomerCanNotWait.setText(MessageFormat.format("percentage of a customer who can not wait: {0}", cantWait));
        CustomerWillLeaveAfter.setText(MessageFormat.format("customer will leave after waiting for: {0}", leaveAfter));
        quantityOfNormalCheckouts.setText(MessageFormat.format("quantity of normal checkouts: {0}", checkouts));
        quantityOfExpresswayCheckouts.setText(MessageFormat.format("quantity of normal expressway: {0}", expresswayCheckouts));
        ExpresswayCheckoutsFor.setText(MessageFormat.format("expressway checkouts for products less than {0}", expresswayForLessThan));
        rangeOfScanTime.setText(MessageFormat.format("range of each product scan time: from {0} to {1}", scanFrom, scanTo));
        date.setText("date: " + DateTimeFormat.forPattern("EEEE, dd MMMM yyyy (ZZZ)").print(new DateTime()));
        ReportPane.setVvalue(0.3);
        initWaitTimeDistributionPie();
        initWaitTimeEachCustomerScatter(names);
        initUtilizationEachCheckoutBar(names);
        initTotalProductionProcessedLine(names);

        hasProcessedCustomers = 0;
        hasProcessedProductsMinute = names.stream().collect(Collectors.toMap(s -> s, s -> 1));
        recordDetail.setText("available after simulation finished...");
    }

    public void initStatisticsTask() {
        if (timeTask != null) {
            timeTask.cancel(false);
        }
        date.setText(date.getText() + "   start: " + DateTimeFormat.forPattern("HH:mm:ss").print(new DateTime()));
        timeTask = model.getThreadPoolExecutor().scheduleAtFixedRate(() -> {
            String waitSecPeriod;
            for (int i = hasProcessedCustomers; i < model.leftCustomers.size(); i++) {
                Customer customer = model.leftCustomers.get(i);
                if (customer.isCannotWait() && (customer.getWaitSecActual() >= customer.getWaitSec())) {
                    waitSecPeriod = "leave";
                } else if (customer.getWaitSecActual() <= 60) {
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
                updateWaitTimeDistributionPie(waitSecPeriod);
                updateWaitTimeEachCustomerScatter(customer.parent.getCounter().getNo(), customer.getNo(), customer.getWaitSecActual());
            }
            model.checkouts.forEach(checkout -> {
                double v = 100.0 * checkout.getCounter().getTotalServedTime().getSecondOfDay() / model.simulatorController.getSimulateTime().getSecondOfDay();
                updateUtilizationEachCheckoutBar(checkout.getCounter().getNo(), v);

                Map<Integer, Integer> totalServed = checkout.getCounter().getTotalServedProducts();
                int minute = model.simulatorController.getSimulateTime().getMinuteOfDay() + 1;

                Integer lastTimeMinute = hasProcessedProductsMinute.get("checkout" + checkout.getCounter().getNo());

                if (minute != lastTimeMinute) {
                    updateTotalProductionProcessedLine(checkout.getCounter().getNo(), minute - 1, totalServed.getOrDefault(minute - 1, 0));
                    hasProcessedProductsMinute.put("checkout" + checkout.getCounter().getNo(), minute);
                } else {
                    updateTotalProductionProcessedLine(checkout.getCounter().getNo(), minute, totalServed.getOrDefault(minute, 0));
                }
            });
            hasProcessedCustomers = model.leftCustomers.size();
        }, 0, 1, TimeUnit.SECONDS);
        model.getThreadPoolExecutor().execute(() -> {
            try {
                timeTask.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void cancelStatisticsTask() {
        if (timeTask != null) {
            timeTask.cancel(false);
        }
        date.setText(date.getText() + "   end: " + DateTimeFormat.forPattern("HH:mm:ss").print(new DateTime()));
        StringBuilder detail = new StringBuilder("total\n" +
                "customers: " + model.leftCustomers.size() + "\n" +
                "wait for less than 1min: " + waitTimeDistributionMap.get("<1min") + "\t\t\t" +
                "wait for 1-5mins: " + waitTimeDistributionMap.get("<1min") + "\t\t\t\t" +
                "wait for 5-10mins: " + waitTimeDistributionMap.get("5-10min") + "\n" +
                "wait for 10-15mins: " + waitTimeDistributionMap.get("10-15min") + "\t\t\t\t" +
                "wait for 15-20mins: " + waitTimeDistributionMap.get("15-20min") + "\t\t\t" +
                "wait for more than 20mins: " + waitTimeDistributionMap.get(">20min") + "\n" +
                "lost customers: " + waitTimeDistributionMap.get("leave") + "\t\t\t\t\t" +
                "products sold: " + model.checkouts.stream().mapToInt(value -> value.getCounter().getTotalServedProducts().values().stream().mapToInt(Integer::intValue).sum()).sum() + "\n");
        DecimalFormat format = new DecimalFormat("#0.00");
        detail.append("\n\naverage\n" +
                "customer wait time: " + format.format(model.leftCustomers.stream().mapToInt(Customer::getWaitSecActual).average().orElse(0)) + "\t\t\t\t" +
                "customer wait time(except left ones): " + format.format(model.leftCustomers.stream().filter(customer -> !(customer.isCannotWait() && (customer.getWaitSecActual() >= customer.getWaitSec()))).mapToInt(Customer::getWaitSecActual).average().orElse(0)) + "\n" +
                "checkout utilization: " + format.format(model.checkouts.stream().mapToDouble(value -> 100.0 * value.getCounter().getTotalServedTime().getSecondOfDay() / model.simulatorController.getSimulateTime().getSecondOfDay()).average().orElse(0)) + "%\t\t\t" +
                "products per trolley: " + format.format(model.leftCustomers.stream().mapToInt(Customer::getQuantityOfGoods).average().orElse(0)) + "\n");
        model.checkouts.stream().forEach(checkout -> {
            detail.append("checkout" + checkout.getCounter().getNo() + ": ");
            double v = 100.0 * checkout.getCounter().getTotalServedTime().getSecondOfDay() / model.simulatorController.getSimulateTime().getSecondOfDay();
            detail.append(format.format(v) + "%\n");
        });
        recordDetail.setText(detail.toString());
    }

    public void initWaitTimeEachCustomerScatter(List<String> names) {
        waitTimeEachCustomerScatter.getData().setAll(names
                .stream().map(name -> {
                    XYChart.Series series = new XYChart.Series();
                    series.setName(name);
                    series.getData().add(new XYChart.Data<>(0, 0));
                    return series;
                }).collect(Collectors.toList()));
        waitTimeEachCustomerScatter.getData().forEach(o -> ((XYChart.Series) o).getData().clear());
    }

    public void updateWaitTimeEachCustomerScatter(int checkoutNo, int customerNo, int sec) {
        waitTimeEachCustomerScatter.getData().stream()
                .filter(o -> ((XYChart.Series) o).getName().equals("checkout" + checkoutNo))
                .findFirst().ifPresent(o -> Platform.runLater(() -> {
            ((XYChart.Series) o).getData().add(new XYChart.Data<>(customerNo, sec));
        }));
    }

    public void initUtilizationEachCheckoutBar(List<String> names) {
        XYChart.Series series = new XYChart.Series();
        series.setName("utilization");
        series.getData().setAll(names
                .stream().map(name -> {
                    XYChart.Data<String, Double> data = new XYChart.Data<>();
                    data.setXValue(name);
                    data.setYValue(0.0);
                    return data;
                }).collect(Collectors.toList()));
        utilizationEachCheckoutBar.getData().add(series);
    }

    public void updateUtilizationEachCheckoutBar(int checkoutNo, double percent) {
        utilizationEachCheckoutBar.getData().stream()
                .findFirst().ifPresent(o -> Platform.runLater(() -> {
            ((XYChart.Data) ((XYChart.Series) o).getData().stream().filter(o1 -> ((XYChart.Data) o1).getXValue().equals("checkout" + checkoutNo)).findFirst().get()).setYValue(percent);
        }));
    }

    public void initWaitTimeDistributionPie() {
        waitTimeDistributionPieData.setAll(
                new PieChart.Data("<1min", 100),
                new PieChart.Data("1-5min", 0),
                new PieChart.Data("5-10min", 0),
                new PieChart.Data("10-15min", 0),
                new PieChart.Data("15-20min", 0),
                new PieChart.Data(">20min", 0),
                new PieChart.Data("leave", 0));
        waitTimeDistributionMap.put("<1min", 0);
        waitTimeDistributionMap.put("1-5min", 0);
        waitTimeDistributionMap.put("5-10min", 0);
        waitTimeDistributionMap.put("10-15min", 0);
        waitTimeDistributionMap.put("15-20min", 0);
        waitTimeDistributionMap.put(">20min", 0);
        waitTimeDistributionMap.put("leave", 0);
    }

    public void updateWaitTimeDistributionPie(String key) {
        waitTimeDistributionMap.put(key, waitTimeDistributionMap.get(key) + 1);
        int total = waitTimeDistributionMap.values().stream().mapToInt(Integer::intValue).sum();
        for (PieChart.Data d : waitTimeDistributionPieData) {
            double v = 100.0 * waitTimeDistributionMap.get(d.getName()) / total;
            Platform.runLater(() -> d.setPieValue(v));
        }
    }

    public void initTotalProductionProcessedLine(List<String> names) {
        totalProductionProcessedLine.getData().setAll(names
                .stream().map(name -> {
                    XYChart.Series series = new XYChart.Series();
                    series.setName(name);
                    return series;
                }).collect(Collectors.toList()));
    }

    public void updateTotalProductionProcessedLine(int checkoutNo, int minute, int quantity) {
        totalProductionProcessedLine.getData().stream()
                .filter(series -> ((XYChart.Series) series).getName().equals("checkout" + checkoutNo))
                .findFirst().ifPresent(series -> ((XYChart.Series) series).getData()
                .stream().filter(data -> (Integer) ((XYChart.Data) data).getXValue() == minute)
                .findFirst().ifPresentOrElse(data -> Platform.runLater(() -> {
                    ((XYChart.Data) data).setYValue(quantity);
                }), () -> Platform.runLater(() -> {
                    ((XYChart.Series) series).getData().add(new XYChart.Data<>(minute, quantity));
                })))
        ;
    }
}
