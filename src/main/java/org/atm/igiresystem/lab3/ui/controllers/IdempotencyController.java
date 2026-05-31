package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class IdempotencyController implements Initializable {

    @FXML private TextField        refIdField;
    @FXML private TextField        amountField;
    @FXML private ComboBox<String> accountCombo;
    @FXML private PasswordField    pinField;
    @FXML private VBox             logBox;
    @FXML private Label            statusLabel;

    private final TransactionService transactionService = new TransactionService();
    private final AccountService     accountService     = new AccountService();

    private List<Account> myAccounts;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadAccounts();
        addLog("System ready. Enter a Reference ID and click Send to test idempotency.", "#374151");
    }

    private void loadAccounts() {
        if (Session.getCustomer() == null) return;
        myAccounts = accountService.getCustomerAccounts(Session.getCustomer().getId());
        accountCombo.getItems().clear();
        for (Account acc : myAccounts) {
            String name = "WALLET".equals(acc.getAccountType()) ? "Main Wallet" : "Savings Account";
            accountCombo.getItems().add(name + " #" + acc.getId());
        }
        if (!myAccounts.isEmpty()) accountCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSendTransaction() {
        String refId = refIdField.getText().trim();
        if (refId.isEmpty()) {
            showStatus("Please enter a Reference ID.", false);
            return;
        }

        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            showStatus("Please select an account.", false);
            return;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showStatus("Please enter an amount.", false);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.replace(",", ""));
        } catch (NumberFormatException e) {
            showStatus("Invalid amount.", false);
            return;
        }

        String pin = pinField.getText().trim();
        if (!accountService.validatePinByCustomer(Session.getCustomer().getId(), pin)) {
            showStatus("Incorrect PIN.", false);
            return;
        }

        int accountId = myAccounts.get(idx).getId();

        addLog("→ Sending transaction with Reference ID: " + refId + "  |  Amount: " + amount + " RWF", "#003087");

        String result = transactionService.deposit(accountId, amount, refId);

        if (result.startsWith("DUPLICATE")) {
            addLog("✗ DUPLICATE DETECTED — Reference ID '" + refId + "' was already processed.", "#DC2626");
            addLog("  The system rejected this transaction to prevent double-charging.", "#DC2626");
            showStatus("Duplicate transaction blocked. Same reference ID cannot be used twice.", false);
        } else if (result.startsWith("SUCCESS")) {
            addLog("✓ Transaction processed successfully. Reference ID: " + refId, "#003087");
            addLog("  This reference ID is now stored and will be rejected if retried.", "#003087");
            showStatus("Transaction successful. Try sending again with the same Reference ID to see duplicate prevention.", true);
        } else {
            addLog("✗ Transaction failed: " + result, "#D97706");
            showStatus(result, false);
        }
    }

    @FXML
    private void handleClearLog() {
        logBox.getChildren().clear();
        addLog("Log cleared. Ready for new test.", "#374151");
        statusLabel.setText("");
    }

    @FXML
    private void handleGenerateRef() {
        String generated = "REF-" + System.currentTimeMillis();
        refIdField.setText(generated);
        addLog("Generated new Reference ID: " + generated, "#6B7280");
    }

    private void addLog(String message, String color) {
        HBox row = new HBox(8);
        row.setStyle("-fx-padding:6 10;-fx-background-color:#F9FAFB;-fx-background-radius:6;");

        Label lbl = new Label(message);
        lbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + color + ";-fx-font-family:monospace;");
        lbl.setWrapText(true);
        lbl.setMaxWidth(500);

        row.getChildren().add(lbl);
        logBox.getChildren().add(row);
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setStyle(success
            ? "-fx-text-fill:#003087;-fx-font-size:13px;-fx-font-weight:bold;"
            : "-fx-text-fill:#DC2626;-fx-font-size:13px;-fx-font-weight:bold;");
        statusLabel.setText(msg);
    }
}
