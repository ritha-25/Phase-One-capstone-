package org.atm.igiresystem.lab3.services;

import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab2.dao.TransactionDAO;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public void exportTransactionsToCSV(int accountId, String filePath) {
        List<Transaction> transactions = transactionDAO.findByAccountId(accountId);
        writeCSV(transactions, filePath);
    }

    public void exportAllTransactionsToCSV(String filePath) {
        List<Transaction> transactions = transactionDAO.findAll();
        writeCSV(transactions, filePath);
    }

    private void writeCSV(List<Transaction> transactions, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("ID,ReferenceID,SenderAccountID,ReceiverAccountID,Amount,Type,Status,Timestamp\n");
            for (Transaction tx : transactions) {
                writer.write(tx.getTransactionId() + "," +
                             tx.getReferenceId() + "," +
                             tx.getSenderAccountId() + "," +
                             tx.getReceiverAccountId() + "," +
                             tx.getAmount() + "," +
                             tx.getTransactionType() + "," +
                             tx.getTransactionStatus() + "," +
                             tx.getTimestamp() + "\n");
            }
            System.out.println("Report exported to: " + filePath);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    public String getDailySummaryText() {
        List<Transaction> all = transactionDAO.findAll();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        List<Transaction> todayTx = new ArrayList<>();
        for (Transaction tx : all) {
            if (tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().toString().equals(today)) {
                todayTx.add(tx);
            }
        }

        double totalDeposits  = 0;
        double totalWithdraws = 0;
        double totalTransfers = 0;

        for (Transaction t : todayTx) {
            if ("DEPOSIT".equals(t.getTransactionType()))  totalDeposits  += t.getAmount();
            if ("WITHDRAW".equals(t.getTransactionType())) totalWithdraws += t.getAmount();
            if ("TRANSFER".equals(t.getTransactionType())) totalTransfers += t.getAmount();
        }

        return "Daily Summary — " + today + "\n" +
               "Total Transactions : " + todayTx.size() + "\n" +
               "Total Deposits     : " + String.format("%.2f", totalDeposits)  + " RWF\n" +
               "Total Withdrawals  : " + String.format("%.2f", totalWithdraws) + " RWF\n" +
               "Total Transfers    : " + String.format("%.2f", totalTransfers) + " RWF";
    }

    public void printDailySummary() {
        System.out.println(getDailySummaryText());
    }
}
