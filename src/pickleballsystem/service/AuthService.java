package pickleballsystem.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AuthService {
    private final Map<String, AccountRecord> accounts = new HashMap<>();
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Set<String> lockedAccounts = new HashSet<>();

    public AuthService() {
        registerAccount("admin", "admin123", "Admin");
        registerAccount("staff", "staff123", "Staff");
    }

    public boolean registerAccount(String username, String password, String role) {
        String key = normalize(username);
        if (key == null || key.isEmpty() || password == null || password.isBlank()) {
            return false;
        }
        if (accounts.containsKey(key)) {
            return false;
        }
        accounts.put(key, new AccountRecord(role, password));
        failedAttempts.remove(key);
        lockedAccounts.remove(key);
        return true;
    }

    public boolean registerCustomer(String username, String password) {
        return registerAccount(username, password, "Customer");
    }

    public boolean registerStaffUser(String username, String password) {
        return registerAccount(username, password, "Staff");
    }

    public String authenticate(String username, String password) {
        String key = normalize(username);
        if (key == null || key.isEmpty() || password == null || password.isBlank()) {
            return null;
        }
        AccountRecord account = accounts.get(key);
        if (account == null) {
            return null;
        }
        if (lockedAccounts.contains(key)) {
            return "LOCKED";
        }
        if (password.equals(account.getPassword())) {
            failedAttempts.remove(key);
            return account.getRole();
        }
        int failures = failedAttempts.getOrDefault(key, 0) + 1;
        failedAttempts.put(key, failures);
        if (failures >= 3) {
            lockedAccounts.add(key);
            return "LOCKED";
        }
        return "INVALID";
    }

    public boolean changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        String key = normalize(username);
        if (key == null || key.isEmpty()) {
            return false;
        }
        AccountRecord account = accounts.get(key);
        if (account == null) {
            return false;
        }
        if (currentPassword == null || newPassword == null || confirmPassword == null
                || newPassword.isBlank()
                || !newPassword.equals(confirmPassword)
                || !currentPassword.equals(account.getPassword())) {
            return false;
        }
        account.setPassword(newPassword);
        failedAttempts.remove(key);
        lockedAccounts.remove(key);
        return true;
    }

    public boolean isLocked(String username) {
        return lockedAccounts.contains(normalize(username));
    }

    public boolean unlockAccount(String username) {
        String key = normalize(username);
        if (key == null || key.isEmpty()) {
            return false;
        }
        if (lockedAccounts.remove(key)) {
            failedAttempts.remove(key);
            return true;
        }
        return false;
    }

    public boolean hasAccount(String username) {
        return accounts.containsKey(normalize(username));
    }

    private String normalize(String username) {
        if (username == null) {
            return null;
        }
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static final class AccountRecord {
        private final String role;
        private String password;

        private AccountRecord(String role, String password) {
            this.role = role;
            this.password = password;
        }

        private String getPassword() {
            return password;
        }

        private void setPassword(String password) {
            this.password = password;
        }

        private String getRole() {
            return role;
        }
    }
}
