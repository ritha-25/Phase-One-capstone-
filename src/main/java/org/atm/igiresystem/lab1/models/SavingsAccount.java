package org.atm.igiresystem.lab1.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SavingsAccount extends Account {

    public static final double MAX_WITHDRAWAL_LIMIT = 500_000.0;
    public static final double INTEREST_RATE        = 0.05;
    private static final int   INTEREST_MONTHS      = 3;

    private LocalDateTime withdrawalAvailableAt;

    public SavingsAccount(int id, int customerId, double balance) {
        super(id, customerId, "SAVINGS", balance);
    }

    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        setBalance(getBalance() + amount);
        System.out.println("Saved " + amount + " RWF. New balance: " + getBalance() + " RWF.");
        return true;
    }

    /** Savings withdrawals are NOT instant — request is recorded, funds available after 48 hours. */
    @Override
    public boolean withdraw(double amount, String referenceId) {
        if (amount <= 0) return false;
        if (amount > MAX_WITHDRAWAL_LIMIT) {
            System.out.println("Exceeds withdrawal limit of " + MAX_WITHDRAWAL_LIMIT + " RWF.");
            return false;
        }
        if (getBalance() < amount) {
            System.out.println("Insufficient savings balance.");
            return false;
        }
        if (withdrawalAvailableAt != null && LocalDateTime.now().isBefore(withdrawalAvailableAt)) {
            System.out.println("Withdrawal request already pending. Available at: " + withdrawalAvailableAt);
            return false;
        }
        withdrawalAvailableAt = LocalDateTime.now().plusHours(48);
        System.out.println("Withdrawal request received. Your funds of " + amount + " RWF will be available after 48 hours.");
        System.out.println("Available at: " + withdrawalAvailableAt);
        return true;
    }

    /** Calculate interest only if account is older than 3 months. */
    public double calculateInterest() {
        long months = ChronoUnit.MONTHS.between(getCreatedAt(), LocalDateTime.now());
        if (months < INTEREST_MONTHS) {
            System.out.println("No interest yet. Account must be at least " + INTEREST_MONTHS + " months old.");
            return 0;
        }
        double interest = getBalance() * INTEREST_RATE;
        System.out.println("Interest earned: " + interest + " RWF.");
        return interest;
    }

    @Override
    public String processTransaction(double amount, String type, String referenceId) {
        boolean success = switch (type.toUpperCase()) {
            case "DEPOSIT"  -> deposit(amount);
            case "WITHDRAW" -> withdraw(amount, referenceId);
            default -> false;
        };
        return success ? "PENDING" : "FAILED";
    }

    public LocalDateTime getWithdrawalAvailableAt() { return withdrawalAvailableAt; }

    @Override
    public String toString() {
        return "SavingsAccount{id=" + getId() + ", balance=" + getBalance() + " RWF}";
    }
}
