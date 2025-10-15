package com.securebankapp.models;

public class User {
    private String username;
    private String saltB64;
    private String hashB64;
    private int failedAttempts;
    private boolean locked;

    public User(String username, String saltB64, String hashB64, int failedAttempts, boolean locked) {
        this.username = username;
        this.saltB64 = saltB64;
        this.hashB64 = hashB64;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    public String getUsername() { return username; }
    public String getSaltB64() { return saltB64; }
    public String getHashB64() { return hashB64; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return locked; }

    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public void setLocked(boolean locked) { this.locked = locked; }
}
