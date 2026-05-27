package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab1.models.User;
import org.atm.igiresystem.lab2.dao.CustomerDAO;
import org.atm.igiresystem.lab2.dao.UserDAO;

public class RegisterController {

    @FXML private TextField     fullNameField;
    @FXML private TextField     phoneField;
    @FXML private TextField     emailField;
    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Label         messageLabel;

    private final UserDAO     userDAO     = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    private void handleRegister() {
        String fullName    = fullNameField.getText().trim();
        String phone       = phoneField.getText().trim();
        String email       = emailField.getText().trim();
        String pin         = pinField.getText().trim();
        String confirmPin  = confirmPinField.getText().trim();

        if (fullName.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
            showError("Full name, phone number, and PIN are required.");
            return;
        }
        if (pin.length() != 4 || !pin.matches("\\d{4}")) {
            showError("PIN must be exactly 4 digits (numbers only).");
            return;
        }
        if (!pin.equals(confirmPin)) {
            showError("PINs do not match.");
            return;
        }
        if (userDAO.phoneExists(phone)) {
            showError("This phone number is already registered.");
            return;
        }

        try {
            User user = new User(0, phone, phone + "_igire", "USER");
            userDAO.create(user);

            Customer customer = new Customer(0, fullName, email.isEmpty() ? null : email, phone, user.getId());
            customer.setPin(pin);
            customerDAO.create(customer);

            messageLabel.setStyle("-fx-text-fill:#1E8E3E;-fx-font-size:13px;");
            messageLabel.setText("✓ Account created! You can now login with your phone and PIN.");
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/atm/igiresystem/lab3/ui/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 440, 580));
            stage.setTitle("IgirePay — Login");
        } catch (Exception e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill:#DC2626;-fx-font-size:12px;");
        messageLabel.setText(msg);
    }
}
