package org.atm.igiresystem.lab3.ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab1.models.Loan;
import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab2.dao.CustomerDAO;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.LoanService;
import org.atm.igiresystem.lab3.services.ReportService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.util.List;

public class AdminController {

    @FXML private TableView<ObservableList<String>>           dataTable;
    @FXML private TableColumn<ObservableList<String>, String> col1;
    @FXML private TableColumn<ObservableList<String>, String> col2;
    @FXML private TableColumn<ObservableList<String>, String> col3;
    @FXML private TableColumn<ObservableList<String>, String> col4;
    @FXML private TableColumn<ObservableList<String>, String> col5;
    @FXML private TextField deleteIdField;
    @FXML private TextField loanIdField;
    @FXML private Label     messageLabel;
    @FXML private VBox      summaryBox;

    private final CustomerDAO        customerDAO        = new CustomerDAO();
    private final TransactionService transactionService = new TransactionService();
    private final AccountService     accountService     = new AccountService();
    private final ReportService      reportService      = new ReportService();
    private final LoanService        loanService        = new LoanService();

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
        hideSummary();
        List<Customer> customers = customerDAO.findAll();
        col1.setText("ID");    col2.setText("Name");
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
        hideSummary();
        List<Transaction> txList = transactionService.getAllTransactions();
        col1.setText("ID");   col2.setText("Reference");
        col3.setText("Type"); col4.setText("Amount (RWF)"); col5.setText("Status");

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
    private void loadLoans() {
        hideSummary();
        List<Loan> loans = loanService.getAllLoans();
        col1.setText("ID");      col2.setText("Amount (RWF)");
        col3.setText("Purpose"); col4.setText("Status"); col5.setText("Date");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (Loan loan : loans) {
            data.add(FXCollections.observableArrayList(
                String.valueOf(loan.getId()),
                String.format("%.2f", loan.getAmount()),
                loan.getPurpose(),
                loan.getStatus(),
                loan.getRequestedAt() != null ? loan.getRequestedAt().toString().substring(0, 10) : "—"
            ));
        }
        dataTable.setItems(data);
        showMsg("Loaded " + loans.size() + " loan requests.", true);
    }

    @FXML
    private void showDailySummary() {
        dataTable.setVisible(false);
        dataTable.setManaged(false);
        summaryBox.setVisible(true);
        summaryBox.setManaged(true);

        String text = reportService.getDailySummaryText();
        summaryBox.getChildren().clear();

        Label title = new Label("Daily Transaction Summary");
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#005B2A;-fx-padding:0 0 8 0;");
        summaryBox.getChildren().add(title);

        for (String line : text.split("\n")) {
            Label lbl = new Label(line);
            lbl.setStyle("-fx-font-size:14px;-fx-text-fill:#374151;-fx-padding:4 0;");
            summaryBox.getChildren().add(lbl);
        }
        showMsg("Daily summary loaded.", true);
    }

    @FXML
    private void exportAll() {
        reportService.exportAllTransactionsToCSV("all_transactions.csv");
        showMsg("Exported to all_transactions.csv", true);
    }

    @FXML
    private void deleteAccount() {
        String idText = deleteIdField.getText().trim();
        if (idText.isEmpty()) { showMsg("Enter an account ID.", false); return; }
        try {
            int id = Integer.parseInt(idText);
            accountService.deleteAccount(id);
            deleteIdField.clear();
            showMsg("Account #" + id + " deleted.", true);
        } catch (NumberFormatException e) {
            showMsg("Invalid account ID.", false);
        }
    }

    @FXML
    private void approveLoan() {
        String idText = loanIdField.getText().trim();
        if (idText.isEmpty()) { showMsg("Enter a loan ID.", false); return; }
        try {
            int id = Integer.parseInt(idText);
            String result = loanService.approveLoan(id);
            showMsg(result, result.startsWith("SUCCESS"));
            if (result.startsWith("SUCCESS")) { loanIdField.clear(); loadLoans(); }
        } catch (NumberFormatException e) {
            showMsg("Invalid loan ID.", false);
        }
    }

    @FXML
    private void rejectLoan() {
        String idText = loanIdField.getText().trim();
        if (idText.isEmpty()) { showMsg("Enter a loan ID.", false); return; }
        try {
            int id = Integer.parseInt(idText);
            String result = loanService.rejectLoan(id);
            showMsg(result, result.startsWith("SUCCESS"));
            if (result.startsWith("SUCCESS")) { loanIdField.clear(); loadLoans(); }
        } catch (NumberFormatException e) {
            showMsg("Invalid loan ID.", false);
        }
    }

    @FXML
    private void unlockCustomer() {
        String idText = deleteIdField.getText().trim();
        if (idText.isEmpty()) { showMsg("Enter a customer ID.", false); return; }
        try {
            int id = Integer.parseInt(idText);
            accountService.unlockCustomer(id);
            deleteIdField.clear();
            showMsg("Customer #" + id + " unlocked.", true);
        } catch (NumberFormatException e) {
            showMsg("Invalid ID.", false);
        }
    }

    private void hideSummary() {
        summaryBox.setVisible(false);
        summaryBox.setManaged(false);
        dataTable.setVisible(true);
        dataTable.setManaged(true);
    }

    private void showMsg(String msg, boolean success) {
        messageLabel.setStyle(success
            ? "-fx-text-fill:#005B2A;-fx-font-size:12px;"
            : "-fx-text-fill:#DC2626;-fx-font-size:12px;");
        messageLabel.setText(msg);
    }
}
