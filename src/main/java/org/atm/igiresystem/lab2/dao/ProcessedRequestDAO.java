package org.atm.igiresystem.lab2.dao;

import org.atm.igiresystem.lab2.db.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages processed_requests table for idempotency protection.
 * Uses String as the entity type (reference ID).
 */
public class ProcessedRequestDAO implements DAO<String> {

    @Override
    public void create(String referenceId) {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT DO NOTHING";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ProcessedRequestDAO.create error: " + e.getMessage());
        }
    }

    @Override
    public Optional<String> findById(int id) {
        String sql = "SELECT reference_id FROM processed_requests WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString("reference_id"));
            }
        } catch (SQLException e) {
            System.err.println("ProcessedRequestDAO.findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<String> findAll() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT reference_id FROM processed_requests ORDER BY processed_at DESC";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString("reference_id"));
        } catch (SQLException e) {
            System.err.println("ProcessedRequestDAO.findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(String referenceId) {
        // Reference IDs are immutable once stored — no update needed.
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM processed_requests WHERE id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ProcessedRequestDAO.delete error: " + e.getMessage());
        }
    }

    /** Check if a reference ID has already been processed. */
    public boolean exists(String referenceId) {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("ProcessedRequestDAO.exists error: " + e.getMessage());
        }
        return false;
    }
}
