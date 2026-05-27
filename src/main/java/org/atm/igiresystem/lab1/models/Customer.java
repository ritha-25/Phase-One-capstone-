package org.atm.igiresystem.lab1.models;

import java.time.LocalDateTime;

public class Customer {

    private int           id;
    private String        fullName;
    private String        email;
    private String        phoneNumber;
    private String        pin;
    private int           userId;
    private LocalDateTime createdAt;

    /** Used when creating a new customer — validates required fields. */
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

    /** Used when loading from database — no validation needed. */
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

    public boolean validatePin(String input) { return this.pin != null && this.pin.equals(input); }

    public int getUserId()                    { return userId; }
    public void setUserId(int userId)         { this.userId = userId; }

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name=" + fullName + ", phone=" + phoneNumber + ", email=" + email + "}";
    }
}
