package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class SavingsController implements Initializable {

    @FXML private ComboBox<String> accountCombo;
    @FXML private Label            balanceLabel;
    @FXML private Label            accountInfoLabel;
    @FXML private TextField        amountField;
    @FXML private PasswordField    pinField;
    @FXML private Label            messageLabel;

    private final AccountService     accountService     = new AccountService();
    private final TransactionService transactionService = new TransactionService();

    private List<Account> savingsAccounts;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSavingsAccounts();
    }

    private void loadSavingsAccounts() {
        if (Session.getCustomer() == null) return;
        savingsAccounts = accountService.getCustomerAccounts(Session.getCustomer().getId())
            .stream().filter(a -> "SAVINGS".equals(a.getAccountType())).toList();

        accountCombo.getItems().clear();
        for (Account acc : savingsAccounts) {
            accountCombo.getItems().add("Savings #" + acc.getId() +
                "  (" + String.format("%.2f", acc.getBalance()) + " RWF)");
        }
        if (!savingsAccounts.isEmpty()) {
            accountCombo.getSelectionModel().selectFirst();
            refreshBalance(savingsAccounts.get(0));
        }
    }

    @FXML
    private void onAccountSelected() {
        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < savingsAccounts.size()) {
            refreshBalance(savingsAccounts.get(idx));
        }
    }

    @FXML
    private void handleDeposit() {
        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg("Please select a savings account.", false); return; }

        double amount = parseAmount();
        if (amount <= 0) return;

        String pin = pinField.getText().trim();
        if (!validatePin(pin)) return;

        String ref    = "SAV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.deposit(savingsAccounts.get(idx).getId(), amount, ref);
        showMsg(result, result.startsWith("SUCCESS"));
        if (result.startsWith("SUCCESS")) {
            amountField.clear(); pinField.clear();
            loadSavingsAccounts();
        }
    }

    @FXML
    private void handleWithdraw() {
        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg("Please select a savings account.", false); return; }

        double amount = parseAmount();
        if (amount <= 0) return;

        String pin = pinField.getText().trim();
        if (!validatePin(pin)) return;

        String ref    = "SWD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.withdraw(savingsAccounts.get(idx).getId(), amount, ref);
        showMsg(result, result.startsWith("SUCCESS") || result.contains("48 hours"));
        if (!result.startsWith("FAILED") && !result.startsWith("DUPLICATE")) {
            amountField.clear(); pinField.clear();
        }
    }

    private void refreshBalance(Account acc) {
        balanceLabel.setText(String.format("%.2f RWF", acc.getBalance()));
        accountInfoLabel.setText("Savings Account #" + acc.getId());
    }

    private boolean validatePin(String pin) {
        if (pin.isEmpty()) { showMsg("Please enter your PIN.", false); return false; }
        if (!accountService.validatePinByCustomer(Session.getCustomer().getId(), pin)) {
            showMsg("Incorrect PIN.", false);
            return false;
        }
        return true;
    }

    private double parseAmount() {
        try {
            double val = Double.parseDouble(amountField.getText().trim().replace(",", ""));
            if (val <= 0) { showMsg("Amount must be greater than 0.", false); return -1; }
            return val;
        } catch (NumberFormatException e) {
            showMsg("Please enter a valid amount.", false);
            return -1;
        }
    }

    private void showMsg(String msg, boolean success) {
        messageLabel.setStyle(success
            ? "-fx-text-fill:#1E8E3E;-fx-font-size:12px;"
            : "-fx-text-fill:#DC2626;-fx-font-size:12px;");
        messageLabel.setText(msg);
    }
}
