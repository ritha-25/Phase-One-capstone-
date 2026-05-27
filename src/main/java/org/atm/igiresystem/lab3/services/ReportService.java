package org.atm.igiresystem.lab3.services;

import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab2.dao.TransactionDAO;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
            System.err.println("Export failed: " + e.getMessage());
        }
    }

    public void printDailySummary() {
        List<Transaction> all = transactionDAO.findAll();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        List<Transaction> todayTx = all.stream()
            .filter(tx -> tx.getTimestamp().toLocalDate().toString().equals(today))
            .collect(Collectors.toList());

        double totalDeposits  = todayTx.stream().filter(t -> "DEPOSIT".equals(t.getTransactionType())).mapToDouble(Transaction::getAmount).sum();
        double totalWithdraws = todayTx.stream().filter(t -> "WITHDRAW".equals(t.getTransactionType())).mapToDouble(Transaction::getAmount).sum();
        double totalTransfers = todayTx.stream().filter(t -> "TRANSFER".equals(t.getTransactionType())).mapToDouble(Transaction::getAmount).sum();

        System.out.println("=== Daily Summary: " + today + " ===");
        System.out.println("Total Transactions : " + todayTx.size());
        System.out.println("Total Deposits     : " + totalDeposits + " RWF");
        System.out.println("Total Withdrawals  : " + totalWithdraws + " RWF");
        System.out.println("Total Transfers    : " + totalTransfers + " RWF");
    }
}
