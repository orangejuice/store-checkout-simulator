package controller;

import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class StatisticsController extends Controller {
    public PieChart waitTimeDistributionPie;
    public ScatterChart waitTimeEachCustomerScatter;
    public BarChart utilizationEachCheckoutBar;

    private ObservableList<PieChart.Data> waitTimeDistributionPieData;
    private Map<String, Integer> waitTimeDistributionPieMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        waitTimeDistributionPie.setTitle("the distribution of wait time period");
        waitTimeDistributionPieData = waitTimeDistributionPie.getData();
        waitTimeDistributionPieMap = new HashMap<>();
        initWaitTimeDistributionPie();

        waitTimeEachCustomerScatter.setTitle("each customer wait time");
        initWaitTimeEachCustomerScatter();

        utilizationEachCheckoutBar.setTitle("utilization of each checkout");
    }

    public void initWaitTimeEachCustomerScatter() {

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
        synchronized (waitTimeDistributionPie) {
            waitTimeDistributionPieMap.put(key, waitTimeDistributionPieMap.get(key) + 1);
            int total = waitTimeDistributionPieMap.values().stream().mapToInt(Integer::intValue).sum();
            for (PieChart.Data d : waitTimeDistributionPieData) {
                double v = 100.0 * waitTimeDistributionPieMap.get(d.getName()) / total;
                d.setPieValue(v);
            }
        }

    }
}
