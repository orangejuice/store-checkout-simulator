module checkout {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.iconli.core;
    requires org.kordamp.ikonli.javafx;
    requires java.logging;

    opens application to javafx.graphics;
    opens controller to javafx.fxml;
}