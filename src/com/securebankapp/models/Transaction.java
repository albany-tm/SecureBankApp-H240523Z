package com.securebankapp.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Transaction {
    private String id;
    private String accountNumber;
    private String type; // DEPOSIT / WITHDRAW
    private BigDecimal amount;
    private String timestamp;

    public Transaction(String id, String accountNumber, String type, BigDecimal amount, String timestamp) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.timestamp = timestamp;
    }

    public static Transaction create(String accountNumber, String type, BigDecimal amount) {
        return new Transaction(UUID.randomUUID().toString(), accountNumber, type, amount, Instant.now().toString());
    }

    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getTimestamp() { return timestamp; }
}
