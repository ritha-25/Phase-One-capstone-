package org.atm.igiresystem.lab2.dao;

import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab2.db.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAO implements DAO<Transaction> {

    @Override
    public void create(Transaction tx) {
        String sql = """
            INSERT INTO transactions
              (sender_account_id, receiver_account_id, reference_id, amount, transaction_type, transaction_status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getSenderAccountId());
            ps.setInt(2, tx.getReceiverAccountId());
            ps.setString(3, tx.getReferenceId());
            ps.setDouble(4, tx.getAmount());
            ps.setString(5, tx.getTransactionType());
            ps.setString(6, tx.getTransactionStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) tx.setTransactionId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.create error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Transaction> findById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("TransactionDAO.findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Transaction tx) {
        String sql = "UPDATE transactions SET transaction_status = ? WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tx.getTransactionStatus());
            ps.setInt(2, tx.getTransactionId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("TransactionDAO.update error: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("TransactionDAO.delete error: " + e.getMessage());
        }
    }

    public List<Transaction> findByAccountId(int accountId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE sender_account_id = ? OR receiver_account_id = ? ORDER BY created_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.findByAccountId error: " + e.getMessage());
        }
        return list;
    }

    /** Find similar transactions within 5 minutes for retry detection. */
    public List<Transaction> findSimilarRecent(int senderAccountId, int receiverAccountId, double amount) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT * FROM transactions
            WHERE sender_account_id = ? AND receiver_account_id = ? AND amount = ?
              AND created_at >= NOW() - INTERVAL '5 minutes'
            ORDER BY created_at DESC
            """;
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderAccountId);
            ps.setInt(2, receiverAccountId);
            ps.setDouble(3, amount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.findSimilarRecent error: " + e.getMessage());
        }
        return list;
    }

    // ── Custom Queries ────────────────────────────────────────────────────────

    /** Find transactions by type (DEPOSIT, WITHDRAW, TRANSFER). */
    public List<Transaction> findByType(String type) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE transaction_type = ? ORDER BY created_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.findByType error: " + e.getMessage());
        }
        return list;
    }

    /** Find transactions by status (SUCCESS, FAILED, PENDING, CANCELLED). */
    public List<Transaction> findByStatus(String status) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE transaction_status = ? ORDER BY created_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.findByStatus error: " + e.getMessage());
        }
        return list;
    }

    /** Find today's transactions for a daily summary. */
    public List<Transaction> findToday() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE DATE(created_at) = CURRENT_DATE ORDER BY created_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("TransactionDAO.findToday error: " + e.getMessage());
        }
        return list;
    }

    private Transaction map(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction(
            rs.getInt("id"),
            rs.getString("reference_id"),
            rs.getInt("sender_account_id"),
            rs.getInt("receiver_account_id"),
            rs.getDouble("amount"),
            rs.getString("transaction_type"),
            rs.getString("transaction_status")
        );
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) tx.setTimestamp(ts.toLocalDateTime());
        return tx;
    }
}
