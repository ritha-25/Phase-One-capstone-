package org.atm.igiresystem.lab3.services;

import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Loan;
import org.atm.igiresystem.lab2.dao.AccountDAO;
import org.atm.igiresystem.lab2.dao.LoanDAO;

import java.util.List;
import java.util.Optional;

public class LoanService {

    private static final double INTEREST_RATE       = 10.0;
    private static final double MIN_SAVINGS_REQUIRED = 20_000.0;
    private static final double MID_SAVINGS_LIMIT    = 50_000.0;
    private static final double MID_LOAN_MAX         = 100_000.0;
    private static final double MAX_LOAN_AMOUNT      = 5_000_000.0;
    private static final double MIN_LOAN_AMOUNT      = 10_000.0;

    private final LoanDAO    loanDAO    = new LoanDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public String requestLoan(int customerId, int accountId, double amount, String purpose) {
        if (purpose == null || purpose.trim().isEmpty()) {
            return "FAILED: Please provide a loan purpose.";
        }
        if (amount < MIN_LOAN_AMOUNT) {
            return "FAILED: Minimum loan amount is " + String.format("%.0f", MIN_LOAN_AMOUNT) + " RWF.";
        }
        if (amount > MAX_LOAN_AMOUNT) {
            return "FAILED: Maximum loan amount is " + String.format("%.0f", MAX_LOAN_AMOUNT) + " RWF.";
        }

        Optional<Account> accountOpt = accountDAO.findById(accountId);
        if (accountOpt.isEmpty()) {
            return "FAILED: Account not found.";
        }

        double totalSavings = getTotalSavings(customerId);

        if (totalSavings < MIN_SAVINGS_REQUIRED) {
            return "FAILED: You need at least " + String.format("%.0f", MIN_SAVINGS_REQUIRED) +
                   " RWF in savings to qualify for a loan. Your current savings: " +
                   String.format("%.2f", totalSavings) + " RWF.";
        }

        if (totalSavings < MID_SAVINGS_LIMIT && amount > MID_LOAN_MAX) {
            return "FAILED: With savings below " + String.format("%.0f", MID_SAVINGS_LIMIT) +
                   " RWF, your maximum loan is " + String.format("%.0f", MID_LOAN_MAX) +
                   " RWF. Your savings: " + String.format("%.2f", totalSavings) + " RWF.";
        }

        List<Loan> existing = loanDAO.findByCustomerId(customerId);
        for (Loan loan : existing) {
            if ("PENDING".equals(loan.getStatus()) || "APPROVED".equals(loan.getStatus())) {
                return "FAILED: You already have an active loan. Please repay it first.";
            }
        }

        Loan loan = new Loan(0, customerId, accountId, amount, INTEREST_RATE, "PENDING", purpose.trim());
        loanDAO.create(loan);

        return "SUCCESS: Loan request of " + String.format("%.2f", amount) + " RWF submitted. " +
               "Interest: " + INTEREST_RATE + "%. Total repayable: " +
               String.format("%.2f", loan.getTotalRepayable()) + " RWF. Awaiting admin approval.";
    }

    private double getTotalSavings(int customerId) {
        List<Account> accounts = accountDAO.findByCustomerId(customerId);
        double total = 0;
        for (Account acc : accounts) {
            if ("SAVINGS".equals(acc.getAccountType())) {
                total += acc.getBalance();
            }
        }
        return total;
    }

    public List<Loan> getCustomerLoans(int customerId) {
        return loanDAO.findByCustomerId(customerId);
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }

    public String approveLoan(int loanId) {
        Optional<Loan> opt = loanDAO.findById(loanId);
        if (opt.isEmpty()) return "FAILED: Loan not found.";
        Loan loan = opt.get();
        if (!"PENDING".equals(loan.getStatus())) return "FAILED: Loan is not in PENDING status.";

        Optional<Account> accountOpt = accountDAO.findById(loan.getAccountId());
        if (accountOpt.isEmpty()) return "FAILED: Account not found.";

        Account account = accountOpt.get();
        account.deposit(loan.getAmount());
        accountDAO.updateBalance(account.getId(), account.getBalance());

        loan.setStatus("APPROVED");
        loanDAO.update(loan);
        return "SUCCESS: Loan #" + loanId + " approved. " +
               String.format("%.2f", loan.getAmount()) + " RWF disbursed to account #" + loan.getAccountId() + ".";
    }

    public String rejectLoan(int loanId) {
        Optional<Loan> opt = loanDAO.findById(loanId);
        if (opt.isEmpty()) return "FAILED: Loan not found.";
        Loan loan = opt.get();
        if (!"PENDING".equals(loan.getStatus())) return "FAILED: Loan is not in PENDING status.";
        loan.setStatus("REJECTED");
        loanDAO.update(loan);
        return "SUCCESS: Loan #" + loanId + " rejected.";
    }

    public String repayLoan(int loanId, int accountId) {
        Optional<Loan> opt = loanDAO.findById(loanId);
        if (opt.isEmpty()) return "FAILED: Loan not found.";
        Loan loan = opt.get();

        if (!"APPROVED".equals(loan.getStatus())) {
            return "FAILED: Only approved loans can be repaid. Current status: " + loan.getStatus() + ".";
        }
        if (loan.getAccountId() != accountId) {
            return "FAILED: Repayment must come from the account that received the loan.";
        }

        Optional<Account> accountOpt = accountDAO.findById(accountId);
        if (accountOpt.isEmpty()) return "FAILED: Account not found.";

        Account account = accountOpt.get();
        double repayable = loan.getTotalRepayable();

        if (account.getBalance() < repayable) {
            return "FAILED: Insufficient balance. You need " + String.format("%.2f", repayable) +
                   " RWF to repay. Available: " + String.format("%.2f", account.getBalance()) + " RWF.";
        }

        double newBalance = account.getBalance() - repayable;
        accountDAO.updateBalance(accountId, newBalance);

        loan.setStatus("REPAID");
        loanDAO.update(loan);

        return "SUCCESS: Loan #" + loanId + " repaid. " + String.format("%.2f", repayable) +
               " RWF deducted from account #" + accountId + ".";
    }
}
