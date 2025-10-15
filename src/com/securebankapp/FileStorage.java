package com.securebankapp;

import com.securebankapp.models.Account;
import com.securebankapp.models.Transaction;
import com.securebankapp.models.User;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class FileStorage {
    private final String usersPath;
    private final String accountsPath;
    private final String txPath;

    public FileStorage(String usersPath, String accountsPath, String txPath) {
        this.usersPath = usersPath;
        this.accountsPath = accountsPath;
        this.txPath = txPath;
    }

    public void ensureFiles() {
        try {
            Files.createDirectories(Paths.get("data"));
            createIfMissing(usersPath, "username,salt,hash,failedAttempts,locked\n");
            createIfMissing(accountsPath, "accountNumber,username,balance,createdAt\n");
            createIfMissing(txPath, "id,accountNumber,type,amount,timestamp\n");
        } catch (IOException e) {
            throw new RuntimeException("Unable to create data files: " + e.getMessage(), e);
        }
    }

    private void createIfMissing(String path, String header) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            try (FileWriter fw = new FileWriter(path, false)) {
                fw.write(header);
            }
        }
    }

    // USERS
    public synchronized Map<String, User> loadUsers() {
        Map<String, User> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(usersPath))) {
            String line; br.readLine(); // header
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",", -1);
                if (p.length < 5) continue;
                User u = new User(p[0], p[1], p[2], Integer.parseInt(p[3]), Boolean.parseBoolean(p[4]));
                map.put(u.getUsername(), u);
            }
        } catch (IOException e) {
            throw new RuntimeException("loadUsers failed: " + e.getMessage(), e);
        }
        return map;
    }

    public synchronized void saveUsers(Collection<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(usersPath, false))) {
            pw.println("username,salt,hash,failedAttempts,locked");
            for (User u : users) {
                pw.printf("%s,%s,%s,%d,%s%n", esc(u.getUsername()), esc(u.getSaltB64()),
                        esc(u.getHashB64()), u.getFailedAttempts(), String.valueOf(u.isLocked()));
            }
        } catch (IOException e) {
            throw new RuntimeException("saveUsers failed: " + e.getMessage(), e);
        }
    }

    // ACCOUNTS
    public synchronized List<Account> loadAccounts() {
        List<Account> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(accountsPath))) {
            String line; br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",", -1);
                if (p.length < 4) continue;
                Account a = new Account(p[0], p[1], new BigDecimal(p[2]), p[3]);
                list.add(a);
            }
        } catch (IOException e) {
            throw new RuntimeException("loadAccounts failed: " + e.getMessage(), e);
        }
        return list;
    }

    public synchronized void saveAccounts(Collection<Account> accounts) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(accountsPath, false))) {
            pw.println("accountNumber,username,balance,createdAt");
            for (Account a : accounts) {
                pw.printf("%s,%s,%s,%s%n", esc(a.getAccountNumber()), esc(a.getUsername()),
                        a.getBalance().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString(), esc(a.getCreatedAt()));
            }
        } catch (IOException e) {
            throw new RuntimeException("saveAccounts failed: " + e.getMessage(), e);
        }
    }

    // TRANSACTIONS
    public synchronized void appendTransaction(Transaction t) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(txPath, true))) {
            pw.printf("%s,%s,%s,%s,%s%n",
                    esc(t.getId()), esc(t.getAccountNumber()), esc(t.getType()),
                    t.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString(),
                    esc(t.getTimestamp()));
        } catch (IOException e) {
            throw new RuntimeException("appendTransaction failed: " + e.getMessage(), e);
        }
    }

    public synchronized List<Transaction> loadTransactionsForAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(txPath))) {
            String line; br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",", -1);
                if (p.length < 5) continue;
                if (Objects.equals(p[1], accountNumber)) {
                    list.add(new Transaction(p[0], p[1], p[2], new BigDecimal(p[3]), p[4]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("loadTransactions failed: " + e.getMessage(), e);
        }
        return list.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
    }

    // Util
    private static String esc(String s) {
        return s.replace(",", "\\,");
    }
}
