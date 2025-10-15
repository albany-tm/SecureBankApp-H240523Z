package com.securebankapp;

import com.securebankapp.models.Account;
import com.securebankapp.models.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class BankService {
    private final FileStorage storage;

    public BankService(FileStorage storage) { this.storage = storage; }

    public Account createAccount(String username) {
        List<Account> accounts = storage.loadAccounts();
        String accNum = generateAccountNumber(accounts);
        Account a = new Account(accNum, username, BigDecimal.ZERO.setScale(2), Instant.now().toString());
        accounts.add(a);
        storage.saveAccounts(accounts);
        return a;
    }

    public BigDecimal getBalance(String username, String accountNumber) {
        Account a = findOwnedAccount(username, accountNumber);
        return a.getBalance();
    }

    public void deposit(String username, String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive.");
        List<Account> accounts = storage.loadAccounts();
        Account a = findOwnedAccount(username, accountNumber, accounts);
        a.setBalance(a.getBalance().add(amount));
        storage.saveAccounts(accounts);
        storage.appendTransaction(Transaction.create(accountNumber, "DEPOSIT", amount));
    }

    public void withdraw(String username, String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive.");
        List<Account> accounts = storage.loadAccounts();
        Account a = findOwnedAccount(username, accountNumber, accounts);
        if (a.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds.");
        a.setBalance(a.getBalance().subtract(amount));
        storage.saveAccounts(accounts);
        storage.appendTransaction(Transaction.create(accountNumber, "WITHDRAW", amount));
    }

    // Helpers
    private Account findOwnedAccount(String username, String accountNumber) {
        return findOwnedAccount(username, accountNumber, storage.loadAccounts());
    }
    private Account findOwnedAccount(String username, String accountNumber, List<Account> accounts) {
        return accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber) && a.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found or not owned by user."));
    }
    private String generateAccountNumber(List<Account> accounts) {
        Set<String> existing = new HashSet<>();
        for (Account a : accounts) existing.add(a.getAccountNumber());
        String acc;
        do {
            acc = "SB" + (100000 + new Random().nextInt(900000));
        } while (existing.contains(acc));
        return acc;
    }
}
