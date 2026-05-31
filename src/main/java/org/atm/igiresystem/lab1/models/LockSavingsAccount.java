package org.atm.igiresystem.lab1.models;

import java.time.LocalDateTime;

public class LockSavingsAccount extends Account {

    public static final double MAX_WITHDRAWAL_LIMIT = 500_000.0;
    public static final long LOCK_MINUTES = 3;

    private LocalDateTime unlockAt;

    public LockSavingsAccount(int id, int customerId, double balance) {
        super(id, customerId, "LOCK_SAVINGS", balance);
    }

    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        setBalance(getBalance() + amount);
        System.out.println("Locked savings deposit: " + amount + " RWF. Balance: " + getBalance() + " RWF.");
        return true;
    }

    @Override
    public boolean withdraw(double amount, String referenceId) {
        if (amount <= 0) return false;
        if (amount > MAX_WITHDRAWAL_LIMIT) {
            System.out.println("Exceeds withdrawal limit of " + MAX_WITHDRAWAL_LIMIT + " RWF.");
            return false;
        }
        if (getBalance() < amount) {
            System.out.println("Insufficient locked savings balance.");
            return false;
        }
        if (unlockAt != null && LocalDateTime.now().isBefore(unlockAt)) {
            System.out.println("Funds are locked until: " + unlockAt);
            return false;
        }
        setBalance(getBalance() - amount);
        System.out.println("Withdrawn " + amount + " RWF from locked savings. Balance: " + getBalance() + " RWF.");
        return true;
    }

    public void requestWithdrawal() {
        unlockAt = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        System.out.println("Withdrawal requested. Funds available at: " + unlockAt);
    }

    public boolean isLocked() {
        return unlockAt != null && LocalDateTime.now().isBefore(unlockAt);
    }

    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }

    public void setUnlockAt(LocalDateTime unlockAt) {
        this.unlockAt = unlockAt;
    }

    @Override
    public String processTransaction(double amount, String type, String referenceId) {
        boolean success;
        switch (type.toUpperCase()) {
            case "DEPOSIT":
                success = deposit(amount);
                break;
            case "WITHDRAW":
                success = withdraw(amount, referenceId);
                break;
            default:
                success = false;
        }
        return success ? "SUCCESS" : "FAILED";
    }

    @Override
    public String toString() {
        return "LockSavingsAccount{id=" + getId() + ", balance=" + getBalance() + " RWF, locked=" + isLocked() + "}";
    }
}
