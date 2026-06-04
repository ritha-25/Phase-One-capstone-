package org.atm.igiresystem.lab3.services;

import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab1.models.WalletAccount;
import org.atm.igiresystem.lab2.dao.AccountDAO;
import org.atm.igiresystem.lab2.dao.ProcessedRequestDAO;
import org.atm.igiresystem.lab2.dao.TransactionDAO;
import org.atm.igiresystem.lab2.db.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TransactionService {

    private final TransactionDAO      transactionDAO      = new TransactionDAO();
    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();
    private final AccountDAO          accountDAO          = new AccountDAO();

    private final Set<String> sessionProcessedRefs = new HashSet<>();

    public String deposit(int accountId, double amount, String referenceId) {
        if (isDuplicate(referenceId)) return "DUPLICATE: Transaction already processed.";
        if (amount <= 0) return "FAILED: Invalid amount.";

        Optional<Account> opt = accountDAO.findById(accountId);
        if (opt.isEmpty()) return "FAILED: Account not found.";

        Account account = opt.get();
        boolean success = account.deposit(amount);
        if (!success) return "FAILED: Deposit failed.";

        accountDAO.updateBalance(accountId, account.getBalance());
        saveTransaction(new Transaction(0, referenceId, accountId, accountId, amount, "DEPOSIT", "SUCCESS"));
        markProcessed(referenceId);
        return "SUCCESS: Deposited " + String.format("%.2f", amount) + " RWF.";
    }

    public String withdraw(int accountId, double amount, String referenceId) {
        if (isDuplicate(referenceId)) return "DUPLICATE: Transaction already processed.";
        if (amount <= 0) return "FAILED: Invalid amount.";

        Optional<Account> opt = accountDAO.findById(accountId);
        if (opt.isEmpty()) return "FAILED: Account not found.";

        Account account = opt.get();
        boolean success = account.withdraw(amount, referenceId);
        String status = success ? "SUCCESS" : "FAILED";

        if (success) {
            accountDAO.updateBalance(accountId, account.getBalance());
        }

        saveTransaction(new Transaction(0, referenceId, accountId, 0, amount, "WITHDRAW", status));
        if (success) markProcessed(referenceId);

        if (!success) return "FAILED: Insufficient balance.";
        if (account instanceof WalletAccount) {
            return "SUCCESS: Withdrawn " + String.format("%.2f", amount) + " RWF (fee: " + WalletAccount.WITHDRAWAL_FEE + " RWF).";
        }
        return "SUCCESS: Withdrawal request submitted. Funds available after 48 hours.";
    }

    public String transfer(int senderAccountId, int receiverAccountId, double amount, String referenceId) {
        if (isDuplicate(referenceId)) return "DUPLICATE: Transaction already processed.";
        if (amount <= 0) return "FAILED: Invalid amount.";
        if (senderAccountId == receiverAccountId) return "FAILED: Cannot transfer to the same account.";

        Optional<Account> senderOpt   = accountDAO.findById(senderAccountId);
        Optional<Account> receiverOpt = accountDAO.findById(receiverAccountId);
        if (senderOpt.isEmpty())   return "FAILED: Sender account not found.";
        if (receiverOpt.isEmpty()) return "FAILED: Receiver account not found.";

        Account sender   = senderOpt.get();
        Account receiver = receiverOpt.get();

        boolean sameCustomer = sender.getCustomerId() == receiver.getCustomerId();

        double fee = sameCustomer ? 0.0 : WalletAccount.TRANSFER_FEE;
        double total = amount + fee;

        if (sender.getBalance() < total) {
            saveTransaction(new Transaction(0, referenceId, senderAccountId, receiverAccountId, amount, "TRANSFER", "FAILED"));
            return "FAILED: Insufficient balance. Need " + String.format("%.2f", total) + " RWF" +
                   (fee > 0 ? " (includes " + fee + " RWF fee)" : "") + ".";
        }

        if (!sameCustomer) {
            List<Transaction> similar = transactionDAO.findSimilarRecent(senderAccountId, receiverAccountId, amount);
            if (!similar.isEmpty()) {
                Transaction existing = similar.get(0);
                return "RETRY_DETECTED|" + existing.getReferenceId() + "|" + existing.getAmount() + "|" + existing.getTransactionStatus();
            }
        }

        try {
            Connection conn = Connect.getConnection();
            conn.setAutoCommit(false);
            try {
                double newSenderBal   = sender.getBalance() - total;
                double newReceiverBal = receiver.getBalance() + amount;

                PreparedStatement ps1 = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
                ps1.setDouble(1, newSenderBal);
                ps1.setInt(2, senderAccountId);
                ps1.executeUpdate();
                ps1.close();

                PreparedStatement ps2 = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
                ps2.setDouble(1, newReceiverBal);
                ps2.setInt(2, receiverAccountId);
                ps2.executeUpdate();
                ps2.close();

                PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO transactions (sender_account_id, receiver_account_id, reference_id, amount, transaction_type, transaction_status) VALUES (?, ?, ?, ?, ?, ?)");
                ps3.setInt(1, senderAccountId);
                ps3.setInt(2, receiverAccountId);
                ps3.setString(3, referenceId);
                ps3.setDouble(4, amount);
                ps3.setString(5, "TRANSFER");
                ps3.setString(6, "SUCCESS");
                ps3.executeUpdate();
                ps3.close();

                PreparedStatement ps4 = conn.prepareStatement(
                    "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT DO NOTHING");
                ps4.setString(1, referenceId);
                ps4.executeUpdate();
                ps4.close();

                conn.commit();
                conn.close();
                sessionProcessedRefs.add(referenceId);

                String msg = "SUCCESS: Transferred " + String.format("%.2f", amount) + " RWF";
                if (fee > 0) msg += " (fee: " + fee + " RWF)";
                msg += ".";
                return msg;

            } catch (Exception ex) {
                conn.rollback();
                conn.close();
                return "FAILED: Transfer error. " + ex.getMessage();
            }
        } catch (Exception e) {
            return "FAILED: Database error. " + e.getMessage();
        }
    }

    public String proceedDuplicateTransfer(int senderAccountId, int receiverAccountId, double amount, String newReferenceId) {
        return transfer(senderAccountId, receiverAccountId, amount, newReferenceId);
    }

    public List<Transaction> getTransactionHistory(int accountId) {
        return transactionDAO.findByAccountId(accountId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }

    public Optional<Transaction> findByReference(String referenceId) {
        List<Transaction> all = transactionDAO.findAll();
        for (Transaction t : all) {
            if (referenceId.equals(t.getReferenceId())) return Optional.of(t);
        }
        return Optional.empty();
    }

    private boolean isDuplicate(String referenceId) {
        return sessionProcessedRefs.contains(referenceId) || processedRequestDAO.exists(referenceId);
    }

    private void markProcessed(String referenceId) {
        sessionProcessedRefs.add(referenceId);
        processedRequestDAO.create(referenceId);
    }

    private void saveTransaction(Transaction tx) {
        transactionDAO.create(tx);
    }
}
