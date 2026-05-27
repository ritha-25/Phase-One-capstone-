package org.atm.igiresystem.lab1.models;

import java.time.LocalDateTime;

public abstract class Account {

    private int id;
    private int customerId;
    private String accountType;
    private double balance;
    private String pin;
    private LocalDateTime createdAt;

    public Account(int id, int customerId, String accountType, double balance) {
        this.id = id;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }

    public abstract boolean deposit(double amount);
    public abstract boolean withdraw(double amount, String referenceId);
    public abstract String processTransaction(double amount, String type, String referenceId);

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) {
        if (balance < 0) throw new IllegalArgumentException("Balance cannot be negative.");
        this.balance = balance;
    }

    public String getPin() { return pin; }
    public void setPin(String pin) {
        if (pin == null || pin.length() != 4) throw new IllegalArgumentException("PIN must be 4 digits.");
        this.pin = pin;
    }

    public boolean validatePin(String inputPin) { return this.pin != null && this.pin.equals(inputPin); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Account{id=" + id + ", customerId=" + customerId +
               ", type=" + accountType + ", balance=" + balance + " RWF}";
    }
}
