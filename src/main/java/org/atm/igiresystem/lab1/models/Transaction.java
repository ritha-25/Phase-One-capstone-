package org.atm.igiresystem.lab1.models;

import java.time.LocalDateTime;

public class Transaction {

    private int           transactionId;
    private String        referenceId;
    private int           senderAccountId;
    private int           receiverAccountId;
    private double        amount;
    private String        transactionType;   // DEPOSIT, WITHDRAW, TRANSFER
    private String        transactionStatus; // SUCCESS, FAILED, PENDING, CANCELLED
    private LocalDateTime timestamp;

    public Transaction(int transactionId, String referenceId, int senderAccountId,
                       int receiverAccountId, double amount,
                       String transactionType, String transactionStatus) {
        this.transactionId     = transactionId;
        this.referenceId       = referenceId;
        this.senderAccountId   = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount            = amount;
        this.transactionType   = transactionType;
        this.transactionStatus = transactionStatus;
        this.timestamp         = LocalDateTime.now();
    }

    public int getTransactionId()                    { return transactionId; }
    public void setTransactionId(int transactionId)  { this.transactionId = transactionId; }

    public String getReferenceId()                   { return referenceId; }
    public void setReferenceId(String referenceId)   { this.referenceId = referenceId; }

    public int getSenderAccountId()                  { return senderAccountId; }
    public void setSenderAccountId(int senderAccountId) { this.senderAccountId = senderAccountId; }

    public int getReceiverAccountId()                { return receiverAccountId; }
    public void setReceiverAccountId(int receiverAccountId) { this.receiverAccountId = receiverAccountId; }

    public double getAmount()                        { return amount; }
    public void setAmount(double amount)             { this.amount = amount; }

    public String getTransactionType()               { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getTransactionStatus()             { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }

    public LocalDateTime getTimestamp()              { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp){ this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + ", ref=" + referenceId +
               ", from=" + senderAccountId + ", to=" + receiverAccountId +
               ", amount=" + amount + " RWF, type=" + transactionType +
               ", status=" + transactionStatus + ", time=" + timestamp + "}";
    }
}
