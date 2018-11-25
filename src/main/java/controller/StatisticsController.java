package controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import util.PropertiesTool;

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
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

    private Properties props = PropertiesTool.getProps();
    private ObservableList<PieChart.Data> waitTimeDistributionPieData;
    private Map<String, Integer> waitTimeDistributionPieMap;
    private ScheduledFuture<?> timeTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        waitTimeDistributionPie.setTitle("the distribution of wait time period");
        waitTimeDistributionPie.setLegendSide(Side.RIGHT);
        waitTimeDistributionPieData = waitTimeDistributionPie.getData();
        waitTimeDistributionPieMap = new HashMap<>();
        initWaitTimeDistributionPie();

        waitTimeEachCustomerScatter.setTitle("each customer wait time");
        waitTimeEachCustomerScatter.getXAxis().setLabel("customer(no)");
        waitTimeEachCustomerScatter.getYAxis().setLabel("wait time(s)");
        waitTimeEachCustomerScatter.setLegendSide(Side.RIGHT);

        utilizationEachCheckoutBar.setTitle("utilization of each checkout");
        utilizationEachCheckoutBar.getXAxis().setLabel("checkout(no)");
        utilizationEachCheckoutBar.getYAxis().setLabel("utilization(%)");
        utilizationEachCheckoutBar.setLegendSide(Side.RIGHT);
    }

    public void initStatistics() {
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
        initTimeTask();
    }

    private void initTimeTask() {
        timeTask = model.getThreadPoolExecutor().scheduleAtFixedRate(() -> {

        }, 0, 1000, TimeUnit.SECONDS);
    }

    public void initWaitTimeEachCustomerScatter(List<String> names) {
        waitTimeEachCustomerScatter.getData().addAll(names
                .stream().map(name -> {
                    XYChart.Series series = new XYChart.Series();
                    series.setName(name);
                    return series;
                }).collect(Collectors.toList()));
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
        series.getData().addAll(names
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
        waitTimeDistributionPieMap.put("<1min", 0);
        waitTimeDistributionPieMap.put("1-5min", 0);
        waitTimeDistributionPieMap.put("5-10min", 0);
        waitTimeDistributionPieMap.put("10-15min", 0);
        waitTimeDistributionPieMap.put("15-20min", 0);
        waitTimeDistributionPieMap.put(">20min", 0);
        waitTimeDistributionPieMap.put("leave", 0);
    }

    public void updateWaitTimeDistributionPieAddNewData(String key) {
        waitTimeDistributionPieMap.put(key, waitTimeDistributionPieMap.get(key) + 1);
        int total = waitTimeDistributionPieMap.values().stream().mapToInt(Integer::intValue).sum();
        for (PieChart.Data d : waitTimeDistributionPieData) {
            double v = 100.0 * waitTimeDistributionPieMap.get(d.getName()) / total;
            d.setPieValue(v);
        }
    }
}
