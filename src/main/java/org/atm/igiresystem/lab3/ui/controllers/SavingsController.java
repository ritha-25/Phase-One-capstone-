package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class SavingsController implements Initializable {

    @FXML private ComboBox<String> accountCombo;
    @FXML private ComboBox<String> walletCombo;
    @FXML private Label            balanceLabel;
    @FXML private Label            accountInfoLabel;
    @FXML private TextField        amountField;
    @FXML private PasswordField    pinField;
    @FXML private Label            messageLabel;

    private final AccountService     accountService     = new AccountService();
    private final TransactionService transactionService = new TransactionService();

    private List<Account> savingsAccounts = new ArrayList<>();
    private List<Account> walletAccounts  = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadAccounts();
    }

    private void loadAccounts() {
        if (Session.getCustomer() == null) return;

        List<Account> all = accountService.getCustomerAccounts(Session.getCustomer().getId());
        savingsAccounts.clear();
        walletAccounts.clear();

        for (Account acc : all) {
            if ("SAVINGS".equals(acc.getAccountType())) savingsAccounts.add(acc);
            if ("WALLET".equals(acc.getAccountType()))  walletAccounts.add(acc);
        }

        accountCombo.getItems().clear();
        for (Account acc : savingsAccounts) {
            accountCombo.getItems().add("Savings Account  (" + String.format("%.2f", acc.getBalance()) + " RWF)");
        }
        if (!savingsAccounts.isEmpty()) {
            accountCombo.getSelectionModel().selectFirst();
            refreshBalance(savingsAccounts.get(0));
        } else {
            balanceLabel.setText("0.00 RWF");
            accountInfoLabel.setText("No savings account. Create one in My Accounts.");
        }

        walletCombo.getItems().clear();
        for (Account acc : walletAccounts) {
            walletCombo.getItems().add("Main Wallet  (" + String.format("%.2f", acc.getBalance()) + " RWF)");
        }
        if (!walletAccounts.isEmpty()) walletCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onAccountSelected() {
        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < savingsAccounts.size()) {
            refreshBalance(savingsAccounts.get(idx));
        }
    }

    @FXML
    private void handleSaveFromWallet() {
        if (walletAccounts.isEmpty()) {
            showMsg("You need a Main Wallet to save money. Create one in My Accounts.", false);
            return;
        }
        if (savingsAccounts.isEmpty()) {
            showMsg("You need a Savings Account. Create one in My Accounts.", false);
            return;
        }

        int walIdx = walletCombo.getSelectionModel().getSelectedIndex();
        int savIdx = accountCombo.getSelectionModel().getSelectedIndex();
        if (walIdx < 0 || savIdx < 0) { showMsg("Please select both accounts.", false); return; }

        double amount = parseAmount();
        if (amount <= 0) return;

        if (!validatePin(pinField.getText().trim())) return;

        Account wallet  = walletAccounts.get(walIdx);
        Account savings = savingsAccounts.get(savIdx);

        if (wallet.getBalance() < amount) {
            showMsg("Insufficient balance in Main Wallet. Available: " + String.format("%.2f", wallet.getBalance()) + " RWF.", false);
            return;
        }

        String ref = "SAV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.transfer(wallet.getId(), savings.getId(), amount, ref);

        if (result.startsWith("SUCCESS")) {
            showMsg(String.format("%.2f", amount) + " RWF moved from Main Wallet to Savings. No fee charged.", true);
            amountField.clear();
            pinField.clear();
            loadAccounts();
        } else {
            showMsg(result, false);
        }
    }

    @FXML
    private void handleWithdrawToWallet() {
        if (savingsAccounts.isEmpty()) {
            showMsg("No savings account found.", false);
            return;
        }
        if (walletAccounts.isEmpty()) {
            showMsg("You need a Main Wallet to receive funds. Create one in My Accounts.", false);
            return;
        }

        int savIdx = accountCombo.getSelectionModel().getSelectedIndex();
        int walIdx = walletCombo.getSelectionModel().getSelectedIndex();
        if (savIdx < 0 || walIdx < 0) { showMsg("Please select both accounts.", false); return; }

        double amount = parseAmount();
        if (amount <= 0) return;

        if (!validatePin(pinField.getText().trim())) return;

        Account savings = savingsAccounts.get(savIdx);
        Account wallet  = walletAccounts.get(walIdx);

        if (savings.getBalance() < amount) {
            showMsg("Insufficient savings balance. Available: " + String.format("%.2f", savings.getBalance()) + " RWF.", false);
            return;
        }

        String ref = "SWD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.transfer(savings.getId(), wallet.getId(), amount, ref);

        if (result.startsWith("SUCCESS")) {
            showMsg(String.format("%.2f", amount) + " RWF withdrawn from Savings to Main Wallet. No fee charged.", true);
            amountField.clear();
            pinField.clear();
            loadAccounts();
        } else {
            showMsg(result, false);
        }
    }

    private void refreshBalance(Account acc) {
        balanceLabel.setText(String.format("%.2f RWF", acc.getBalance()));
        accountInfoLabel.setText("Savings Account");
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
            ? "-fx-text-fill:#005B2A;-fx-font-size:12px;"
            : "-fx-text-fill:#DC2626;-fx-font-size:12px;");
        messageLabel.setText(msg);
    }
}
