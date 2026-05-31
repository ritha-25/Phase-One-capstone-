package org.atm.igiresystem.lab2.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaSetup {

    public static void createTables() {
        String sql1 = "CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, role VARCHAR(20) DEFAULT 'USER', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String sql2 = "CREATE TABLE IF NOT EXISTS customers (id SERIAL PRIMARY KEY, full_name VARCHAR(100) NOT NULL, email VARCHAR(100), phone_number VARCHAR(20) UNIQUE NOT NULL, pin VARCHAR(4), pin_hash VARCHAR(64), user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, failed_attempts INTEGER DEFAULT 0, locked BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String sql3 = "CREATE TABLE IF NOT EXISTS accounts (id SERIAL PRIMARY KEY, customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE, account_type VARCHAR(20) NOT NULL, balance DECIMAL(15,2) DEFAULT 0.00, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String sql4 = "CREATE TABLE IF NOT EXISTS transactions (id SERIAL PRIMARY KEY, sender_account_id INTEGER REFERENCES accounts(id), receiver_account_id INTEGER REFERENCES accounts(id), reference_id VARCHAR(100) UNIQUE NOT NULL, amount DECIMAL(15,2) NOT NULL, transaction_type VARCHAR(20) NOT NULL, transaction_status VARCHAR(20) DEFAULT 'PENDING', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String sql5 = "CREATE TABLE IF NOT EXISTS processed_requests (id SERIAL PRIMARY KEY, reference_id VARCHAR(100) UNIQUE NOT NULL, processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String sql6 = "CREATE TABLE IF NOT EXISTS loans (id SERIAL PRIMARY KEY, customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE, account_id INTEGER REFERENCES accounts(id), amount DECIMAL(15,2) NOT NULL, interest_rate DECIMAL(5,2) DEFAULT 10.00, status VARCHAR(20) DEFAULT 'PENDING', purpose VARCHAR(255), requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String idx1 = "CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone_number)";
        String idx2 = "CREATE INDEX IF NOT EXISTS idx_accounts_customer ON accounts(customer_id)";
        String idx3 = "CREATE INDEX IF NOT EXISTS idx_transactions_reference ON transactions(reference_id)";
        String idx4 = "CREATE INDEX IF NOT EXISTS idx_processed_reference ON processed_requests(reference_id)";

        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
            stmt.execute(sql4);
            stmt.execute(sql5);
            stmt.execute(sql6);
            stmt.execute(idx1);
            stmt.execute(idx2);
            stmt.execute(idx3);
            stmt.execute(idx4);

            try { stmt.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS pin_hash VARCHAR(64)"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS failed_attempts INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS locked BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE customers ALTER COLUMN email DROP NOT NULL"); } catch (Exception ignored) {}

            seedAdminUser(conn);

            System.out.println("Tables ready.");

        } catch (SQLException e) {
            System.out.println("SchemaSetup error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        createTables();
    }

    private static void seedAdminUser(Connection conn) {
        try {
            java.sql.PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            check.setString(1, "0780000000");
            java.sql.ResultSet rs = check.executeQuery();
            if (rs.next()) { rs.close(); check.close(); return; }
            rs.close();
            check.close();

            java.sql.PreparedStatement insertUser = conn.prepareStatement(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            insertUser.setString(1, "0780000000");
            insertUser.setString(2, "admin_igire");
            insertUser.setString(3, "ADMIN");
            insertUser.executeUpdate();

            java.sql.ResultSet keys = insertUser.getGeneratedKeys();
            int userId = 0;
            if (keys.next()) userId = keys.getInt(1);
            keys.close();
            insertUser.close();

            String pinHash = org.atm.igiresystem.lab2.dao.CustomerDAO.hashPin("1234");
            java.sql.PreparedStatement insertCustomer = conn.prepareStatement(
                "INSERT INTO customers (full_name, email, phone_number, pin_hash, user_id) VALUES (?, ?, ?, ?, ?)");
            insertCustomer.setString(1, "IgirePay Admin");
            insertCustomer.setString(2, "admin@igirepay.rw");
            insertCustomer.setString(3, "0780000000");
            insertCustomer.setString(4, pinHash);
            insertCustomer.setInt(5, userId);
            insertCustomer.executeUpdate();
            insertCustomer.close();

            System.out.println("Admin created Phone: 0780000000  PIN: 1234");
        } catch (Exception e) {
            System.out.println("Admin skipped: " + e.getMessage());
        }
    }
}
