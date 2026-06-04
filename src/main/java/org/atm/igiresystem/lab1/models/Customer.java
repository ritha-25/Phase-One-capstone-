package org.atm.igiresystem.lab1.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Customer {

    private int           id;
    private String        fullName;
    private String        email;
    private String        phoneNumber;
    private String        pin;
    private String        pinHash;
    private int           userId;
    private LocalDateTime createdAt;
    private int           failedPinAttempts;
    private boolean       locked;

    private List<Account>            accounts           = new ArrayList<>();
    private List<Transaction>        transactionHistory = new ArrayList<>();
    private Set<String>              processedRefIds    = new HashSet<>();
    private Map<String, Transaction> failedTxLogs       = new HashMap<>();

    public Customer(int id, String fullName, String email, String phoneNumber, int userId) {
        if (fullName == null || fullName.isEmpty()) throw new IllegalArgumentException("Full name is required.");
        if (phoneNumber == null || phoneNumber.isEmpty()) throw new IllegalArgumentException("Phone number is required.");
        this.id          = id;
        this.fullName    = fullName;
        this.email       = email;
        this.phoneNumber = phoneNumber;
        this.userId      = userId;
        this.createdAt   = LocalDateTime.now();
    }

    public Customer() {}

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getFullName()              { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }

    public String getPhoneNumber()           { return phoneNumber; }
    public void setPhoneNumber(String phone) { this.phoneNumber = phone; }

    public String getPin()                   { return pin; }
    public void setPin(String pin) {
        if (pin == null || pin.length() != 4) throw new IllegalArgumentException("PIN must be 4 digits.");
        this.pin = pin;
    }

    public String getPinHash()               { return pinHash; }
    public void setPinHash(String pinHash)   { this.pinHash = pinHash; }

    public boolean validatePin(String input) {
        if (input == null) return false;
        if (pinHash != null && !pinHash.isEmpty()) {
            String inputHash = org.atm.igiresystem.lab2.dao.CustomerDAO.hashPin(input);
            return pinHash.equals(inputHash);
        }
        return this.pin != null && this.pin.equals(input);
    }

    public int getUserId()                    { return userId; }
    public void setUserId(int userId)         { this.userId = userId; }

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    public int getFailedPinAttempts()                       { return failedPinAttempts; }
    public void setFailedPinAttempts(int failedPinAttempts) { this.failedPinAttempts = failedPinAttempts; }

    public boolean isLocked()             { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public List<Account> getAccounts()                        { return accounts; }
    public void addAccount(Account account)                   { accounts.add(account); }

    public List<Transaction> getTransactionHistory()          { return transactionHistory; }
    public void addTransaction(Transaction tx)                { transactionHistory.add(tx); }

    public Set<String> getProcessedRefIds()                   { return processedRefIds; }
    public boolean hasProcessedRef(String refId)              { return processedRefIds.contains(refId); }
    public void addProcessedRef(String refId)                 { processedRefIds.add(refId); }

    public Map<String, Transaction> getFailedTxLogs()         { return failedTxLogs; }
    public void logFailedTx(String refId, Transaction tx)     { failedTxLogs.put(refId, tx); }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name=" + fullName + ", phone=" + phoneNumber + ", email=" + email + "}";
    }
}
