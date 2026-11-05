
package pickleballsystem.service;

import pickleballsystem.model.Staff;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 * Handles admin/staff accounts, login sessions, lockouts,
 * and saving account passwords.
 */
public class AccountManager {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long ADMIN_LOCKOUT_DURATION_MS = 5 * 60 * 1000L;

    private final ArrayList<Staff> staffMembers = new ArrayList<>();
    private final Map<String, Staff> staffAccounts = new HashMap<>();
    private final Map<String, String> staffPasswords = new HashMap<>();
    private final Map<String, Integer> failedLoginAttempts = new HashMap<>();
    private final Set<String> lockedAccounts = new HashSet<>();

    // Temporary admin lockout expiry times, stored as timestamps.
    private final Map<String, Long> adminLockoutUntil = new HashMap<>();

    private final FileStorageService fileStorageService = new FileStorageService();
    private final String adminUsername = "admin";

    private String adminPassword = "admin123";
    private String currentUserName = "";
    private String loginUsername = "";
    private boolean adminLoggedIn = false;
    private boolean staffLoggedIn = false;
    private int nextStaffId = 1;

    public AccountManager() {
        Staff defaultStaff = new Staff(createStaffId(), "staff");
        staffMembers.add(defaultStaff);
        staffAccounts.put("staff", defaultStaff);
        staffPasswords.put("staff", "staff123");
        loadPersistedAccounts();
    }

    public ArrayList<Staff> getStaffMembers() {
        return staffMembers;
    }

    public Map<String, Staff> getStaffAccounts() {
        return staffAccounts;
    }

    public Map<String, String> getStaffPasswords() {
        return staffPasswords;
    }

    public Map<String, Integer> getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Set<String> getLockedAccounts() {
        return lockedAccounts;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    public void setAdminLoggedIn(boolean adminLoggedIn) {
        this.adminLoggedIn = adminLoggedIn;
    }

    public boolean isStaffLoggedIn() {
        return staffLoggedIn;
    }

    public void setStaffLoggedIn(boolean staffLoggedIn) {
        this.staffLoggedIn = staffLoggedIn;
    }

    /**
     * Checks whether the admin is still temporarily locked.
     * Returns false and clears the expired lockout when time is up.
     */
    public boolean isAdminTemporarilyLocked(String username) {
        if (username == null
                || !username.equalsIgnoreCase(adminUsername)) {
            return false;
        }

        String key = adminUsername.toLowerCase(Locale.ROOT);
        Long unlockTime = adminLockoutUntil.get(key);

        if (unlockTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= unlockTime) {
            adminLockoutUntil.remove(key);
            failedLoginAttempts.remove(key);
            return false;
        }

        return true;
    }

    /**
     * Returns the remaining admin lockout time in seconds.
     */
    public long getAdminLockoutRemainingSeconds(String username) {
        if (username == null
                || !username.equalsIgnoreCase(adminUsername)) {
            return 0;
        }

        String key = adminUsername.toLowerCase(Locale.ROOT);
        Long unlockTime = adminLockoutUntil.get(key);

        if (unlockTime == null) {
            return 0;
        }

        long remainingMillis = unlockTime - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            adminLockoutUntil.remove(key);
            failedLoginAttempts.remove(key);
            return 0;
        }

        // Round up so a few remaining milliseconds show as 1 second.
        return (remainingMillis + 999) / 1000;
    }

    /**
     * Records a failed admin login. Three failures trigger a five-minute lockout.
     * Returns true if the admin is now locked.
     */
    public boolean recordAdminFailedLogin() {
        String key = adminUsername.toLowerCase(Locale.ROOT);

        if (isAdminTemporarilyLocked(adminUsername)) {
            return true;
        }

        int attempts = failedLoginAttempts.getOrDefault(key, 0) + 1;
        failedLoginAttempts.put(key, attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            adminLockoutUntil.put(
                    key,
                    System.currentTimeMillis() + ADMIN_LOCKOUT_DURATION_MS
            );
            return true;
        }

        return false;
    }

    /**
     * Clears admin failed attempts and any temporary lockout.
     */
    public void clearAdminLoginFailures() {
        String key = adminUsername.toLowerCase(Locale.ROOT);
        failedLoginAttempts.remove(key);
        adminLockoutUntil.remove(key);
    }

    private void loadPersistedAccounts() {
        Map<String, String> persisted = fileStorageService.loadAccounts();
        Map<String, Staff> persistedStaff = fileStorageService.loadStaffDetails();

        for (Map.Entry<String, String> entry : persisted.entrySet()) {
            String usernameKey = entry.getKey();

            if (usernameKey.equalsIgnoreCase(adminUsername)) {
                String storedPassword = entry.getValue();

                if (!storedPassword.contains(":") && !storedPassword.isEmpty()) {
                    adminPassword = storedPassword;
                }

                continue;
            }

            String storedPassword = entry.getValue();

            if (storedPassword.contains(":")) {
                storedPassword = usernameKey.equals("staff") ? "staff123" : "";
            }

            staffPasswords.put(usernameKey, storedPassword);

            Staff staff = staffAccounts.get(usernameKey);

            if (staff == null) {
                Staff savedDetails = persistedStaff.get(usernameKey);

                staff = new Staff(
                        createStaffId(),
                        savedDetails == null
                                ? usernameKey
                                : savedDetails.getName()
                );

                staffMembers.add(staff);
                staffAccounts.put(usernameKey, staff);
            }

            Staff savedDetails = persistedStaff.get(usernameKey);

            if (savedDetails != null) {
                staff.setName(savedDetails.getName());
                staff.setActive(savedDetails.isActive());
            }
        }
    }

    public boolean persistAccountPasswords() {
        Map<String, String> allAccounts = new HashMap<>(staffPasswords);
        allAccounts.put(adminUsername, adminPassword);

        try {
            fileStorageService.saveAccounts(allAccounts, staffAccounts);
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "User account changes could not be saved. Please check the data folder and try again.",
                    "Storage Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }
    }

    public boolean passwordMatchesStoredValue(
            String rawPassword,
            String storedValue) {

        return rawPassword != null
                && !rawPassword.isEmpty()
                && storedValue != null
                && !storedValue.isEmpty()
                && rawPassword.equals(storedValue);
    }

    public boolean isLockedAccount(String username) {
        if (username == null) {
            return false;
        }

        return lockedAccounts.contains(
                username.toLowerCase(Locale.ROOT)
        );
    }

    public String createStaffId() {
        return String.format(Locale.ROOT, "STF-%03d", nextStaffId++);
    }

    public Staff findStaffByUsername(String username) {
        return username == null
                ? null
                : staffAccounts.get(
                        username.trim().toLowerCase(Locale.ROOT)
                );
    }

    public String getStaffUsername(Staff staff) {
        for (Map.Entry<String, Staff> account : staffAccounts.entrySet()) {
            if (account.getValue() == staff) {
                return account.getKey();
            }
        }

        return "";
    }

    public int getStaffCount() {
        return staffMembers.size();
    }

    public Staff getStaffAt(int index) {
        return staffMembers.get(index);
    }
}
