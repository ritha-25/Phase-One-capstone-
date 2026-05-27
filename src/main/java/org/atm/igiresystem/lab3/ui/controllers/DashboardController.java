package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab3.services.AccountService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label     headerTitle;
    @FXML private Label     headerUser;
    @FXML private Label     sidebarUserLabel;
    @FXML private Button    adminBtn;
    @FXML private Label     adminSection;
    @FXML private Button    btnHome;
    @FXML private Button    btnAccounts;
    @FXML private Button    btnTransfer;
    @FXML private Button    btnSavings;
    @FXML private Button    btnHistory;

    private final AccountService accountService = new AccountService();
    private Button activeNavBtn = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer customer = Session.getCustomer();
        if (customer != null) {
            sidebarUserLabel.setText(customer.getFullName() + "\n" + customer.getPhoneNumber());
            headerUser.setText(customer.getPhoneNumber());
        }
        if (Session.isAdmin()) {
            adminBtn.setVisible(true);
            adminBtn.setManaged(true);
            adminSection.setVisible(true);
            adminSection.setManaged(true);
        }
        setActive(btnHome);
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        setActive(btnHome);
        headerTitle.setText("Home");
        Customer customer = Session.getCustomer();
        if (customer == null) { loadPane("transfer.fxml"); return; }

        List<Account> accounts = accountService.getCustomerAccounts(customer.getId());

        VBox home = new VBox(20);
        home.setStyle("-fx-padding:4 0 0 0;");

        // Balance cards
        for (Account acc : accounts) {
            VBox card = new VBox(6);
            card.setStyle("-fx-background-color:linear-gradient(to bottom right,#1E8E3E,#2ECC71);" +
                          "-fx-background-radius:14;-fx-padding:22 26;" +
                          "-fx-effect:dropshadow(gaussian,rgba(30,142,62,0.30),12,0,0,3);");
            card.setMaxWidth(460);

            Label typeLabel = new Label(acc.getAccountType() + " ACCOUNT");
            typeLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.80);-fx-font-size:11px;-fx-font-weight:bold;");

            Label balLabel = new Label(String.format("%.2f RWF", acc.getBalance()));
            balLabel.setStyle("-fx-text-fill:#FFFFFF;-fx-font-size:30px;-fx-font-weight:bold;");

            Label idLabel = new Label("Account #" + acc.getId());
            idLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-size:12px;");

            card.getChildren().addAll(typeLabel, balLabel, idLabel);
            home.getChildren().add(card);
        }

        if (accounts.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:14;-fx-padding:32;" +
                           "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),10,0,0,2);-fx-alignment:CENTER;");
            empty.setMaxWidth(460);
            Label icon = new Label("💳");
            icon.setStyle("-fx-font-size:40px;");
            Label msg = new Label("No accounts yet");
            msg.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#374151;");
            Label hint = new Label("Go to 'My Accounts' to create your first account");
            hint.setStyle("-fx-font-size:12px;-fx-text-fill:#9CA3AF;");
            empty.getChildren().addAll(icon, msg, hint);
            home.getChildren().add(empty);
        }

        // Quick actions row
        HBox quickActions = new HBox(12);
        quickActions.setMaxWidth(460);
        quickActions.setStyle("-fx-padding:4 0 0 0;");

        String[][] actions = {
            {"💸", "Send"},
            {"⬇", "Deposit"},
            {"⬆", "Withdraw"},
            {"🏦", "Savings"},
            {"📋", "History"}
        };
        Runnable[] handlers = {
            this::showTransfer, this::showTransfer,
            this::showTransfer, this::showSavings, this::showHistory
        };

        for (int i = 0; i < actions.length; i++) {
            final int idx = i;
            VBox btn = new VBox(6);
            btn.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;" +
                         "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);" +
                         "-fx-padding:14 10;-fx-cursor:hand;-fx-alignment:CENTER;");
            btn.setPrefWidth(80);
            Label icon = new Label(actions[i][0]);
            icon.setStyle("-fx-font-size:20px;");
            Label lbl = new Label(actions[i][1]);
            lbl.setStyle("-fx-font-size:11px;-fx-text-fill:#4B5563;-fx-font-weight:bold;");
            btn.getChildren().addAll(icon, lbl);
            btn.setOnMouseClicked(e -> handlers[idx].run());
            btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#FFFFFF", "#F0FFF4")));
            btn.setOnMouseExited(e  -> btn.setStyle(btn.getStyle().replace("#F0FFF4", "#FFFFFF")));
            quickActions.getChildren().add(btn);
        }
        home.getChildren().add(quickActions);
        setContent(home);
    }

    @FXML public void showAccounts() {
        setActive(btnAccounts);
        headerTitle.setText("My Accounts");
        loadPane("accounts.fxml");
    }

    @FXML public void showTransfer() {
        setActive(btnTransfer);
        headerTitle.setText("Send Money");
        loadPane("transfer.fxml");
    }

    @FXML public void showSavings() {
        setActive(btnSavings);
        headerTitle.setText("Savings");
        loadPane("savings.fxml");
    }

    @FXML public void showHistory() {
        setActive(btnHistory);
        headerTitle.setText("Transaction History");
        loadPane("history.fxml");
    }

    @FXML public void showAdmin() {
        headerTitle.setText("Admin Panel");
        loadPane("admin.fxml");
    }

    @FXML
    public void handleLogout() {
        Session.clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/atm/igiresystem/lab3/ui/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 440, 580));
            stage.setTitle("IgirePay — Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPane(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/atm/igiresystem/lab3/ui/" + fxml));
            Node node = loader.load();
            setContent(node);
        } catch (Exception e) {
            Label err = new Label("Failed to load screen: " + e.getMessage());
            err.setStyle("-fx-text-fill:#DC2626;");
            setContent(err);
        }
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    private void setActive(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("nav-btn-active");
        }
        activeNavBtn = btn;
        if (btn != null) btn.getStyleClass().add("nav-btn-active");
    }
}
