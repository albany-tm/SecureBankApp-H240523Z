package com.securebankapp;

import com.securebankapp.models.User;

import java.util.Map;

public class AuthService {
    private static final int MAX_FAILED = 5;
    private final FileStorage storage;

    public AuthService(FileStorage storage) { this.storage = storage; }

    public void register(String username, String password) {
        Map<String, User> users = storage.loadUsers();
        if (users.containsKey(username)) throw new IllegalArgumentException("Username already exists.");
        String salt = SecurityUtils.generateSaltB64();
        String hash = SecurityUtils.hashPasswordB64(password.toCharArray(), salt);
        users.put(username, new User(username, salt, hash, 0, false));
        storage.saveUsers(users.values());
    }

    public boolean login(String username, String password) {
        Map<String, User> users = storage.loadUsers();
        User u = users.get(username);
        if (u == null) return false;
        if (u.isLocked()) throw new IllegalStateException("Account locked due to repeated failures. Contact admin.");

        boolean ok = SecurityUtils.verify(password.toCharArray(), u.getSaltB64(), u.getHashB64());
        if (ok) {
            u.setFailedAttempts(0);
        } else {
            u.setFailedAttempts(u.getFailedAttempts() + 1);
            if (u.getFailedAttempts() >= MAX_FAILED) u.setLocked(true);
        }
        storage.saveUsers(users.values());
        return ok;
    }
}
