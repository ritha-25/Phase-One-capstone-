package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Loan;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.LoanService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LoanController implements Initializable {

    @FXML private ComboBox<String> accountCombo;
    @FXML private TextField        amountField;
    @FXML private TextField        purposeField;
    @FXML private PasswordField    pinField;
    @FXML private Label            messageLabel;
    @FXML private VBox             loansList;

    private final LoanService    loanService    = new LoanService();
    private final AccountService accountService = new AccountService();

    private List<Account> myAccounts = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadAccounts();
        loadMyLoans();
    }

    private void loadAccounts() {
        if (Session.getCustomer() == null) return;
        myAccounts = accountService.getCustomerAccounts(Session.getCustomer().getId());
        accountCombo.getItems().clear();
        for (Account acc : myAccounts) {
            String name = "WALLET".equals(acc.getAccountType()) ? "Main Wallet" : "Savings Account";
            accountCombo.getItems().add(name + "  (" + String.format("%.2f", acc.getBalance()) + " RWF)");
        }
        if (!myAccounts.isEmpty()) accountCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRequestLoan() {
        if (Session.getCustomer() == null) return;

        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showMsg("Please select an account to receive the loan.", false); return; }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) { showMsg("Please enter a loan amount.", false); return; }

        double amount;
        try {
            amount = Double.parseDouble(amountText.replace(",", ""));
        } catch (NumberFormatException e) {
            showMsg("Invalid amount.", false);
            return;
        }

        String purpose = purposeField.getText().trim();
        if (purpose.isEmpty()) { showMsg("Please describe the purpose of this loan.", false); return; }

        String pin = pinField.getText().trim();
        if (pin.isEmpty()) { showMsg("Please enter your PIN to confirm.", false); return; }

        if (!accountService.validatePinByCustomer(Session.getCustomer().getId(), pin)) {
            showMsg("Incorrect PIN.", false);
            return;
        }

        int accountId = myAccounts.get(idx).getId();
        String result = loanService.requestLoan(Session.getCustomer().getId(), accountId, amount, purpose);
        showMsg(result, result.startsWith("SUCCESS"));

        if (result.startsWith("SUCCESS")) {
            amountField.clear(); purposeField.clear(); pinField.clear();
            loadAccounts(); loadMyLoans();
        }
    }

    @FXML
    public void loadMyLoans() {
        loansList.getChildren().clear();
        if (Session.getCustomer() == null) return;

        List<Loan> loans = loanService.getCustomerLoans(Session.getCustomer().getId());

        if (loans.isEmpty()) {
            Label empty = new Label("No loan requests yet.");
            empty.setStyle("-fx-text-fill:#9CA3AF;-fx-font-size:13px;");
            loansList.getChildren().add(empty);
            return;
        }

        for (Loan loan : loans) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;-fx-padding:14 16;" +
                          "-fx-border-color:#E5E7EB;-fx-border-radius:10;-fx-border-width:1;");

            HBox topRow = new HBox();
            topRow.setSpacing(0);

            Label amountLabel = new Label(String.format("%.2f RWF", loan.getAmount()));
            amountLabel.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#005B2A;");
            HBox.setHgrow(amountLabel, javafx.scene.layout.Priority.ALWAYS);

            String statusColor;
            switch (loan.getStatus()) {
                case "APPROVED": statusColor = "#005B2A"; break;
                case "REJECTED": statusColor = "#C62828"; break;
                case "REPAID":   statusColor = "#6B7280"; break;
                default:         statusColor = "#F5A623"; break;
            }
            Label statusLabel = new Label(loan.getStatus());
            statusLabel.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + statusColor + ";");
            topRow.getChildren().addAll(amountLabel, statusLabel);

            Label purposeLabel = new Label("Purpose: " + loan.getPurpose());
            purposeLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#4B5563;");

            Label repayLabel = new Label("Total repayable: " + String.format("%.2f", loan.getTotalRepayable()) + " RWF  (10% interest)");
            repayLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#9CA3AF;");

            String dateStr = loan.getRequestedAt() != null ? loan.getRequestedAt().toString().substring(0, 10) : "";
            Label dateLabel = new Label("Requested: " + dateStr);
            dateLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#9CA3AF;");

            card.getChildren().addAll(topRow, purposeLabel, repayLabel, dateLabel);

            if ("APPROVED".equals(loan.getStatus())) {
                Button repayBtn = new Button("Repay Loan — " + String.format("%.2f", loan.getTotalRepayable()) + " RWF");
                repayBtn.setStyle("-fx-background-color:#F5A623;-fx-text-fill:white;-fx-font-weight:bold;" +
                                  "-fx-padding:8 16;-fx-background-radius:6;-fx-cursor:hand;");
                final int loanId      = loan.getId();
                final int loanAccount = loan.getAccountId();
                repayBtn.setOnAction(e -> handleRepayLoan(loanId, loanAccount));
                card.getChildren().add(repayBtn);
            }

            loansList.getChildren().add(card);
        }
    }

    private void handleRepayLoan(int loanId, int accountId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Loan Repayment");
        confirm.setHeaderText("Repay this loan?");
        confirm.setContentText("The full repayment amount will be deducted from your account.");

        java.util.Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            String result = loanService.repayLoan(loanId, accountId);
            showMsg(result, result.startsWith("SUCCESS"));
            if (result.startsWith("SUCCESS")) {
                loadAccounts();
                loadMyLoans();
            }
        }
    }

    private void showMsg(String msg, boolean success) {
        messageLabel.setStyle(success
            ? "-fx-text-fill:#005B2A;-fx-font-size:12px;"
            : "-fx-text-fill:#C62828;-fx-font-size:12px;");
        messageLabel.setText(msg);
    }
}
