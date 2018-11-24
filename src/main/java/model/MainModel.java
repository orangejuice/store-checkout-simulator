package model;

import controller.OutputController;
import controller.PreferenceController;
import controller.ShellController;
import controller.SimulatorController;

public class MainModel {
    public OutputController outputController;
    public ShellController shellController;
    public PreferenceController preferenceController;
    public SimulatorController simulatorController;

    private static MainModel model = new MainModel();

    public static MainModel getInstance() {
        return model;
    }
}
