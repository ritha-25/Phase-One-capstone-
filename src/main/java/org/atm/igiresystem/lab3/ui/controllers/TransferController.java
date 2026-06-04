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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class TransferController implements Initializable {

    @FXML private Button tabDeposit;
    @FXML private Button tabWithdraw;
    @FXML private Button tabTransfer;

    @FXML private VBox             depositForm;
    @FXML private ComboBox<String> depositAccountCombo;
    @FXML private TextField        depositAmount;
    @FXML private PasswordField    depositPin;
    @FXML private Label            depositMsg;

    @FXML private VBox             withdrawForm;
    @FXML private ComboBox<String> withdrawAccountCombo;
    @FXML private TextField        withdrawAmount;
    @FXML private PasswordField    withdrawPin;
    @FXML private Label            withdrawMsg;

    @FXML private VBox             transferForm;
    @FXML private ComboBox<String> transferAccountCombo;
    @FXML private TextField        receiverPhone;
    @FXML private Label            receiverNameLabel;
    @FXML private TextField        transferAmount;
    @FXML private PasswordField    transferPin;
    @FXML private Label            transferMsg;
    @FXML private VBox             refIdBox;
    @FXML private Label            refIdLabel;

    private final AccountService     accountService     = new AccountService();
    private final TransactionService transactionService = new TransactionService();
    private final CustomerDAO        customerDAO        = new CustomerDAO();

    private List<Account> walletAccounts = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadWalletAccounts();
        receiverPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 10) {
                Optional<Customer> found = customerDAO.findByPhone(newVal.trim());
                if (found.isPresent()) {
                    receiverNameLabel.setText("✓ " + found.get().getFullName());
                    receiverNameLabel.setStyle("-fx-text-fill:#005B2A;-fx-font-size:12px;-fx-font-weight:bold;");
                } else {
                    receiverNameLabel.setText("Phone not registered");
                    receiverNameLabel.setStyle("-fx-text-fill:#C62828;-fx-font-size:12px;");
                }
            } else {
                receiverNameLabel.setText("");
            }
        });
    }

    private void loadWalletAccounts() {
        if (Session.getCustomer() == null) return;
        List<Account> all = accountService.getCustomerAccounts(Session.getCustomer().getId());
        walletAccounts.clear();
        depositAccountCombo.getItems().clear();
        withdrawAccountCombo.getItems().clear();
        transferAccountCombo.getItems().clear();

        for (Account acc : all) {
            if ("WALLET".equals(acc.getAccountType())) {
                walletAccounts.add(acc);
                String label = "Main Wallet  (" + String.format("%.2f", acc.getBalance()) + " RWF)";
                depositAccountCombo.getItems().add(label);
                withdrawAccountCombo.getItems().add(label);
                transferAccountCombo.getItems().add(label);
            }
        }

        if (!walletAccounts.isEmpty()) {
            depositAccountCombo.getSelectionModel().selectFirst();
            withdrawAccountCombo.getSelectionModel().selectFirst();
            transferAccountCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML private void switchToDeposit() {
        depositForm.setVisible(true);   depositForm.setManaged(true);
        withdrawForm.setVisible(false); withdrawForm.setManaged(false);
        transferForm.setVisible(false); transferForm.setManaged(false);
        styleTab(tabDeposit);
    }

    @FXML private void switchToWithdraw() {
        depositForm.setVisible(false);  depositForm.setManaged(false);
        withdrawForm.setVisible(true);  withdrawForm.setManaged(true);
        transferForm.setVisible(false); transferForm.setManaged(false);
        styleTab(tabWithdraw);
    }

    @FXML private void switchToTransfer() {
        depositForm.setVisible(false);  depositForm.setManaged(false);
        withdrawForm.setVisible(false); withdrawForm.setManaged(false);
        transferForm.setVisible(true);  transferForm.setManaged(true);
        styleTab(tabTransfer);
    }

    private void styleTab(Button active) {
        String on  = "-fx-background-color:#005B2A;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:12 24;-fx-cursor:hand;";
        String off = "-fx-background-color:#F5F5F5;-fx-text-fill:#4B4B4B;-fx-font-weight:bold;-fx-padding:12 24;-fx-cursor:hand;";
        tabDeposit.setStyle(off  + "-fx-background-radius:10 0 0 10;");
        tabWithdraw.setStyle(off + "-fx-background-radius:0;");
        tabTransfer.setStyle(off + "-fx-background-radius:0 10 10 0;");
        if (active == tabDeposit)  tabDeposit.setStyle(on  + "-fx-background-radius:10 0 0 10;");
        if (active == tabWithdraw) tabWithdraw.setStyle(on + "-fx-background-radius:0;");
        if (active == tabTransfer) tabTransfer.setStyle(on + "-fx-background-radius:0 10 10 0;");
    }

    @FXML
    private void handleDeposit() {
        if (walletAccounts.isEmpty()) {
            showMsg(depositMsg, "You have no Main Wallet. Create one in My Accounts.", false);
            return;
        }
        int idx = depositAccountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg(depositMsg, "Please select a wallet.", false); return; }

        double amount = parseAmount(depositAmount, depositMsg);
        if (amount <= 0) return;

        if (!validatePin(depositPin.getText().trim(), depositMsg)) return;

        String ref    = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.deposit(walletAccounts.get(idx).getId(), amount, ref);
        showMsg(depositMsg, result.startsWith("SUCCESS")
            ? "Deposit successful. " + String.format("%.2f", amount) + " RWF added. Ref: " + ref
            : result, result.startsWith("SUCCESS"));
        if (result.startsWith("SUCCESS")) {
            depositAmount.clear(); depositPin.clear(); loadWalletAccounts();
        }
    }

    @FXML
    private void handleWithdraw() {
        if (walletAccounts.isEmpty()) {
            showMsg(withdrawMsg, "You have no Main Wallet.", false);
            return;
        }
        int idx = withdrawAccountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg(withdrawMsg, "Please select a wallet.", false); return; }

        double amount = parseAmount(withdrawAmount, withdrawMsg);
        if (amount <= 0) return;

        if (!validatePin(withdrawPin.getText().trim(), withdrawMsg)) return;

        String ref    = "WIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result = transactionService.withdraw(walletAccounts.get(idx).getId(), amount, ref);
        showMsg(withdrawMsg, result.startsWith("SUCCESS")
            ? "Withdrawal successful. " + String.format("%.2f", amount) + " RWF withdrawn. Ref: " + ref
            : result, result.startsWith("SUCCESS"));
        if (result.startsWith("SUCCESS")) {
            withdrawAmount.clear(); withdrawPin.clear(); loadWalletAccounts();
        }
    }

    @FXML
    private void handleTransfer() {
        if (walletAccounts.isEmpty()) {
            showMsg(transferMsg, "You need a Main Wallet to send money.", false);
            return;
        }
        int idx = transferAccountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg(transferMsg, "Please select your wallet.", false); return; }

        String phone = receiverPhone.getText().trim();
        if (phone.isEmpty()) { showMsg(transferMsg, "Please enter receiver phone number.", false); return; }

        if (phone.equals(Session.getCustomer().getPhoneNumber())) {
            showMsg(transferMsg, "You cannot send money to yourself. Use the Savings screen to move between your accounts.", false);
            return;
        }

        Optional<Customer> receiverCustomer = customerDAO.findByPhone(phone);
        if (receiverCustomer.isEmpty()) { showMsg(transferMsg, "Receiver not found.", false); return; }

        List<Account> receiverAccounts = accountService.getCustomerAccounts(receiverCustomer.get().getId());
        Account receiverWallet = null;
        for (Account a : receiverAccounts) {
            if ("WALLET".equals(a.getAccountType())) { receiverWallet = a; break; }
        }
        if (receiverWallet == null) { showMsg(transferMsg, "Receiver has no wallet account.", false); return; }

        double amount = parseAmount(transferAmount, transferMsg);
        if (amount <= 0) return;

        if (!validatePin(transferPin.getText().trim(), transferMsg)) return;

        int senderAccountId   = walletAccounts.get(idx).getId();
        int receiverAccountId = receiverWallet.getId();
        String ref = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String result = transactionService.transfer(senderAccountId, receiverAccountId, amount, ref);

        if (result.startsWith("RETRY_DETECTED")) {
            String[] parts = result.split("\\|");
            String existingRef    = parts.length > 1 ? parts[1] : "N/A";
            String existingAmount = parts.length > 2 ? parts[2] : "N/A";
            String existingStatus = parts.length > 3 ? parts[3] : "N/A";

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Transaction Already Sent");
            alert.setHeaderText("A similar transaction was recently completed.");
            alert.setContentText(
                "Transaction Reference: " + existingRef + "\n" +
                "Amount: " + existingAmount + " RWF\n" +
                "Status: " + existingStatus + "\n\n" +
                "This looks like a duplicate. Do you want to send again?"
            );
            ButtonType sendAgainBtn = new ButtonType("Yes, Send Again");
            ButtonType cancelBtn    = new ButtonType("No, Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(sendAgainBtn, cancelBtn);

            Optional<ButtonType> choice = alert.showAndWait();
            if (choice.isPresent() && choice.get() == sendAgainBtn) {
                String newRef = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String newResult = transactionService.proceedDuplicateTransfer(senderAccountId, receiverAccountId, amount, newRef);
                if (newResult.startsWith("SUCCESS")) {
                    showTransferSuccess(amount, newRef);
                    transferAmount.clear(); transferPin.clear(); receiverPhone.clear();
                    receiverNameLabel.setText(""); loadWalletAccounts();
                } else {
                    showMsg(transferMsg, newResult, false);
                }
            } else {
                showMsg(transferMsg, "Transfer cancelled. Your money was not sent.", false);
            }
            return;
        }

        if (result.startsWith("DUPLICATE")) {
            showMsg(transferMsg, "This transaction was already processed. Same reference ID cannot be used twice.", false);
            return;
        }

        if (result.startsWith("SUCCESS")) {
            showTransferSuccess(amount, ref);
            transferAmount.clear(); transferPin.clear(); receiverPhone.clear();
            receiverNameLabel.setText(""); loadWalletAccounts();
        } else {
            showMsg(transferMsg, result, false);
        }
    }

    private void showTransferSuccess(double amount, String ref) {
        showMsg(transferMsg, "Money sent successfully. " + String.format("%.2f", amount) + " RWF transferred.", true);
        refIdLabel.setText(ref);
        refIdBox.setVisible(true);
        refIdBox.setManaged(true);
    }

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
            ? "-fx-text-fill:#005B2A;-fx-font-size:12px;"
            : "-fx-text-fill:#C62828;-fx-font-size:12px;");
        label.setText(msg);
    }
}
