package org.atm.igiresystem.lab3.console;

import org.atm.igiresystem.lab1.models.Account;
import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab1.models.Transaction;
import org.atm.igiresystem.lab1.models.User;
import org.atm.igiresystem.lab2.dao.CustomerDAO;
import org.atm.igiresystem.lab2.dao.UserDAO;
import org.atm.igiresystem.lab3.services.AccountService;
import org.atm.igiresystem.lab3.services.ReportService;
import org.atm.igiresystem.lab3.services.TransactionService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApp {

    private static final Scanner            scanner            = new Scanner(System.in);
    private static final UserDAO            userDAO            = new UserDAO();
    private static final CustomerDAO        customerDAO        = new CustomerDAO();
    private static final AccountService     accountService     = new AccountService();
    private static final TransactionService transactionService = new TransactionService();
    private static final ReportService      reportService      = new ReportService();

    private static User     currentUser     = null;
    private static Customer currentCustomer = null;

    public static void main(String[] args) {
        SchemaSetup.createTables();
        printBanner();
        mainMenu();
    }

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║       IgirePay Digital Wallet        ║");
        System.out.println("║     Secure Mobile Money System       ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n1. Login\n2. Register\n0. Exit");
            System.out.print("Choice: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> { System.out.println("Goodbye!"); System.exit(0); }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    // ── MoMo-style login: phone number → PIN ─────────────────────────────────

    private static void login() {
        System.out.print("Enter your phone number: ");
        String phone = scanner.nextLine().trim();

        Optional<User> userOpt = userDAO.findByPhone(phone);
        if (userOpt.isEmpty()) {
            System.out.println("Phone number not registered.");
            return;
        }

        Optional<Customer> customerOpt = customerDAO.findByPhone(phone);
        if (customerOpt.isEmpty()) {
            System.out.println("No account found for this number.");
            return;
        }

        System.out.print("Enter your PIN: ");
        String pin = scanner.nextLine().trim();

        Customer customer = customerOpt.get();
        if (!customer.validatePin(pin)) {
            System.out.println("Incorrect PIN. Please try again.");
            return;
        }

        currentUser     = userOpt.get();
        currentCustomer = customer;
        System.out.println("\nWelcome, " + currentCustomer.getFullName() + "!");

        if (currentUser.isAdmin()) adminMenu();
        else userMenu();
    }

    private static void register() {
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine().trim();

        if (userDAO.phoneExists(phone)) {
            System.out.println("This phone number is already registered.");
            return;
        }

        System.out.print("Email (optional, press Enter to skip): ");
        String email = scanner.nextLine().trim();
        System.out.print("Set 4-digit PIN: ");
        String pin = scanner.nextLine().trim();
        System.out.print("Confirm PIN: ");
        String confirmPin = scanner.nextLine().trim();

        if (!pin.equals(confirmPin)) {
            System.out.println("PINs do not match.");
            return;
        }

        // phone is stored as username in users table
        User user = new User(0, phone, phone + "_igire", "USER");
        userDAO.create(user);

        Customer customer = new Customer(0, fullName, email.isEmpty() ? null : email, phone, user.getId());
        customer.setPin(pin);
        customerDAO.create(customer);

        System.out.println("Registration successful! You can now login with your phone number and PIN.");
    }

    // ── User Menu ─────────────────────────────────────────────────────────────

    private static void userMenu() {
        while (true) {
            System.out.println("\n╔═══════════════════════╗");
            System.out.println("║    IgirePay Menu      ║");
            System.out.println("╠═══════════════════════╣");
            System.out.println("║ 1. My Accounts        ║");
            System.out.println("║ 2. Create Account     ║");
            System.out.println("║ 3. Deposit            ║");
            System.out.println("║ 4. Withdraw           ║");
            System.out.println("║ 5. Send Money         ║");
            System.out.println("║ 6. Transaction History║");
            System.out.println("║ 7. Change PIN         ║");
            System.out.println("║ 8. Export Statement   ║");
            System.out.println("║ 0. Logout             ║");
            System.out.println("╚═══════════════════════╝");
            System.out.print("Choice: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> viewMyAccounts();
                case "2" -> createAccountMenu();
                case "3" -> deposit();
                case "4" -> withdraw();
                case "5" -> transfer();
                case "6" -> viewHistory();
                case "7" -> changePin();
                case "8" -> exportStatement();
                case "0" -> { currentUser = null; currentCustomer = null; return; }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewMyAccounts() {
        List<Account> accounts = accountService.getCustomerAccounts(currentCustomer.getId());
        if (accounts.isEmpty()) {
            System.out.println("You have no accounts yet. Select 'Create Account' to get started.");
            return;
        }
        System.out.println("\n── Your Accounts ──────────────────");
        for (Account acc : accounts) {
            System.out.printf("  [%s] %s Account — Balance: %.2f RWF%n",
                acc.getId(), acc.getAccountType(), acc.getBalance());
        }
    }

    /** Single "Create Account" entry that shows a sub-menu. */
    private static void createAccountMenu() {
        System.out.println("\n── Create Account ─────────────────");
        System.out.println("  1. Wallet Account  (instant transfers, fees apply)");
        System.out.println("  2. Savings Account (locked savings, 48h withdrawal)");
        System.out.println("  0. Back");
        System.out.print("Choice: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> {
                Account acc = accountService.createWalletAccount(currentCustomer.getId());
                System.out.println("Wallet account created successfully. Account ID: " + acc.getId());
            }
            case "2" -> {
                Account acc = accountService.createSavingsAccount(currentCustomer.getId());
                System.out.println("Savings account created successfully. Account ID: " + acc.getId());
            }
            case "0" -> {}
            default  -> System.out.println("Invalid choice.");
        }
    }

    private static void deposit() {
        List<Account> accounts = accountService.getCustomerAccounts(currentCustomer.getId());
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }

        Account account = selectAccount(accounts);
        if (account == null) return;

        System.out.print("Amount to deposit (RWF): ");
        double amount = parseAmount();
        if (amount <= 0) return;

        System.out.print("Enter PIN to confirm: ");
        String pin = scanner.nextLine().trim();
        if (!accountService.validatePinByCustomer(currentCustomer.getId(), pin)) {
            System.out.println("Incorrect PIN.");
            return;
        }

        String ref = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println(transactionService.deposit(account.getId(), amount, ref));
    }

    private static void withdraw() {
        List<Account> accounts = accountService.getCustomerAccounts(currentCustomer.getId());
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }

        Account account = selectAccount(accounts);
        if (account == null) return;

        System.out.print("Amount to withdraw (RWF): ");
        double amount = parseAmount();
        if (amount <= 0) return;

        System.out.print("Enter PIN to confirm: ");
        String pin = scanner.nextLine().trim();
        if (!accountService.validatePinByCustomer(currentCustomer.getId(), pin)) {
            System.out.println("Incorrect PIN.");
            return;
        }

        String ref = "WIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println(transactionService.withdraw(account.getId(), amount, ref));
    }

    private static void transfer() {
        List<Account> accounts = accountService.getCustomerAccounts(currentCustomer.getId());
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }

        System.out.println("Select your sending account:");
        Account sender = selectAccount(accounts);
        if (sender == null) return;

        System.out.print("Receiver phone number: ");
        String receiverPhone = scanner.nextLine().trim();
        Optional<Customer> receiverCustomer = customerDAO.findByPhone(receiverPhone);
        if (receiverCustomer.isEmpty()) {
            System.out.println("Receiver not found. Check the phone number.");
            return;
        }

        List<Account> receiverAccounts = accountService.getCustomerAccounts(receiverCustomer.get().getId());
        if (receiverAccounts.isEmpty()) {
            System.out.println("Receiver has no wallet account.");
            return;
        }
        // Default to first wallet account of receiver
        Account receiver = receiverAccounts.stream()
            .filter(a -> "WALLET".equals(a.getAccountType()))
            .findFirst()
            .orElse(receiverAccounts.get(0));

        System.out.println("Sending to: " + receiverCustomer.get().getFullName() + " (" + receiverPhone + ")");
        System.out.print("Amount (RWF): ");
        double amount = parseAmount();
        if (amount <= 0) return;

        System.out.print("Enter PIN to confirm: ");
        String pin = scanner.nextLine().trim();
        if (!accountService.validatePinByCustomer(currentCustomer.getId(), pin)) {
            System.out.println("Incorrect PIN.");
            return;
        }

        String ref = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println(transactionService.transfer(sender.getId(), receiver.getId(), amount, ref));
    }

    private static void viewHistory() {
        List<Account> accounts = accountService.getCustomerAccounts(currentCustomer.getId());
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }

        Account account = selectAccount(accounts);
        if (account == null) return;

        List<Transaction> history = transactionService.getTransactionHistory(account.getId());
        if (history.isEmpty()) { System.out.println("No transactions yet."); return; }

        System.out.println("\n── Transaction History ─────────────");
        for (Transaction tx : history) {
            System.out.printf("  [%s] %s  %.2f RWF  %s  %s%n",
                tx.getReferenceId(), tx.getTransactionType(),
                tx.getAmount(), tx.getTransactionStatus(), tx.getTimestamp());
        }
    }

    private static void changePin() {
        System.out.print("Current PIN: ");
        String oldPin = scanner.nextLine().trim();
        if (!accountService.validatePinByCustomer(currentCustomer.getId(), oldPin)) {
            System.out.println("Incorrect PIN.");
            return;
        }
        System.out.print("New 4-digit PIN: ");
        String newPin = scanner.nextLine().trim();
        System.out.print("Confirm new PIN: ");
        String confirm = scanner.nextLine().trim();
        if (!newPin.equals(confirm)) { System.out.println("PINs do not match."); return; }

        accountService.updateCustomerPin(currentCustomer.getId(), newPin);
        currentCustomer.setPin(newPin);
        System.out.println("PIN changed successfully.");
    }

    private static void exportStatement() {
        List<Account> accounts = accountService.getCustomerAccounts(currentCustomer.getId());
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }
        Account account = selectAccount(accounts);
        if (account == null) return;
        String path = "statement_" + currentCustomer.getPhoneNumber() + "_acc" + account.getId() + ".csv";
        reportService.exportTransactionsToCSV(account.getId(), path);
    }

    // ── Admin Menu ────────────────────────────────────────────────────────────

    private static void adminMenu() {
        while (true) {
            System.out.println("\n╔═══════════════════════╗");
            System.out.println("║     Admin Panel       ║");
            System.out.println("╠═══════════════════════╣");
            System.out.println("║ 1. All Customers      ║");
            System.out.println("║ 2. All Transactions   ║");
            System.out.println("║ 3. Daily Summary      ║");
            System.out.println("║ 4. Export All CSV     ║");
            System.out.println("║ 5. Delete Account     ║");
            System.out.println("║ 0. Logout             ║");
            System.out.println("╚═══════════════════════╝");
            System.out.print("Choice: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> customerDAO.findAll().forEach(c -> System.out.println("  " + c));
                case "2" -> transactionService.getAllTransactions().forEach(t -> System.out.println("  " + t));
                case "3" -> reportService.printDailySummary();
                case "4" -> reportService.exportAllTransactionsToCSV("all_transactions.csv");
                case "5" -> {
                    System.out.print("Account ID to delete: ");
                    try {
                        accountService.deleteAccount(Integer.parseInt(scanner.nextLine().trim()));
                        System.out.println("Account deleted.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID.");
                    }
                }
                case "0" -> { currentUser = null; return; }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Show numbered list of accounts and let user pick one. */
    private static Account selectAccount(List<Account> accounts) {
        System.out.println("── Select Account ─────────────────");
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            System.out.printf("  %d. %s Account — %.2f RWF%n", i + 1, acc.getAccountType(), acc.getBalance());
        }
        System.out.print("Choice (0 to cancel): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) return null;
            if (choice >= 1 && choice <= accounts.size()) return accounts.get(choice - 1);
        } catch (NumberFormatException ignored) {}
        System.out.println("Invalid selection.");
        return null;
    }

    private static double parseAmount() {
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) { System.out.println("Amount must be greater than 0."); return -1; }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return -1;
        }
    }
}
