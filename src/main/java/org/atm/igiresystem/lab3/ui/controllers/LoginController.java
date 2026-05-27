package org.atm.igiresystem.lab3.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab1.models.User;
import org.atm.igiresystem.lab2.dao.CustomerDAO;
import org.atm.igiresystem.lab2.dao.UserDAO;

import java.util.Optional;

public class LoginController {

    @FXML private VBox          phoneStep;
    @FXML private VBox          pinStep;
    @FXML private TextField     phoneField;
    @FXML private PasswordField pinField;
    @FXML private Label         messageLabel;
    @FXML private Label         pinMessageLabel;
    @FXML private Label         greetingLabel;
    @FXML private Label         phoneDisplayLabel;

    private final UserDAO     userDAO     = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    private String verifiedPhone;

    /** Step 1 — user enters phone number */
    @FXML
    private void handlePhoneContinue() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            messageLabel.setText("Please enter your phone number.");
            return;
        }

        Optional<Customer> customerOpt = customerDAO.findByPhone(phone);
        if (customerOpt.isEmpty()) {
            messageLabel.setText("Phone number not registered. Please register first.");
            return;
        }

        verifiedPhone = phone;
        Customer customer = customerOpt.get();

        // Show PIN step
        phoneStep.setVisible(false);
        phoneStep.setManaged(false);
        pinStep.setVisible(true);
        pinStep.setManaged(true);

        greetingLabel.setText("Welcome, " + customer.getFullName() + "!");
        phoneDisplayLabel.setText(phone);
        pinField.requestFocus();
    }

    /** Step 2 — user enters PIN */
    @FXML
    private void handlePinLogin() {
        String pin = pinField.getText().trim();
        if (pin.isEmpty()) {
            pinMessageLabel.setText("Please enter your PIN.");
            return;
        }

        Optional<Customer> customerOpt = customerDAO.findByPhone(verifiedPhone);
        Optional<User>     userOpt     = userDAO.findByPhone(verifiedPhone);

        if (customerOpt.isEmpty() || userOpt.isEmpty()) {
            pinMessageLabel.setText("Account error. Please try again.");
            return;
        }

        Customer customer = customerOpt.get();
        if (!customer.validatePin(pin)) {
            pinMessageLabel.setText("Incorrect PIN. Please try again.");
            pinField.clear();
            return;
        }

        Session.setUser(userOpt.get());
        Session.setCustomer(customer);
        navigate("dashboard.fxml", "IgirePay — Dashboard", 960, 640);
    }

    @FXML
    private void handleBack() {
        pinStep.setVisible(false);
        pinStep.setManaged(false);
        phoneStep.setVisible(true);
        phoneStep.setManaged(true);
        pinField.clear();
        pinMessageLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        navigate("register.fxml", "IgirePay — Register", 440, 640);
    }

    private void navigate(String fxml, String title, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/atm/igiresystem/lab3/ui/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) phoneField.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            messageLabel.setText("Navigation error: " + e.getMessage());
        }
    }
}
