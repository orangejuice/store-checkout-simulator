package model;

import controller.*;

public class MainModel {
    public OutputController outputController;
    public ShellController shellController;
    public PreferenceController preferenceController;
    public SimulatorController simulatorController;
    public StatisticsController statisticsController;

    private static MainModel model = new MainModel();

    public static MainModel getInstance() {
        return model;
    }
}
