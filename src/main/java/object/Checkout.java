package object;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.MainModel;

import java.util.LinkedList;
import java.util.Queue;


public class Checkout extends HBox {
    private Counter counter;
    private Label moreLabel;

    private Queue<Customer> customers = new LinkedList<>();
    private Queue<Customer> waitingCustomers = new LinkedList<>();
    private boolean more;
    private int type;

    public Checkout(int no, int type) {
        setMinWidth(0);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.type = type;
        counter = new Counter(no, type, true);

        moreLabel = new Label("...");
        moreLabel.setVisible(false);
        moreLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold");

        getChildren().addAll(counter, moreLabel);

        setAlignment(Pos.BOTTOM_LEFT);
    }

    public Checkout(int no) {
        this(no, CheckoutType.NORMAL);
    }

    public Queue<Customer> getCustomers() {
        return customers;
    }

    public Queue<Customer> getWaitingCustomers() {
        return waitingCustomers;
    }

    public Counter getCounter() {
        return counter;
    }

    public int getType() {
        return type;
    }

    public void addCustomer(Customer customer) {
        synchronized (this) {
            if (more) {
                waitingCustomers.offer(customer);
            } else if (getChildren().size() >= 5) {
                waitingCustomers.offer(customer);
                Platform.runLater(() -> moreLabel.setVisible(true));
                more = !more;
            } else {
                customers.offer(customer);
                Platform.runLater(() -> getChildren().add(getChildren().size() - 1, customer));
            }
            MainModel.getInstance().outputController.customerComeEvent(customer);
        }
    }

    public void leaveCustomer(Customer customer, boolean isAfterCheckout) {
        synchronized (this) {
            customer.leave();
            if (isAfterCheckout) {
                MainModel.getInstance().leftCustomers.add(customer);
                MainModel.getInstance().outputController.customerCheckoutEvent(this, customer);
            } else {
                MainModel.getInstance().leftCustomers.add(customer);
                MainModel.getInstance().outputController.customerLeaveEvent(customer);
                waitingCustomers.remove(customer);
                customers.remove(customer);
            }
            Platform.runLater(() -> getChildren().remove(customer));

            if (getChildren().size() <= 5) {
                if (waitingCustomers.size() > 0) {
                    Customer poll = waitingCustomers.poll();
                    customers.offer(poll);
                    Platform.runLater(() -> getChildren().add(getChildren().size() - 1, poll));
                }
                if (waitingCustomers.size() == 0) {
                    Platform.runLater(() -> moreLabel.setVisible(false));
                }
            }
        }
    }

    public static class CheckoutType {
        public static final int NORMAL = 0;
        public static final int EXPRESSWAY = 1;
    }
}
