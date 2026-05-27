package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab3.services.AccountService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AccountsController implements Initializable {

    @FXML private VBox  accountsList;
    @FXML private Label createMsg;

    private final AccountService accountService = new AccountService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadAccounts();
    }

    @FXML
    private void createWallet() {
        if (Session.getCustomer() == null) return;
        Account acc = accountService.createWalletAccount(Session.getCustomer().getId());
        createMsg.setStyle("-fx-text-fill:#1E8E3E;-fx-font-size:13px;");
        createMsg.setText("✓ Wallet Account created! Account #" + acc.getId());
        loadAccounts();
    }

    @FXML
    private void createSavings() {
        if (Session.getCustomer() == null) return;
        Account acc = accountService.createSavingsAccount(Session.getCustomer().getId());
        createMsg.setStyle("-fx-text-fill:#1E8E3E;-fx-font-size:13px;");
        createMsg.setText("✓ Savings Account created! Account #" + acc.getId());
        loadAccounts();
    }

    @FXML
    public void loadAccounts() {
        accountsList.getChildren().clear();
        if (Session.getCustomer() == null) return;

        List<Account> accounts = accountService.getCustomerAccounts(Session.getCustomer().getId());
        if (accounts.isEmpty()) {
            Label empty = new Label("No accounts yet. Create one above.");
            empty.setStyle("-fx-text-fill:#9CA3AF;-fx-font-size:13px;");
            accountsList.getChildren().add(empty);
            return;
        }

        for (Account acc : accounts) {
            HBox row = new HBox();
            row.setStyle("-fx-background-color:#F9FAFB;-fx-background-radius:8;-fx-padding:14 16;" +
                         "-fx-border-color:#E5E7EB;-fx-border-radius:8;-fx-border-width:1;");
            row.setSpacing(0);

            VBox info = new VBox(4);
            info.setStyle("-fx-alignment:CENTER_LEFT;");
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            String icon = "WALLET".equals(acc.getAccountType()) ? "💳" : "🏦";
            Label typeLabel = new Label(icon + "  " + acc.getAccountType() + " Account");
            typeLabel.setStyle("-fx-font-weight:bold;-fx-text-fill:#374151;-fx-font-size:13px;");
            Label idLabel = new Label("Account #" + acc.getId());
            idLabel.setStyle("-fx-text-fill:#9CA3AF;-fx-font-size:11px;");
            info.getChildren().addAll(typeLabel, idLabel);

            Label balLabel = new Label(String.format("%.2f RWF", acc.getBalance()));
            balLabel.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1E8E3E;" +
                              "-fx-alignment:CENTER_RIGHT;");

            row.getChildren().addAll(info, balLabel);
            accountsList.getChildren().add(row);
        }
    }
}
