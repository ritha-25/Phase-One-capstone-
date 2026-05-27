package org.atm.igiresystem.lab3.services;

import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab1.models.WalletAccount;
import org.atm.igiresystem.lab2.dao.AccountDAO;
import org.atm.igiresystem.lab2.dao.ProcessedRequestDAO;
import org.atm.igiresystem.lab2.dao.TransactionDAO;

import java.util.*;

public class TransactionService {

    private final TransactionDAO       transactionDAO       = new TransactionDAO();
    private final ProcessedRequestDAO  processedRequestDAO  = new ProcessedRequestDAO();
    private final AccountDAO           accountDAO           = new AccountDAO();

    // In-memory Set for fast duplicate detection during session
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
        return "SUCCESS: Deposited " + amount + " RWF.";
    }

    public String withdraw(int accountId, double amount, String referenceId) {
        if (isDuplicate(referenceId)) return "DUPLICATE: Transaction already processed.";
        if (amount <= 0) return "FAILED: Invalid amount.";

        Optional<Account> opt = accountDAO.findById(accountId);
        if (opt.isEmpty()) return "FAILED: Account not found.";

        Account account = opt.get();
        boolean success = account.withdraw(amount, referenceId);
        String status = success ? "SUCCESS" : "FAILED";

        if (success && account instanceof WalletAccount) {
            accountDAO.updateBalance(accountId, account.getBalance());
        }

        saveTransaction(new Transaction(0, referenceId, accountId, 0, amount, "WITHDRAW", status));
        if (success) markProcessed(referenceId);
        return success ? "SUCCESS: Withdrawn " + amount + " RWF (fee: " + WalletAccount.WITHDRAWAL_FEE + " RWF)."
                       : "FAILED: Insufficient balance.";
    }

    /**
     * Transfer money between two wallet accounts.
     * Detects similar recent transactions and prompts retry handling.
     */
    public String transfer(int senderAccountId, int receiverAccountId, double amount, String referenceId) {
        if (isDuplicate(referenceId)) return "DUPLICATE: Transaction already processed.";
        if (amount <= 0) return "FAILED: Invalid amount.";
        if (senderAccountId == receiverAccountId) return "FAILED: Cannot transfer to same account.";

        // Retry detection — check for similar recent transactions
        List<Transaction> similar = transactionDAO.findSimilarRecent(senderAccountId, receiverAccountId, amount);
        if (!similar.isEmpty()) {
            StringBuilder sb = new StringBuilder("RETRY_DETECTED: Possible existing transaction found:\n");
            for (Transaction t : similar) {
                sb.append("  → Transfer ").append(t.getAmount()).append(" RWF | Ref: ")
                  .append(t.getReferenceId()).append(" | Status: ").append(t.getTransactionStatus()).append("\n");
            }
            sb.append("If this is a new transfer, it will proceed. Otherwise use the existing reference ID.");
            System.out.println(sb);
        }

        Optional<Account> senderOpt   = accountDAO.findById(senderAccountId);
        Optional<Account> receiverOpt = accountDAO.findById(receiverAccountId);
        if (senderOpt.isEmpty())   return "FAILED: Sender account not found.";
        if (receiverOpt.isEmpty()) return "FAILED: Receiver account not found.";

        Account sender   = senderOpt.get();
        Account receiver = receiverOpt.get();

        if (!(sender instanceof WalletAccount walletSender)) return "FAILED: Only wallet accounts can transfer.";

        double total = amount + WalletAccount.TRANSFER_FEE;
        if (sender.getBalance() < total) {
            saveTransaction(new Transaction(0, referenceId, senderAccountId, receiverAccountId, amount, "TRANSFER", "FAILED"));
            return "FAILED: Insufficient balance. Need " + total + " RWF (fee: " + WalletAccount.TRANSFER_FEE + " RWF).";
        }

        // Use JDBC transaction for atomicity
        try (var conn = org.atm.igiresystem.lab2.db.Connect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                double newSenderBalance   = sender.getBalance() - total;
                double newReceiverBalance = receiver.getBalance() + amount;

                try (var ps1 = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
                     var ps2 = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
                     var ps3 = conn.prepareStatement(
                         "INSERT INTO transactions (sender_account_id, receiver_account_id, reference_id, amount, transaction_type, transaction_status) VALUES (?, ?, ?, ?, ?, ?)");
                     var ps4 = conn.prepareStatement(
                         "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT DO NOTHING")) {

                    ps1.setDouble(1, newSenderBalance);
                    ps1.setInt(2, senderAccountId);
                    ps1.executeUpdate();

                    ps2.setDouble(1, newReceiverBalance);
                    ps2.setInt(2, receiverAccountId);
                    ps2.executeUpdate();

                    ps3.setInt(1, senderAccountId);
                    ps3.setInt(2, receiverAccountId);
                    ps3.setString(3, referenceId);
                    ps3.setDouble(4, amount);
                    ps3.setString(5, "TRANSFER");
                    ps3.setString(6, "SUCCESS");
                    ps3.executeUpdate();

                    ps4.setString(1, referenceId);
                    ps4.executeUpdate();
                }

                conn.commit();
                sessionProcessedRefs.add(referenceId);
                return "SUCCESS: Transferred " + amount + " RWF to account #" + receiverAccountId +
                       " (fee: " + WalletAccount.TRANSFER_FEE + " RWF).";
            } catch (Exception ex) {
                conn.rollback();
                return "FAILED: Transfer rolled back. " + ex.getMessage();
            }
        } catch (Exception e) {
            return "FAILED: Database error. " + e.getMessage();
        }
    }

    public List<Transaction> getTransactionHistory(int accountId) {
        return transactionDAO.findByAccountId(accountId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
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
