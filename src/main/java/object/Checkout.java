package object;

import controller.OutputController.LeaveEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.MainModel;

import java.util.LinkedList;
import java.util.Queue;


public class Checkout extends HBox {
    private Counter counter;
    private CustomerQueue customerQueue;
    private Queue<Customer> customers = new LinkedList<>();
    private int type;

    public Checkout(int no, int type) {
        setMinWidth(580);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.type = type;
        counter = new Counter(no, type, true);
        customerQueue = new CustomerQueue();
        getChildren().addAll(counter, customerQueue);
    }

    public Checkout(int no) {
        this(no, CheckoutType.NORMAL);
    }

    public Counter getCounter() {
        return counter;
    }

    public int getType() {
        return type;
    }

    public void addCustomer(Customer customer) {
        synchronized (this) {
            MainModel.getInstance().outputController.customerComeEvent(customer);
            if (this.customers.size() > 20) {
                MainModel.getInstance().leftCustomers.add(customer);
                MainModel.getInstance().outputController.customerLeaveEvent(customer, LeaveEvent.TOO_LONG_QUEUE);
                return;
            }
            customers.offer(customer);
            customerQueue.updateQuantity(customers.size());
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
                MainModel.getInstance().outputController.customerLeaveEvent(customer, LeaveEvent.WAIT_TIME);
            }
            customers.remove(customer);
            customerQueue.updateQuantity(customers.size());
        }
    }

    public Queue<Customer> getCustomers() {
        return customers;
    }

    public CustomerQueue getCustomerQueue() {
        return customerQueue;
    }

    public static class CheckoutType {
        public static final int NORMAL = 0;
        public static final int EXPRESSWAY = 1;
    }
}
