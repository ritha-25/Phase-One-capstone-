package org.atm.igiresystem.lab1.models;

public class WalletAccount extends Account {

    public static final double WITHDRAWAL_FEE = 100.0;
    public static final double TRANSFER_FEE   = 100.0;

    public WalletAccount(int id, int customerId, double balance) {
        super(id, customerId, "WALLET", balance);
    }

    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        setBalance(getBalance() + amount);
        System.out.println("Deposited " + amount + " RWF. New balance: " + getBalance() + " RWF.");
        return true;
    }

    @Override
    public boolean withdraw(double amount, String referenceId) {
        if (amount <= 0) return false;
        double total = amount + WITHDRAWAL_FEE;
        if (getBalance() < total) {
            System.out.println("Insufficient balance. Need " + total + " RWF (fee: " + WITHDRAWAL_FEE + " RWF).");
            return false;
        }
        setBalance(getBalance() - total);
        System.out.println("Withdrawn " + amount + " RWF + fee " + WITHDRAWAL_FEE + " RWF. New balance: " + getBalance() + " RWF.");
        return true;
    }

    public boolean transfer(WalletAccount receiver, double amount, String referenceId) {
        if (amount <= 0) return false;
        double total = amount + TRANSFER_FEE;
        if (getBalance() < total) {
            System.out.println("Insufficient balance for transfer. Need " + total + " RWF (fee: " + TRANSFER_FEE + " RWF).");
            return false;
        }
        setBalance(getBalance() - total);
        receiver.setBalance(receiver.getBalance() + amount);
        System.out.println("Transferred " + amount + " RWF + fee " + TRANSFER_FEE + " RWF. Your balance: " + getBalance() + " RWF.");
        return true;
    }

    @Override
    public String processTransaction(double amount, String type, String referenceId) {
        boolean success = switch (type.toUpperCase()) {
            case "DEPOSIT"  -> deposit(amount);
            case "WITHDRAW" -> withdraw(amount, referenceId);
            default -> false;
        };
        return success ? "SUCCESS" : "FAILED";
    }

    @Override
    public String toString() {
        return "WalletAccount{id=" + getId() + ", balance=" + getBalance() + " RWF}";
    }
}
