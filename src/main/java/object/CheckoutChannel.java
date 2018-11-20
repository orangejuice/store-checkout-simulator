package object;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedList;
import java.util.Queue;


public class CheckoutChannel extends HBox {
    private Counter counter;

    private Queue<Customer> customers = new LinkedList<>();
    private int type;

    public CheckoutChannel(int no, int type) {
        setMinWidth(0);
        VBox.setVgrow(this, Priority.ALWAYS);

        counter = new Counter(no, type, true);
        getChildren().add(counter);
    }

    public CheckoutChannel(int no) {
        this(no, CheckoutChannelType.NORMAL);
    }

    public Queue<Customer> getCustomers() {
        return customers;
    }

    public Counter getCounter() {
        return counter;
    }

    public static class CheckoutChannelType {
        public static final int NORMAL = 0;
        public static final int EXPRESSWAY = 1;
    }
}
