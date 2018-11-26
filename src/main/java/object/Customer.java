package object;

import model.MainModel;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Customer {
    private int no;
    private int quantityOfGoods;
    private int quantityWaitForCheckout;
    private boolean isBeingServed;
    private boolean cannotWait;
    private int waitSec;
    private int waitSecActual;
    public Checkout parent;
    private ScheduledFuture<?> timeCountTask;

    public Customer(int no, int quantityOfGoods, boolean cannotWait, int waitSec) {
        this.no = no;
        this.quantityOfGoods = quantityOfGoods;
        this.quantityWaitForCheckout = quantityOfGoods;
        this.cannotWait = cannotWait;
        this.waitSecActual = 0;
        this.waitSec = waitSec;
        this.isBeingServed = false;

        initTimeCountService();
    }

    public void initTimeCountService() {
        if (timeCountTask != null) {
            timeCountTask.cancel(false);
        }
        if (isBeingServed) {
            return;
        }
        int playSpeedDivide = MainModel.getInstance().simulatorController.getPlaySpeedDivide();
        if (playSpeedDivide != 0) {
            int period = 1000000 / playSpeedDivide;
            timeCountTask = MainModel.getInstance().getThreadPoolExecutor().scheduleAtFixedRate(() -> {
                waitSecActual += 1;

                if (cannotWait && (waitSecActual >= waitSec)) {
                    MainModel.getInstance().leftCustomers.add(this);
                    MainModel.getInstance().outputController.customerLeaveEvent(this);
                    parent.getCustomers().remove(this);
                    //todo Platform.runLater(() -> ((Checkout) getParent()).getChildren().remove(this));
                    this.leave();
                } else {
                    initTimeCountService();
                }
            }, period, period, TimeUnit.MICROSECONDS);
        }
    }

    public void leave() {
        timeCountTask.cancel(false);
    }

    public int getNo() {
        return no;
    }

    public int getQuantityOfGoods() {
        return quantityOfGoods;
    }

    public int getQuantityWaitForCheckout() {
        return quantityWaitForCheckout;
    }

    public void setQuantityWaitForCheckout(int quantityWaitForCheckout) {
        this.quantityWaitForCheckout = quantityWaitForCheckout;
    }

    public boolean isCannotWait() {
        return cannotWait;
    }

    public int getWaitSec() {
        return waitSec;
    }

    public boolean isBeingServed() {
        return isBeingServed;
    }

    public int getWaitSecActual() {
        return waitSecActual;
    }

    public void setBeingServed(boolean beingServed) {
        isBeingServed = beingServed;
    }
}
