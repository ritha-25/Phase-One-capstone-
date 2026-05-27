package org.atm.igiresystem.lab3.ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab2.dao.CustomerDAO;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.ReportService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.util.List;

public class AdminController {

    @FXML private TableView<ObservableList<String>>        dataTable;
    @FXML private TableColumn<ObservableList<String>, String> col1;
    @FXML private TableColumn<ObservableList<String>, String> col2;
    @FXML private TableColumn<ObservableList<String>, String> col3;
    @FXML private TableColumn<ObservableList<String>, String> col4;
    @FXML private TableColumn<ObservableList<String>, String> col5;
    @FXML private TextField deleteIdField;
    @FXML private Label     messageLabel;

    private final CustomerDAO        customerDAO        = new CustomerDAO();
    private final TransactionService transactionService = new TransactionService();
    private final AccountService     accountService     = new AccountService();
    private final ReportService      reportService      = new ReportService();

    @FXML
    public void initialize() {
        col1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        col2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        col3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        col4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        col5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
    }

    @FXML
    private void loadCustomers() {
        List<Customer> customers = customerDAO.findAll();
        col1.setText("ID");   col2.setText("Name");
        col3.setText("Phone"); col4.setText("Email"); col5.setText("Registered");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (Customer c : customers) {
            data.add(FXCollections.observableArrayList(
                String.valueOf(c.getId()),
                c.getFullName(),
                c.getPhoneNumber(),
                c.getEmail() != null ? c.getEmail() : "—",
                c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 10) : "—"
            ));
        }
        dataTable.setItems(data);
        showMsg("Loaded " + customers.size() + " customers.", true);
    }

    @FXML
    private void loadTransactions() {
        List<Transaction> txList = transactionService.getAllTransactions();
        col1.setText("ID");     col2.setText("Reference ID");
        col3.setText("Type");   col4.setText("Amount (RWF)"); col5.setText("Status");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (Transaction tx : txList) {
            data.add(FXCollections.observableArrayList(
                String.valueOf(tx.getTransactionId()),
                tx.getReferenceId(),
                tx.getTransactionType(),
                String.format("%.2f", tx.getAmount()),
                tx.getTransactionStatus()
            ));
        }
        dataTable.setItems(data);
        showMsg("Loaded " + txList.size() + " transactions.", true);
    }

    @FXML
    private void showDailySummary() {
        reportService.printDailySummary();
        showMsg("Daily summary printed to console.", true);
    }

    @FXML
    private void exportAll() {
        reportService.exportAllTransactionsToCSV("all_transactions.csv");
        showMsg("✓ Exported to all_transactions.csv", true);
    }

    @FXML
    private void deleteAccount() {
        String idText = deleteIdField.getText().trim();
        if (idText.isEmpty()) { showMsg("Enter an account ID.", false); return; }
        try {
            int id = Integer.parseInt(idText);
            accountService.deleteAccount(id);
            deleteIdField.clear();
            showMsg("✓ Account #" + id + " deleted.", true);
        } catch (NumberFormatException e) {
            showMsg("Invalid account ID.", false);
        }
    }

    private void showMsg(String msg, boolean success) {
        messageLabel.setStyle(success
            ? "-fx-text-fill:#1E8E3E;-fx-font-size:12px;"
            : "-fx-text-fill:#DC2626;-fx-font-size:12px;");
        messageLabel.setText(msg);
    }
}
