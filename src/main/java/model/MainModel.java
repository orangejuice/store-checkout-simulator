package model;

import controller.*;
import object.Checkout;
import object.Customer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MainModel {
    public OutputController outputController;
    public ShellController shellController;
    public PreferenceController preferenceController;
    public SimulatorController simulatorController;
    public StatisticsController statisticsController;

    private static ScheduledThreadPoolExecutor threadPoolExecutor;
    private static MainModel model = new MainModel();
    public List<Checkout> checkouts = new LinkedList<>();
    public List<Customer> leftCustomers = new LinkedList<>();
    public boolean pauseStatus;

    static {
        threadPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(100);
        threadPoolExecutor.setRemoveOnCancelPolicy(true);
    }

    public ScheduledThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public static MainModel getInstance() {
        return model;
    }
}
