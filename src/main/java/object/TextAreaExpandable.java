package object;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class TextAreaExpandable extends TextArea {

    private final double DEFAULT_HEIGHT = 17.0;

    public TextAreaExpandable() {
        setMinHeight(DEFAULT_HEIGHT);
        setPrefHeight(DEFAULT_HEIGHT);
        setMaxHeight(DEFAULT_HEIGHT);
        disableEnter();
        setEditable(false);

        setStyle("-fx-background-color: transparent; -fx-font-size: 14");
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        setWrapText(true);
        setPadding(new Insets(0, 0, 0, 0));

        ScrollPane scrollPane = (ScrollPane) lookup(".scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(0, 0, 0, 0));
        scrollPane.setStyle("-fx-background-color: transparent;");

        StackPane viewport = (StackPane) scrollPane.lookup(".viewport");
        viewport.setPadding(new Insets(0, 0, 0, 0));
        viewport.setStyle("-fx-background-color: transparent;");

        Region content = (Region) viewport.lookup(".content");
        content.setPadding(new Insets(-1, 1, 0, 1));
        content.setStyle("-fx-background-color: transparent;");

        Text text = (Text) content.lookup(".text");

        text.textProperty().addListener((property) -> {
            double textHeight = text.getBoundsInLocal().getHeight();
            if (textHeight < DEFAULT_HEIGHT) {
                textHeight = DEFAULT_HEIGHT;
            }

            textHeight = textHeight + 1;

            setMinHeight(textHeight);
            setPrefHeight(textHeight);
            setMaxHeight(textHeight);
        });
    }

    private void disableEnter() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
            }
        });
    }
}