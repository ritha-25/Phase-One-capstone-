package org.atm.igiresystem.lab2.dao;

import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.SavingsAccount;
import org.atm.igiresystem.lab1.models.WalletAccount;
import org.atm.igiresystem.lab2.db.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAO implements DAO<Account> {

    @Override
    public void create(Account account) {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, account.getCustomerId());
            ps.setString(2, account.getAccountType());
            ps.setDouble(3, account.getBalance());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) account.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("AccountDAO.create error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Account> findById(int id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AccountDAO.findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY created_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("AccountDAO.findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Account account) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, account.getBalance());
            ps.setInt(2, account.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AccountDAO.update error: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AccountDAO.delete error: " + e.getMessage());
        }
    }

    public List<Account> findByCustomerId(int customerId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AccountDAO.findByCustomerId error: " + e.getMessage());
        }
        return list;
    }

    public void updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AccountDAO.updateBalance error: " + e.getMessage());
        }
    }

    // ── Custom Queries ────────────────────────────────────────────────────────

    /** Find all accounts of a specific type (WALLET or SAVINGS). */
    public List<Account> findByType(String accountType) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE account_type = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AccountDAO.findByType error: " + e.getMessage());
        }
        return list;
    }

    /** Find accounts with balance above a threshold. */
    public List<Account> findByMinBalance(double minBalance) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE balance >= ? ORDER BY balance DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, minBalance);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AccountDAO.findByMinBalance error: " + e.getMessage());
        }
        return list;
    }

    /** Find wallet account for a customer (first wallet found). */
    public Optional<Account> findWalletByCustomerId(int customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ? AND account_type = 'WALLET' LIMIT 1";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AccountDAO.findWalletByCustomerId error: " + e.getMessage());
        }
        return Optional.empty();
    }

    private Account map(ResultSet rs) throws SQLException {
        int    id         = rs.getInt("id");
        int    customerId = rs.getInt("customer_id");
        String type       = rs.getString("account_type");
        double balance    = rs.getDouble("balance");

        Account account = "SAVINGS".equalsIgnoreCase(type)
            ? new SavingsAccount(id, customerId, balance)
            : new WalletAccount(id, customerId, balance);

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) account.setCreatedAt(ts.toLocalDateTime());
        return account;
    }
}
