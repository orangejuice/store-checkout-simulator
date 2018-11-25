package controller;

import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;

import java.net.URL;
import java.util.ResourceBundle;

public class StatisticsController extends Controller {
    public PieChart waitTimeDistributionPie;
    public ScatterChart waitTimeEachCustomerScatter;
    public BarChart utilizationEachCheckoutBar;

    private ObservableList<PieChart.Data> waitTimeDistributionPieData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        waitTimeDistributionPie.setTitle("the distribution of wait time period");
        waitTimeDistributionPieData = waitTimeDistributionPie.getData();
        initWaitTimeDistributionPie();

        waitTimeEachCustomerScatter.setTitle("each customer wait time");
        utilizationEachCheckoutBar.setTitle("utilization of each checkout");
    }

    public void initWaitTimeDistributionPie() {
        waitTimeDistributionPieData.setAll(new PieChart.Data("less than 1min", 100),
                new PieChart.Data("1-5min", 0),
                new PieChart.Data("5-10min", 0),
                new PieChart.Data("10-15min", 0),
                new PieChart.Data("15-20min", 0),
                new PieChart.Data("20-25min", 0),
                new PieChart.Data("25-30min", 0),
                new PieChart.Data("leave", 0));
    }
}
