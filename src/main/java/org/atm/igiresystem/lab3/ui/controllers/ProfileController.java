package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab2.dao.CustomerDAO;
import org.atm.igiresystem.lab3.services.AccountService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private TextField     fullNameField;
    @FXML private TextField     emailField;
    @FXML private TextField     phoneField;
    @FXML private Label         messageLabel;

    @FXML private PasswordField currentPinField;
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Label         pinMessageLabel;

    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final AccountService accountService = new AccountService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer customer = Session.getCustomer();
        if (customer != null) {
            fullNameField.setText(customer.getFullName());
            emailField.setText(customer.getEmail() != null ? customer.getEmail() : "");
            phoneField.setText(customer.getPhoneNumber());
        }
    }

    @FXML
    private void handleUpdateProfile() {
        Customer customer = Session.getCustomer();
        if (customer == null) return;

        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();

        if (fullName.isEmpty()) {
            showMsg(messageLabel, "Full name cannot be empty.", false);
            return;
        }
        if (phone.isEmpty()) {
            showMsg(messageLabel, "Phone number cannot be empty.", false);
            return;
        }

        customer.setFullName(fullName);
        customer.setEmail(email.isEmpty() ? null : email);
        customer.setPhoneNumber(phone);

        customerDAO.update(customer);
        Session.setCustomer(customer);
        showMsg(messageLabel, "Profile updated successfully.", true);
    }

    @FXML
    private void handleChangePin() {
        Customer customer = Session.getCustomer();
        if (customer == null) return;

        String currentPin = currentPinField.getText().trim();
        String newPin     = newPinField.getText().trim();
        String confirmPin = confirmPinField.getText().trim();

        if (currentPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            showMsg(pinMessageLabel, "All PIN fields are required.", false);
            return;
        }

        Optional<Customer> fresh = customerDAO.findById(customer.getId());
        if (fresh.isEmpty() || !fresh.get().validatePin(currentPin)) {
            showMsg(pinMessageLabel, "Current PIN is incorrect.", false);
            return;
        }

        if (newPin.length() != 4 || !newPin.matches("\\d{4}")) {
            showMsg(pinMessageLabel, "New PIN must be exactly 4 digits.", false);
            return;
        }

        if (!newPin.equals(confirmPin)) {
            showMsg(pinMessageLabel, "New PINs do not match.", false);
            return;
        }

        accountService.updateCustomerPin(customer.getId(), newPin);
        currentPinField.clear();
        newPinField.clear();
        confirmPinField.clear();
        showMsg(pinMessageLabel, "PIN changed successfully.", true);
    }

    private void showMsg(Label label, String msg, boolean success) {
        label.setStyle(success
            ? "-fx-text-fill:#003087;-fx-font-size:12px;"
            : "-fx-text-fill:#DC2626;-fx-font-size:12px;");
        label.setText(msg);
    }
}
