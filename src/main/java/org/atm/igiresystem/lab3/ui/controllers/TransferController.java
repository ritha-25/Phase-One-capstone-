package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab2.dao.CustomerDAO;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class TransferController implements Initializable {

    // Tab buttons
    @FXML private Button tabDeposit;
    @FXML private Button tabWithdraw;
    @FXML private Button tabTransfer;

    // Deposit form
    @FXML private VBox          depositForm;
    @FXML private ComboBox<String> depositAccountCombo;
    @FXML private TextField     depositAmount;
    @FXML private PasswordField depositPin;
    @FXML private Label         depositMsg;

    // Withdraw form
    @FXML private VBox          withdrawForm;
    @FXML private ComboBox<String> withdrawAccountCombo;
    @FXML private TextField     withdrawAmount;
    @FXML private PasswordField withdrawPin;
    @FXML private Label         withdrawMsg;

    // Transfer form
    @FXML private VBox          transferForm;
    @FXML private ComboBox<String> transferAccountCombo;
    @FXML private TextField     receiverPhone;
    @FXML private Label         receiverNameLabel;
    @FXML private TextField     transferAmount;
    @FXML private PasswordField transferPin;
    @FXML private Label         transferMsg;

    private final AccountService     accountService     = new AccountService();
    private final TransactionService transactionService = new TransactionService();
    private final CustomerDAO        customerDAO        = new CustomerDAO();

    private List<Account> myAccounts;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadMyAccounts();
        // Live receiver name lookup
        receiverPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 10) {
                customerDAO.findByPhone(newVal.trim()).ifPresentOrElse(
                    c -> receiverNameLabel.setText("✓ " + c.getFullName()),
                    ()  -> receiverNameLabel.setText("Phone not registered")
                );
            } else {
                receiverNameLabel.setText("");
            }
        });
    }

    private void loadMyAccounts() {
        if (Session.getCustomer() == null) return;
        myAccounts = accountService.getCustomerAccounts(Session.getCustomer().getId());
        depositAccountCombo.getItems().clear();
        withdrawAccountCombo.getItems().clear();
        transferAccountCombo.getItems().clear();
        for (Account acc : myAccounts) {
            String label = acc.getAccountType() + " #" + acc.getId() +
                           "  (" + String.format("%.2f", acc.getBalance()) + " RWF)";
            depositAccountCombo.getItems().add(label);
            withdrawAccountCombo.getItems().add(label);
            transferAccountCombo.getItems().add(label);
        }
        if (!myAccounts.isEmpty()) {
            depositAccountCombo.getSelectionModel().selectFirst();
            withdrawAccountCombo.getSelectionModel().selectFirst();
            transferAccountCombo.getSelectionModel().selectFirst();
        }
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    @FXML private void switchToDeposit() {
        depositForm.setVisible(true);  depositForm.setManaged(true);
        withdrawForm.setVisible(false); withdrawForm.setManaged(false);
        transferForm.setVisible(false); transferForm.setManaged(false);
        styleActiveTab(tabDeposit);
    }

    @FXML private void switchToWithdraw() {
        depositForm.setVisible(false);  depositForm.setManaged(false);
        withdrawForm.setVisible(true);  withdrawForm.setManaged(true);
        transferForm.setVisible(false); transferForm.setManaged(false);
        styleActiveTab(tabWithdraw);
    }

    @FXML private void switchToTransfer() {
        depositForm.setVisible(false);  depositForm.setManaged(false);
        withdrawForm.setVisible(false); withdrawForm.setManaged(false);
        transferForm.setVisible(true);  transferForm.setManaged(true);
        styleActiveTab(tabTransfer);
    }

    private void styleActiveTab(Button active) {
        String activeStyle  = "-fx-background-color:#1E8E3E;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:12 28;-fx-cursor:hand;";
        String inactiveStyle = "-fx-background-color:#F9FAFB;-fx-text-fill:#6B7280;-fx-font-weight:bold;-fx-padding:12 28;-fx-cursor:hand;";
        tabDeposit.setStyle(inactiveStyle  + "-fx-background-radius:10 0 0 10;");
        tabWithdraw.setStyle(inactiveStyle + "-fx-background-radius:0;");
        tabTransfer.setStyle(inactiveStyle + "-fx-background-radius:0 10 10 0;");
        if (active == tabDeposit)  tabDeposit.setStyle(activeStyle  + "-fx-background-radius:10 0 0 10;");
        if (active == tabWithdraw) tabWithdraw.setStyle(activeStyle + "-fx-background-radius:0;");
        if (active == tabTransfer) tabTransfer.setStyle(activeStyle + "-fx-background-radius:0 10 10 0;");
    }

    // ── Deposit ───────────────────────────────────────────────────────────────

    @FXML
    private void handleDeposit() {
        int idx = depositAccountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg(depositMsg, "Please select an account.", false); return; }

        double amount = parseAmount(depositAmount, depositMsg);
        if (amount <= 0) return;

        String pin = depositPin.getText().trim();
        if (!validatePin(pin, depositMsg)) return;

        String ref    = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.deposit(myAccounts.get(idx).getId(), amount, ref);
        showMsg(depositMsg, result, result.startsWith("SUCCESS"));
        if (result.startsWith("SUCCESS")) { depositAmount.clear(); depositPin.clear(); loadMyAccounts(); }
    }

    // ── Withdraw ──────────────────────────────────────────────────────────────

    @FXML
    private void handleWithdraw() {
        int idx = withdrawAccountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg(withdrawMsg, "Please select an account.", false); return; }

        double amount = parseAmount(withdrawAmount, withdrawMsg);
        if (amount <= 0) return;

        String pin = withdrawPin.getText().trim();
        if (!validatePin(pin, withdrawMsg)) return;

        String ref    = "WIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.withdraw(myAccounts.get(idx).getId(), amount, ref);
        showMsg(withdrawMsg, result, result.startsWith("SUCCESS"));
        if (result.startsWith("SUCCESS")) { withdrawAmount.clear(); withdrawPin.clear(); loadMyAccounts(); }
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    @FXML
    private void handleTransfer() {
        int idx = transferAccountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg(transferMsg, "Please select your account.", false); return; }

        String phone = receiverPhone.getText().trim();
        if (phone.isEmpty()) { showMsg(transferMsg, "Please enter receiver phone number.", false); return; }

        Optional<Customer> receiverCustomer = customerDAO.findByPhone(phone);
        if (receiverCustomer.isEmpty()) { showMsg(transferMsg, "Receiver not found.", false); return; }

        List<Account> receiverAccounts = accountService.getCustomerAccounts(receiverCustomer.get().getId());
        Optional<Account> receiverWallet = receiverAccounts.stream()
            .filter(a -> "WALLET".equals(a.getAccountType())).findFirst();
        if (receiverWallet.isEmpty()) { showMsg(transferMsg, "Receiver has no wallet account.", false); return; }

        double amount = parseAmount(transferAmount, transferMsg);
        if (amount <= 0) return;

        String pin = transferPin.getText().trim();
        if (!validatePin(pin, transferMsg)) return;

        String ref    = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.transfer(
            myAccounts.get(idx).getId(), receiverWallet.get().getId(), amount, ref);
        showMsg(transferMsg, result, result.startsWith("SUCCESS"));
        if (result.startsWith("SUCCESS")) {
            transferAmount.clear(); transferPin.clear(); receiverPhone.clear();
            receiverNameLabel.setText(""); loadMyAccounts();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean validatePin(String pin, Label msgLabel) {
        if (pin.isEmpty()) { showMsg(msgLabel, "Please enter your PIN.", false); return false; }
        if (!accountService.validatePinByCustomer(Session.getCustomer().getId(), pin)) {
            showMsg(msgLabel, "Incorrect PIN. Please try again.", false);
            return false;
        }
        return true;
    }

    private double parseAmount(TextField field, Label msgLabel) {
        try {
            double val = Double.parseDouble(field.getText().trim().replace(",", ""));
            if (val <= 0) { showMsg(msgLabel, "Amount must be greater than 0.", false); return -1; }
            return val;
        } catch (NumberFormatException e) {
            showMsg(msgLabel, "Please enter a valid amount.", false);
            return -1;
        }
    }

    private void showMsg(Label label, String msg, boolean success) {
        label.setStyle(success
            ? "-fx-text-fill:#1E8E3E;-fx-font-size:12px;"
            : "-fx-text-fill:#DC2626;-fx-font-size:12px;");
        label.setText(msg);
    }
}
