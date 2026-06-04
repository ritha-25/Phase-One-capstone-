package org.atm.igiresystem.lab3.ui.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.atm.igiresystem.lab2.db.SchemaSetup;

public class IgirePayApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SchemaSetup.createTables();
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/org/atm/igiresystem/lab3/ui/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("IgirePay — Secure Digital Wallet");
        stage.setScene(scene);
        stage.setMinWidth(480);
        stage.setMinHeight(560);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
