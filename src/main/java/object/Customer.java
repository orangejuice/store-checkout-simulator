package object;

import model.MainModel;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Customer {
    public Checkout parent;

    private int no;
    private int quantityOfGoods;
    private int quantityWaitForCheckout;
    private boolean isBeingServed;
    private boolean cannotWait;
    private int waitSec;
    private int waitSecActual;
    private ScheduledFuture<?> timeCountTask;

    public Customer(int no, int quantityOfGoods, boolean cannotWait, int waitSec, Checkout parent) {
        this.no = no;
        this.quantityOfGoods = quantityOfGoods;
        this.quantityWaitForCheckout = quantityOfGoods;
        this.cannotWait = cannotWait;
        this.waitSecActual = 0;
        this.waitSec = waitSec;
        this.isBeingServed = false;
        this.parent = parent;

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
                if (!MainModel.getInstance().pauseStatus) {
                    waitSecActual += 1;
                    if (cannotWait && (waitSecActual >= waitSec)) {
                        parent.leaveCustomer(this, false);
                    } else {
                        initTimeCountService();
                    }
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
