package controller;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import util.PropertiesTool;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferenceController extends Controller {
    public Slider prefBusyDegree;
    public ChoiceBox<String> prefRangeOfGoodsQuantityPerCustomerFrom;
    public ChoiceBox<String> prefRangeOfGoodsQuantityPerCustomerTo;
    public ChoiceBox<String> prefPercentageOfACustomerWhoCantWait;
    public ChoiceBox<String> prefCustomerWillLeaveAfterWaitingFor;
    public ChoiceBox<String> prefQuantityOfCheckouts;
    public ChoiceBox<String> prefQuantityOfExpresswayCheckouts;
    public ChoiceBox<String> prefExpresswayCheckoutsForProductsLessThan;
    public ChoiceBox<String> prefRangeOfEachProductScanTimeFrom;
    public ChoiceBox<String> prefRangeOfEachProductScanTimeTo;
    public Label busyDegreeDesc;
    public Button applyButton;
    public Button applyAndContinueButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initSettings();
        initBtnsEvent();
    }

    private void initBtnsEvent() {
        applyButton.setOnAction(actionEvent -> {
            saveSettings();
            model.shellController.setStep(1);
        });
        applyAndContinueButton.setOnAction(actionEvent -> {
            saveSettings();
            model.shellController.setStep(1);
            model.shellController.stepTabPane.getSelectionModel().select(model.shellController.simulationTab);
        });
    }

    private void saveSettings() {
        PropertiesTool.saveProperties(prefBusyDegree.getId(), String.valueOf(prefBusyDegree.getValue()));
        PropertiesTool.saveProperties(prefRangeOfGoodsQuantityPerCustomerFrom.getId(), prefRangeOfGoodsQuantityPerCustomerFrom.getValue());
        PropertiesTool.saveProperties(prefRangeOfGoodsQuantityPerCustomerTo.getId(), prefRangeOfGoodsQuantityPerCustomerTo.getValue());
        PropertiesTool.saveProperties(prefPercentageOfACustomerWhoCantWait.getId(), prefPercentageOfACustomerWhoCantWait.getValue());
        PropertiesTool.saveProperties(prefCustomerWillLeaveAfterWaitingFor.getId(), prefCustomerWillLeaveAfterWaitingFor.getValue());
        PropertiesTool.saveProperties(prefQuantityOfCheckouts.getId(), prefQuantityOfCheckouts.getValue());
        PropertiesTool.saveProperties(prefQuantityOfExpresswayCheckouts.getId(), prefQuantityOfExpresswayCheckouts.getValue());
        PropertiesTool.saveProperties(prefExpresswayCheckoutsForProductsLessThan.getId(), prefExpresswayCheckoutsForProductsLessThan.getValue());
        PropertiesTool.saveProperties(prefRangeOfEachProductScanTimeFrom.getId(), prefRangeOfEachProductScanTimeFrom.getValue());
        PropertiesTool.saveProperties(prefRangeOfEachProductScanTimeTo.getId(), prefRangeOfEachProductScanTimeTo.getValue());
    }

    private void initSettings() {
        prefBusyDegree.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 0) {
                busyDegreeDesc.setText("very idle");
            } else if (newValue.doubleValue() == 25) {
                busyDegreeDesc.setText("idle");
            } else if (newValue.doubleValue() == 50) {
                busyDegreeDesc.setText("normal");
            } else if (newValue.doubleValue() == 75) {
                busyDegreeDesc.setText("busy");
            } else if (newValue.doubleValue() == 100) {
                busyDegreeDesc.setText("very busy");
            }
        });
        prefBusyDegree.setValue(Double.valueOf(
                PropertiesTool.readProperties(prefBusyDegree.getId(), "50")
        ));

        for (int i = 1; i <= 200; i++) {
            prefRangeOfGoodsQuantityPerCustomerFrom.getItems().add(String.valueOf(i));
            prefRangeOfGoodsQuantityPerCustomerTo.getItems().add(String.valueOf(i));
        }
        prefRangeOfGoodsQuantityPerCustomerFrom.setValue(
                PropertiesTool.readProperties(prefRangeOfGoodsQuantityPerCustomerFrom.getId(), "1")
        );
        prefRangeOfGoodsQuantityPerCustomerTo.setValue(
                PropertiesTool.readProperties(prefRangeOfGoodsQuantityPerCustomerTo.getId(), "200")
        );

        prefPercentageOfACustomerWhoCantWait.getItems().addAll("0%", "10%", "20%", "30%",
                "40%", "50%", "60%", "70%", "80%", "90%", "100%");
        prefPercentageOfACustomerWhoCantWait.setValue(
                PropertiesTool.readProperties(prefPercentageOfACustomerWhoCantWait.getId(), "20%")
        );

        prefCustomerWillLeaveAfterWaitingFor.getItems().addAll("2mins", "5mins", "10mins");
        prefCustomerWillLeaveAfterWaitingFor.setValue(
                PropertiesTool.readProperties(prefCustomerWillLeaveAfterWaitingFor.getId(), "5mins")
        );

        prefQuantityOfCheckouts.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8");
        prefQuantityOfCheckouts.setValue(
                PropertiesTool.readProperties(prefQuantityOfCheckouts.getId(), "4")
        );

        prefQuantityOfExpresswayCheckouts.getItems().addAll("0", "1", "2", "3", "4");
        prefQuantityOfExpresswayCheckouts.setValue(
                PropertiesTool.readProperties(prefQuantityOfExpresswayCheckouts.getId(), "0")
        );

        prefExpresswayCheckoutsForProductsLessThan.getItems().addAll("5", "10", "20", "30");
        prefExpresswayCheckoutsForProductsLessThan.setValue(
                PropertiesTool.readProperties(prefExpresswayCheckoutsForProductsLessThan.getId(), "5")
        );

        prefRangeOfEachProductScanTimeFrom.getItems().addAll("0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5", "6");
        prefRangeOfEachProductScanTimeTo.getItems().addAll("0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5", "6");
        prefRangeOfEachProductScanTimeFrom.setValue(
                PropertiesTool.readProperties(prefRangeOfEachProductScanTimeFrom.getId(), "0.5")
        );
        prefRangeOfEachProductScanTimeTo.setValue(
                PropertiesTool.readProperties(prefRangeOfEachProductScanTimeTo.getId(), "1.5")
        );
    }

}
