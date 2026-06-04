package org.atm.igiresystem.lab2.dao;

import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab2.db.Connect;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO implements DAO<Customer> {

    public static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return pin;
        }
    }

    @Override
    public void create(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone_number, pin, pin_hash, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setNull(4, Types.VARCHAR);
            ps.setString(5, hashPin(customer.getPin()));
            ps.setInt(6, customer.getUserId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) customer.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("CustomerDAO.create error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Customer> findById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("CustomerDAO.findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY created_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.out.println("CustomerDAO.findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setInt(4, customer.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("CustomerDAO.update error: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("CustomerDAO.delete error: " + e.getMessage());
        }
    }

    public Optional<Customer> findByUserId(int userId) {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("CustomerDAO.findByUserId error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone_number = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("CustomerDAO.findByPhone error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updatePin(int customerId, String newPin) {
        String sql = "UPDATE customers SET pin = NULL, pin_hash = ?, failed_attempts = 0, locked = FALSE WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashPin(newPin));
            ps.setInt(2, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("CustomerDAO.updatePin error: " + e.getMessage());
        }
    }

    public void incrementFailedAttempts(int customerId) {
        String sql = "UPDATE customers SET failed_attempts = failed_attempts + 1 WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("CustomerDAO.incrementFailedAttempts error: " + e.getMessage());
        }
    }

    public void lockAccount(int customerId) {
        String sql = "UPDATE customers SET locked = TRUE WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("CustomerDAO.lockAccount error: " + e.getMessage());
        }
    }

    public void unlockAccount(int customerId) {
        String sql = "UPDATE customers SET locked = FALSE, failed_attempts = 0 WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("CustomerDAO.unlockAccount error: " + e.getMessage());
        }
    }

    public void resetFailedAttempts(int customerId) {
        String sql = "UPDATE customers SET failed_attempts = 0 WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("CustomerDAO.resetFailedAttempts error: " + e.getMessage());
        }
    }

    public List<Customer> searchByName(String name) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE LOWER(full_name) LIKE LOWER(?)";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("CustomerDAO.searchByName error: " + e.getMessage());
        }
        return list;
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhoneNumber(rs.getString("phone_number"));
        c.setUserId(rs.getInt("user_id"));

        String pinHash = null;
        try { pinHash = rs.getString("pin_hash"); } catch (Exception ignored) {}
        String plainPin = null;
        try { plainPin = rs.getString("pin"); } catch (Exception ignored) {}

        if (pinHash != null && !pinHash.isEmpty()) {
            c.setPinHash(pinHash);
        } else if (plainPin != null && plainPin.length() == 4) {
            c.setPin(plainPin);
        }

        try { c.setFailedPinAttempts(rs.getInt("failed_attempts")); } catch (Exception ignored) {}
        try { c.setLocked(rs.getBoolean("locked")); } catch (Exception ignored) {}

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
