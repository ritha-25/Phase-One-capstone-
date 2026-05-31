package org.atm.igiresystem.lab2.dao;

import org.atm.igiresystem.lab1.models.Loan;
import org.atm.igiresystem.lab2.db.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO implements DAO<Loan> {

    @Override
    public void create(Loan loan) {
        String sql = "INSERT INTO loans (customer_id, account_id, amount, interest_rate, status, purpose) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getCustomerId());
            ps.setInt(2, loan.getAccountId());
            ps.setDouble(3, loan.getAmount());
            ps.setDouble(4, loan.getInterestRate());
            ps.setString(5, loan.getStatus());
            ps.setString(6, loan.getPurpose());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) loan.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("LoanDAO.create error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Loan> findById(int id) {
        String sql = "SELECT * FROM loans WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("LoanDAO.findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Loan> findAll() {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT * FROM loans ORDER BY requested_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.out.println("LoanDAO.findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Loan loan) {
        String sql = "UPDATE loans SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loan.getStatus());
            ps.setInt(2, loan.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("LoanDAO.update error: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM loans WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("LoanDAO.delete error: " + e.getMessage());
        }
    }

    public List<Loan> findByCustomerId(int customerId) {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE customer_id = ? ORDER BY requested_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("LoanDAO.findByCustomerId error: " + e.getMessage());
        }
        return list;
    }

    public List<Loan> findByStatus(String status) {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE status = ? ORDER BY requested_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("LoanDAO.findByStatus error: " + e.getMessage());
        }
        return list;
    }

    private Loan map(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setCustomerId(rs.getInt("customer_id"));
        loan.setAccountId(rs.getInt("account_id"));
        loan.setAmount(rs.getDouble("amount"));
        loan.setInterestRate(rs.getDouble("interest_rate"));
        loan.setStatus(rs.getString("status"));
        loan.setPurpose(rs.getString("purpose"));
        Timestamp ts = rs.getTimestamp("requested_at");
        if (ts != null) loan.setRequestedAt(ts.toLocalDateTime());
        Timestamp ts2 = rs.getTimestamp("updated_at");
        if (ts2 != null) loan.setUpdatedAt(ts2.toLocalDateTime());
        return loan;
    }
}
