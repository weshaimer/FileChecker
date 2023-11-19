module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.sql;
    requires MarvinFramework;
    requires thumbnailator;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;
    requires com.google.common;

    opens org.example to javafx.fxml;
    exports org.example;
}