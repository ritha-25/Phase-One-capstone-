package org.atm.igiresystem.lab2.dao;

import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab2.db.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO implements DAO<Customer> {

    @Override
    public void create(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone_number, pin, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getPin());
            ps.setInt(5, customer.getUserId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) customer.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("CustomerDAO.create error: " + e.getMessage());
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
            System.err.println("CustomerDAO.findById error: " + e.getMessage());
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
            System.err.println("CustomerDAO.findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ?, pin = ? WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getPin());
            ps.setInt(5, customer.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("CustomerDAO.update error: " + e.getMessage());
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
            System.err.println("CustomerDAO.delete error: " + e.getMessage());
        }
    }

    // ── Custom Queries ────────────────────────────────────────────────────────

    /** Find customer by their linked user ID. */
    public Optional<Customer> findByUserId(int userId) {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerDAO.findByUserId error: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Find customer by phone number — used for MoMo-style PIN login. */
    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone_number = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerDAO.findByPhone error: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Update only the PIN for a customer. */
    public void updatePin(int customerId, String newPin) {
        String sql = "UPDATE customers SET pin = ? WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPin);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("CustomerDAO.updatePin error: " + e.getMessage());
        }
    }

    /** Search customers by name (partial match). */
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
            System.err.println("CustomerDAO.searchByName error: " + e.getMessage());
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
        String pin = rs.getString("pin");
        if (pin != null && pin.length() == 4) c.setPin(pin);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
