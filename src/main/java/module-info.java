module org.atm.igiresystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens org.atm.igiresystem to javafx.fxml;
    opens org.atm.igiresystem.lab3.ui.controllers to javafx.fxml;
    opens org.atm.igiresystem.lab3.ui.app to javafx.fxml;

    exports org.atm.igiresystem;
    exports org.atm.igiresystem.lab1.models;
    exports org.atm.igiresystem.lab2.db;
    exports org.atm.igiresystem.lab2.dao;
    exports org.atm.igiresystem.lab3.services;
    exports org.atm.igiresystem.lab3.ui.controllers;
    exports org.atm.igiresystem.lab3.ui.app;
}
