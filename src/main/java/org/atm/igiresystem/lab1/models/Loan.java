package org.atm.igiresystem.lab1.models;

import java.time.LocalDateTime;

public class Loan {

    private int           id;
    private int           customerId;
    private int           accountId;
    private double        amount;
    private double        interestRate;
    private String        status;
    private String        purpose;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;

    public Loan(int id, int customerId, int accountId, double amount, double interestRate, String status, String purpose) {
        this.id           = id;
        this.customerId   = customerId;
        this.accountId    = accountId;
        this.amount       = amount;
        this.interestRate = interestRate;
        this.status       = status;
        this.purpose      = purpose;
        this.requestedAt  = LocalDateTime.now();
    }

    public Loan() {}

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getCustomerId()                  { return customerId; }
    public void setCustomerId(int customerId)   { this.customerId = customerId; }

    public int getAccountId()                   { return accountId; }
    public void setAccountId(int accountId)     { this.accountId = accountId; }

    public double getAmount()                   { return amount; }
    public void setAmount(double amount)        { this.amount = amount; }

    public double getInterestRate()             { return interestRate; }
    public void setInterestRate(double rate)    { this.interestRate = rate; }

    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }

    public String getPurpose()                  { return purpose; }
    public void setPurpose(String purpose)      { this.purpose = purpose; }

    public LocalDateTime getRequestedAt()               { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }

    public double getTotalRepayable() {
        return amount + (amount * interestRate / 100);
    }

    @Override
    public String toString() {
        return "Loan{id=" + id + ", customerId=" + customerId + ", amount=" + amount +
               " RWF, status=" + status + ", purpose=" + purpose + "}";
    }
}
