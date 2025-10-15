package com.securebankapp;

import com.securebankapp.models.Account;
import com.securebankapp.models.Transaction;

import java.io.Console;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        FileStorage storage = new FileStorage("data/users.txt", "data/accounts.txt", "data/transactions.txt");
        storage.ensureFiles();

        AuthService auth = new AuthService(storage);
        BankService bank = new BankService(storage);

        println("==== Secure Banking Application ====");

        while (true) {
            println("\n1) Register  2) Login  3) Exit");
            print("Choose: ");
            String choice = IN.nextLine().trim();
            switch (choice) {
                case "1":
                    register(auth);
                    break;
                case "2":
                    String user = login(auth);
                    if (user != null) {
                        afterLoginMenu(user, bank, storage);
                    }
                    break;
                case "3":
                    println("Goodbye!");
                    return;
                default:
                    println("Invalid option.");
            }
        }
    }

    private static void register(AuthService auth) {
        print("Choose a username: ");
        String username = IN.nextLine().trim();
        if (username.isEmpty()) { println("Username required."); return; }

        String password = readPassword("Choose a strong password: ");
        if (password == null || password.length() < 8) {
            println("Password must be at least 8 characters.");
            return;
        }
        try {
            auth.register(username, password);
            println("Registration successful. You can now log in.");
        } catch (IllegalArgumentException iae) {
            println("Registration failed: " + iae.getMessage());
        } catch (Exception e) {
            println("Unexpected error: " + e.getMessage());
        }
    }

    private static String login(AuthService auth) {
        print("Username: ");
        String username = IN.nextLine().trim();
        String password = readPassword("Password: ");
        try {
            if (auth.login(username, password)) {
                println("Login success. Welcome, " + username + "!");
                return username;
            } else {
                println("Login failed.");
                return null;
            }
        } catch (IllegalStateException ise) {
            println("Login blocked: " + ise.getMessage());
            return null;
        } catch (Exception e) {
            println("Error: " + e.getMessage());
            return null;
        }
    }

    private static void afterLoginMenu(String username, BankService bank, FileStorage storage) {
        while (true) {
            println("\n== Main Menu ==");
            println("1) Create Bank Account");
            println("2) View Balance");
            println("3) Deposit");
            println("4) Withdraw");
            println("5) View Transaction History");
            println("6) Logout");
            print("Choose: ");
            String choice = IN.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        Account acc = bank.createAccount(username);
                        println("Account created. Number: " + acc.getAccountNumber());
                        break;
                    case "2":
                        String accNumB = prompt("Enter account number: ");
                        BigDecimal bal = bank.getBalance(username, accNumB);
                        println("Balance: " + bal);
                        break;
                    case "3":
                        String accNumD = prompt("Enter account number: ");
                        BigDecimal dep = readMoney("Amount to deposit: ");
                        bank.deposit(username, accNumD, dep);
                        println("Deposit successful.");
                        break;
                    case "4":
                        String accNumW = prompt("Enter account number: ");
                        BigDecimal w = readMoney("Amount to withdraw: ");
                        bank.withdraw(username, accNumW, w);
                        println("Withdrawal successful.");
                        break;
                    case "5":
                        String accNumT = prompt("Enter account number: ");
                        List<Transaction> txs = storage.loadTransactionsForAccount(accNumT);
                        if (txs.isEmpty()) {
                            println("No transactions found.");
                        } else {
                            println("Date/Time | Type | Amount");
                            for (Transaction t : txs) {
                                println(t.getTimestamp() + " | " + t.getType() + " | " + t.getAmount());
                            }
                        }
                        break;
                    case "6":
                        return;
                    default:
                        println("Invalid option.");
                }
            } catch (Exception e) {
                println("Operation failed: " + e.getMessage());
            }
        }
    }

    // Helpers
    private static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pass = console.readPassword(prompt);
            return pass == null ? null : new String(pass);
        }
        print(prompt);
        return IN.nextLine();
    }
    private static BigDecimal readMoney(String prompt) {
        while (true) {
            print(prompt);
            String s = IN.nextLine().trim();
            try {
                BigDecimal v = new BigDecimal(s);
                if (v.scale() > 2) v = v.setScale(2, BigDecimal.ROUND_HALF_UP);
                if (v.compareTo(BigDecimal.ZERO) <= 0) {
                    println("Amount must be positive.");
                } else {
                    return v;
                }
            } catch (NumberFormatException e) {
                println("Invalid number. Try again.");
            }
        }
    }
    private static String prompt(String p) { print(p); return IN.nextLine().trim(); }
    private static void println(String s) { System.out.println(s); }
    private static void print(String s) { System.out.print(s); }
}

