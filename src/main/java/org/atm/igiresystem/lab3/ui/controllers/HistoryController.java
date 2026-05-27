package org.atm.igiresystem.lab3.ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab3.services.ReportService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    @FXML private ComboBox<String>                     accountCombo;
    @FXML private TableView<Transaction>               transactionTable;
    @FXML private TableColumn<Transaction, String>     colRef;
    @FXML private TableColumn<Transaction, String>     colType;
    @FXML private TableColumn<Transaction, String>     colAmount;
    @FXML private TableColumn<Transaction, String>     colStatus;
    @FXML private TableColumn<Transaction, String>     colTime;
    @FXML private Label                                messageLabel;

    private final TransactionService transactionService = new TransactionService();
    private final ReportService      reportService      = new ReportService();
    private List<Account>            myAccounts;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colRef.setCellValueFactory(   d -> new SimpleStringProperty(d.getValue().getReferenceId()));
        colType.setCellValueFactory(  d -> new SimpleStringProperty(d.getValue().getTransactionType()));
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%.2f", d.getValue().getAmount())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransactionStatus()));
        colTime.setCellValueFactory(  d -> new SimpleStringProperty(
            d.getValue().getTimestamp() != null
                ? d.getValue().getTimestamp().toString().replace("T", "  ").substring(0, 19)
                : ""));

        loadAccounts();
    }

    private void loadAccounts() {
        if (Session.getCustomer() == null) return;
        myAccounts = new org.atm.igiresystem.lab3.services.AccountService()
            .getCustomerAccounts(Session.getCustomer().getId());
        accountCombo.getItems().clear();
        for (Account acc : myAccounts) {
            accountCombo.getItems().add(acc.getAccountType() + " #" + acc.getId());
        }
        if (!myAccounts.isEmpty()) accountCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onAccountSelected() {
        loadHistory();
    }

    @FXML
    public void loadHistory() {
        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0 || myAccounts == null || myAccounts.isEmpty()) {
            messageLabel.setText("Please select an account.");
            return;
        }
        List<Transaction> list = transactionService.getTransactionHistory(myAccounts.get(idx).getId());
        transactionTable.setItems(FXCollections.observableArrayList(list));
        messageLabel.setStyle("-fx-text-fill:#1E8E3E;-fx-font-size:12px;");
        messageLabel.setText(list.isEmpty() ? "No transactions yet." : list.size() + " transactions loaded.");
    }

    @FXML
    private void exportCSV() {
        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0 || myAccounts == null || myAccounts.isEmpty()) {
            messageLabel.setText("Please select an account first.");
            return;
        }
        Account acc  = myAccounts.get(idx);
        String  path = "statement_" + Session.getCustomer().getPhoneNumber() + "_acc" + acc.getId() + ".csv";
        reportService.exportTransactionsToCSV(acc.getId(), path);
        messageLabel.setStyle("-fx-text-fill:#1E8E3E;-fx-font-size:12px;");
        messageLabel.setText("✓ Exported to: " + path);
    }
}
