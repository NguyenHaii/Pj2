module com.example.pj2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens quanlynet to javafx.fxml;
    exports quanlynet;
    exports quanlynet.controller;
    opens quanlynet.controller to javafx.fxml;
    opens quanlynet.view to javafx.fxml;
}