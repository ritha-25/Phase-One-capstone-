package org.atm.igiresystem.lab3.services;

import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab1.models.SavingsAccount;
import org.atm.igiresystem.lab1.models.WalletAccount;
import org.atm.igiresystem.lab2.dao.AccountDAO;
import org.atm.igiresystem.lab2.dao.CustomerDAO;

import java.util.List;
import java.util.Optional;

public class AccountService {

    private final AccountDAO  accountDAO  = new AccountDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    public Account createWalletAccount(int customerId) {
        WalletAccount account = new WalletAccount(0, customerId, 0);
        accountDAO.create(account);
        return account;
    }

    public Account createSavingsAccount(int customerId) {
        SavingsAccount account = new SavingsAccount(0, customerId, 0);
        accountDAO.create(account);
        return account;
    }

    public Optional<Account> getAccount(int accountId) {
        return accountDAO.findById(accountId);
    }

    public List<Account> getCustomerAccounts(int customerId) {
        return accountDAO.findByCustomerId(customerId);
    }

    public boolean deposit(int accountId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        Optional<Account> opt = accountDAO.findById(accountId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Account not found: " + accountId);
        Account account = opt.get();
        boolean success = account.deposit(amount);
        if (success) accountDAO.updateBalance(accountId, account.getBalance());
        return success;
    }

    public boolean withdraw(int accountId, double amount, String referenceId) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        Optional<Account> opt = accountDAO.findById(accountId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Account not found: " + accountId);
        Account account = opt.get();
        boolean success = account.withdraw(amount, referenceId);
        if (success && account instanceof WalletAccount) {
            accountDAO.updateBalance(accountId, account.getBalance());
        }
        return success;
    }

    public boolean validatePin(int accountId, String pin) {
        Optional<Account> accountOpt = accountDAO.findById(accountId);
        if (accountOpt.isEmpty()) return false;
        int customerId = accountOpt.get().getCustomerId();
        Optional<Customer> customerOpt = customerDAO.findById(customerId);
        return customerOpt.map(c -> c.validatePin(pin)).orElse(false);
    }

    public boolean validatePinByCustomer(int customerId, String pin) {
        Optional<Customer> opt = customerDAO.findById(customerId);
        if (opt.isEmpty()) return false;
        Customer c = opt.get();
        if (c.isLocked()) return false;
        boolean valid = c.validatePin(pin);
        if (!valid) {
            customerDAO.incrementFailedAttempts(customerId);
            int attempts = c.getFailedPinAttempts() + 1;
            if (attempts >= 3) customerDAO.lockAccount(customerId);
        } else {
            customerDAO.resetFailedAttempts(customerId);
        }
        return valid;
    }

    public void updateCustomerPin(int customerId, String newPin) {
        customerDAO.updatePin(customerId, newPin);
    }

    public void deleteAccount(int accountId) {
        accountDAO.delete(accountId);
    }

    public boolean isCustomerLocked(int customerId) {
        Optional<Customer> opt = customerDAO.findById(customerId);
        return opt.map(Customer::isLocked).orElse(false);
    }

    public void unlockCustomer(int customerId) {
        customerDAO.unlockAccount(customerId);
    }

    public int getFailedAttempts(int customerId) {
        Optional<Customer> opt = customerDAO.findById(customerId);
        return opt.map(Customer::getFailedPinAttempts).orElse(0);
    }
}
