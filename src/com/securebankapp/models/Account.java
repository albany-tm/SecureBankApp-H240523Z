package com.securebankapp.models;

import java.math.BigDecimal;

public class Account {
    private String accountNumber;
    private String username;
    private BigDecimal balance;
    private String createdAt;

    public Account(String accountNumber, String username, BigDecimal balance, String createdAt) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.createdAt = createdAt;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getUsername() { return username; }
    public BigDecimal getBalance() { return balance; }
    public String getCreatedAt() { return createdAt; }

    public void setBalance(BigDecimal balance) { this.balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP); }
}
