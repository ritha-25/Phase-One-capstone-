package org.atm.igiresystem.lab3.services;

import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Loan;
import org.atm.igiresystem.lab2.dao.AccountDAO;
import org.atm.igiresystem.lab2.dao.LoanDAO;

import java.util.List;
import java.util.Optional;

public class LoanService {

    private static final double INTEREST_RATE   = 10.0;
    private static final double MAX_LOAN_AMOUNT = 5_000_000.0;
    private static final double MIN_LOAN_AMOUNT = 10_000.0;

    private final LoanDAO    loanDAO    = new LoanDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public String requestLoan(int customerId, int accountId, double amount, String purpose) {
        if (amount < MIN_LOAN_AMOUNT) {
            return "FAILED: Minimum loan amount is " + MIN_LOAN_AMOUNT + " RWF.";
        }
        if (amount > MAX_LOAN_AMOUNT) {
            return "FAILED: Maximum loan amount is " + MAX_LOAN_AMOUNT + " RWF.";
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            return "FAILED: Please provide a loan purpose.";
        }

        Optional<Account> accountOpt = accountDAO.findById(accountId);
        if (accountOpt.isEmpty()) {
            return "FAILED: Account not found.";
        }

        List<Account> allAccounts = accountDAO.findByCustomerId(customerId);
        double totalSavings = 0;
        for (Account a : allAccounts) {
            if ("SAVINGS".equals(a.getAccountType()) || "LOCK_SAVINGS".equals(a.getAccountType())) {
                totalSavings += a.getBalance();
            }
        }

        if (totalSavings < 20_000.0) {
            return "FAILED: You need at least 20,000 RWF in savings to be eligible for a loan.";
        }
        if (totalSavings < 50_000.0 && amount > 100_000.0) {
            return "FAILED: You need at least 50,000 RWF in savings to request a loan above 100,000 RWF.";
        }

        List<Loan> existing = loanDAO.findByCustomerId(customerId);
        for (Loan loan : existing) {
            if ("PENDING".equals(loan.getStatus()) || "APPROVED".equals(loan.getStatus())) {
                return "FAILED: You already have an active loan. Please repay it first.";
            }
        }

        Loan loan = new Loan(0, customerId, accountId, amount, INTEREST_RATE, "PENDING", purpose.trim());
        loanDAO.create(loan);
        return "SUCCESS: Loan request of " + amount + " RWF submitted. Interest rate: " + INTEREST_RATE + "%. Total repayable: " + loan.getTotalRepayable() + " RWF. Status: PENDING approval.";
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
        return "SUCCESS: Loan #" + loanId + " approved. " + loan.getAmount() + " RWF disbursed to account #" + loan.getAccountId() + ".";
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
}
