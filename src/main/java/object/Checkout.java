package object;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedList;
import java.util.Queue;


public class Checkout extends HBox {
    private Counter counter;

    private Queue<Customer> customers = new LinkedList<>();
    private int type;

    public Checkout(int no, int type) {
        setMinWidth(0);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.type = type;
        counter = new Counter(no, type, true);
        getChildren().add(counter);
    }

    public Checkout(int no) {
        this(no, CheckoutType.NORMAL);
    }

    public Queue<Customer> getCustomers() {
        return customers;
    }

    public Counter getCounter() {
        return counter;
    }

    public int getType() {
        return type;
    }

    public static class CheckoutType {
        public static final int NORMAL = 0;
        public static final int EXPRESSWAY = 1;
    }
}
