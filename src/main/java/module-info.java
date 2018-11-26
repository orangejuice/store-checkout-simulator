module checkout {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.iconli.core;
    requires org.kordamp.ikonli.javafx;
    requires java.logging;
    requires joda.time;
    requires javafx.swing;
    requires itextpdf;

    opens application to javafx.graphics;
    opens controller to javafx.fxml;
}