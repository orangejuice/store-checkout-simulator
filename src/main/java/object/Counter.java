package object;

import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Counter extends StackPane {
    private ImageView counterImageView;
    private Circle counterStatusCircle;

    private int no;
    private boolean status;
    private int type;

    public Counter(int no, int type, boolean status) {
        this.no = no;
        this.status = status;
        this.type = type;

        counterImageView = new ImageView();
        counterImageView.setFitHeight(150);
        counterImageView.setFitWidth(200);
        counterImageView.setPickOnBounds(true);
        counterImageView.setPreserveRatio(true);
        Image image = new Image(getClass().getResourceAsStream("/image/counter.png"));
        counterImageView.setImage(image);

        counterImageView.setOnMouseEntered(mouseEvent -> {
            Tooltip tooltip = new Tooltip("Checkout " + no + "\n\n" +
                    "open:\t\t" + status + "\n" +
                    "type:\t\t" + (type == 0 ? "normal" : "expressway"));
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setStyle("-fx-font-weight: bold");
            Tooltip.install(counterImageView, tooltip);
        });

        counterStatusCircle = new Circle();
        counterStatusCircle.setStroke(Paint.valueOf("limegreen"));
        counterStatusCircle.setStrokeWidth(25.0);
        StackPane.setAlignment(counterStatusCircle, Pos.TOP_RIGHT);

        getChildren().addAll(counterImageView, counterStatusCircle);
    }

    public int getNo() {
        return no;
    }

    public boolean isStatus() {
        return status;
    }

    public int getType() {
        return type;
    }

    public Circle getCounterStatusCircle() {
        return counterStatusCircle;
    }
}
