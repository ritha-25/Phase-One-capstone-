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

        List<Account> existing = accountService.getCustomerAccounts(Session.getCustomer().getId());
        for (Account acc : existing) {
            if ("WALLET".equals(acc.getAccountType())) {
                showMsg("You already have a Main Wallet.", false);
                return;
            }
        }

        accountService.createWalletAccount(Session.getCustomer().getId());
        showMsg("Main Wallet created successfully.", true);
        loadAccounts();
    }

    @FXML
    private void createSavings() {
        if (Session.getCustomer() == null) return;

        List<Account> existing = accountService.getCustomerAccounts(Session.getCustomer().getId());
        for (Account acc : existing) {
            if ("SAVINGS".equals(acc.getAccountType())) {
                showMsg("You already have a Savings Account.", false);
                return;
            }
        }

        accountService.createSavingsAccount(Session.getCustomer().getId());
        showMsg("Savings Account created successfully.", true);
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
            row.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;-fx-padding:14 16;" +
                         "-fx-border-color:#E8E8E8;-fx-border-radius:10;-fx-border-width:1;" +
                         "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),6,0,0,1);");
            row.setSpacing(0);

            VBox info = new VBox(4);
            info.setStyle("-fx-alignment:CENTER_LEFT;");
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            boolean isWallet = "WALLET".equals(acc.getAccountType());
            String icon        = isWallet ? "💳" : "🏦";
            String displayName = isWallet ? "Main Wallet" : "Savings Account";
            String accentColor = isWallet ? "#005B2A" : "#F5A623";

            Label typeLabel = new Label(icon + "  " + displayName);
            typeLabel.setStyle("-fx-font-weight:bold;-fx-text-fill:#1A1A1A;-fx-font-size:13px;");

            String hint = isWallet
                ? "For deposits, withdrawals and sending money"
                : "For saving — no fees when moving to/from wallet";
            Label hintLabel = new Label(hint);
            hintLabel.setStyle("-fx-text-fill:#9CA3AF;-fx-font-size:11px;");

            info.getChildren().addAll(typeLabel, hintLabel);

            Label balLabel = new Label(String.format("%.2f RWF", acc.getBalance()));
            balLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + accentColor + ";-fx-alignment:CENTER_RIGHT;");

            row.getChildren().addAll(info, balLabel);
            accountsList.getChildren().add(row);
        }
    }

    private void showMsg(String msg, boolean success) {
        createMsg.setStyle(success
            ? "-fx-text-fill:#005B2A;-fx-font-size:13px;"
            : "-fx-text-fill:#DC2626;-fx-font-size:13px;");
        createMsg.setText(msg);
    }
}
