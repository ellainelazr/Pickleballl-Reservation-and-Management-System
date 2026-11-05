
package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import pickleballsystem.PickleballMain;
import pickleballsystem.model.Staff;

import static pickleballsystem.ui.UiHelper.*;

public class LoginScreen extends BaseScreen {

    public LoginScreen(PickleballMain app) {
        super(app);
    }

    public void showLoginScreen() {
        JFrame loginFrame = new JFrame("SmashPoint Pickleball");
        loginFrame.setSize(500, 400);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CYAN);
        headerPanel.setPreferredSize(new Dimension(500, 80));

        JLabel logoLabel = new JLabel("SmashPoint Pickleball");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        logoLabel.setForeground(WHITE);
        logoLabel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
        headerPanel.add(logoLabel, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(LIGHT_GRAY);
        contentPanel.setBorder(
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        );

        JLabel loginTitle = new JLabel("Staff / Admin Login");
        loginTitle.setFont(new Font("Arial", Font.PLAIN, 16));
        loginTitle.setForeground(DARK_TEXT);
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 15, 20));
        inputPanel.setBackground(LIGHT_GRAY);
        inputPanel.setMaximumSize(new Dimension(350, 100));

        JLabel userLabel = new JLabel("Name / Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(DARK_TEXT);

        JTextField userField = new JTextField();
        userField.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passLabel.setForeground(DARK_TEXT);

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Arial", Font.PLAIN, 12));

        inputPanel.add(userLabel);
        inputPanel.add(userField);
        inputPanel.add(passLabel);
        inputPanel.add(passField);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 0)
        );
        buttonPanel.setBackground(LIGHT_GRAY);

        JButton loginButton = new JButton("Login");
        styleButton(loginButton, TEAL, WHITE, 12, 100, 40);

        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String normalizedUsername = username.toLowerCase(Locale.ROOT);

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(
                        loginFrame,
                        "Please enter your name.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            boolean adminUsername = username.equalsIgnoreCase(
                    accounts.getAdminUsername()
            );

            /*
             * Check lockout BEFORE checking the password.
             * This prevents a locked account from bypassing the lock
             * by entering the correct password.
             */
            if (adminUsername
                    && accounts.isAdminTemporarilyLocked(username)) {

                long secondsRemaining =
                        accounts.getAdminLockoutRemainingSeconds(username);

                long minutesRemaining = (secondsRemaining + 59) / 60;

                JOptionPane.showMessageDialog(
                        loginFrame,
                        "The administrator account is temporarily locked "
                        + "after 3 failed login attempts.\n"
                        + "Please try again in about "
                        + minutesRemaining + " minute(s).",
                        "Admin Account Temporarily Locked",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!adminUsername
                    && accounts.isLockedAccount(normalizedUsername)) {

                JOptionPane.showMessageDialog(
                        loginFrame,
                        "This staff account is locked due to repeated "
                        + "failed login attempts.\n"
                        + "Please ask an administrator to unlock it.",
                        "Staff Account Locked",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // ADMIN LOGIN
            if (adminUsername) {
                if (password.equals(accounts.getAdminPassword())) {
                    accounts.clearAdminLoginFailures();

                    accounts.setCurrentUserName("admin");
                    accounts.setLoginUsername(
                            accounts.getAdminUsername().toLowerCase(Locale.ROOT)
                    );
                    accounts.setAdminLoggedIn(true);
                    accounts.setStaffLoggedIn(false);

                    data.getReservationHistory().recordActivity(
                            "Admin login: admin"
                    );

                    loginFrame.dispose();
                    app.showAdminDashboard();
                    return;
                }

                boolean lockedNow = accounts.recordAdminFailedLogin();

                if (lockedNow) {
                    JOptionPane.showMessageDialog(
                            loginFrame,
                            "You have reached the maximum of 3 failed login "
                            + "attempts.\nThe administrator account is now "
                            + "temporarily locked for 5 minutes.",
                            "Admin Account Temporarily Locked",
                            JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    int attemptsUsed = accounts.getFailedLoginAttempts()
                            .getOrDefault(normalizedUsername, 0);
                    int attemptsRemaining = 3 - attemptsUsed;

                    JOptionPane.showMessageDialog(
                            loginFrame,
                            "The username or password is incorrect.\n"
                            + "Attempts remaining: " + attemptsRemaining,
                            "Login Information",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

                return;
            }

            // STAFF LOGIN
            String storedStaffPassword =
                    accounts.getStaffPasswords().get(normalizedUsername);

            Staff matchingStaff =
                    accounts.findStaffByUsername(username);

            if (matchingStaff != null
                    && storedStaffPassword != null
                    && accounts.passwordMatchesStoredValue(
                            password, storedStaffPassword)) {

                if (!matchingStaff.isActive()) {
                    JOptionPane.showMessageDialog(
                            loginFrame,
                            "This staff account is inactive. Please ask an "
                            + "administrator to reactivate it.",
                            "Inactive Staff Account",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                accounts.getFailedLoginAttempts().remove(normalizedUsername);

                accounts.setCurrentUserName(matchingStaff.getName());
                accounts.setLoginUsername(normalizedUsername);
                accounts.setStaffLoggedIn(true);
                accounts.setAdminLoggedIn(false);

                data.getReservationHistory().recordActivity(
                        "Staff login: " + accounts.getCurrentUserName()
                );

                loginFrame.dispose();
                app.showCustomerDashboard();
                return;
            }

            // FAILED STAFF LOGIN
            int failureCount = accounts.getFailedLoginAttempts()
                    .getOrDefault(normalizedUsername, 0) + 1;

            accounts.getFailedLoginAttempts().put(
                    normalizedUsername, failureCount
            );

            if (failureCount >= 3) {
                accounts.getLockedAccounts().add(normalizedUsername);

                JOptionPane.showMessageDialog(
                        loginFrame,
                        "You have reached the maximum of 3 failed login "
                        + "attempts.\nThis staff account is now locked. "
                        + "Please ask an administrator to unlock it.",
                        "Staff Account Locked",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int attemptsRemaining = 3 - failureCount;

            JOptionPane.showMessageDialog(
                    loginFrame,
                    "The username or password is incorrect.\n"
                    + "Attempts remaining: " + attemptsRemaining,
                    "Login Information",
                    JOptionPane.ERROR_MESSAGE
            );
        });

        JButton backButton = new JButton("Back");
        styleButton(
                backButton,
                new Color(100, 100, 100),
                WHITE,
                12,
                100,
                40
        );

        backButton.addActionListener(e -> {
            loginFrame.dispose();
            app.showIntroScreen();
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(backButton);

        contentPanel.add(loginTitle);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(buttonPanel);

        loginFrame.add(headerPanel, BorderLayout.NORTH);
        loginFrame.add(contentPanel, BorderLayout.CENTER);
        loginFrame.setVisible(true);
    }

    public void showPasswordChangeDialog(JFrame parentFrame) {
        PasswordChangePanel fields = new PasswordChangePanel();

        int answer = JOptionPane.showConfirmDialog(
                parentFrame,
                fields,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        String currentPassword = fields.getCurrentPassword();
        String newPassword = fields.getNewPassword();
        String confirmPassword = fields.getConfirmPassword();

        String normalizedUsername =
                accounts.getLoginUsername().toLowerCase(Locale.ROOT);

        if (newPassword.isEmpty()
                || !newPassword.equals(confirmPassword)) {

            JOptionPane.showMessageDialog(
                    parentFrame,
                    "The new password must be entered and confirmed.",
                    "Password Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (accounts.isAdminLoggedIn()
                && accounts.getCurrentUserName().equalsIgnoreCase(
                        accounts.getAdminUsername())) {

            if (!accounts.getAdminPassword().equals(currentPassword)) {
                JOptionPane.showMessageDialog(
                        parentFrame,
                        "Current password is incorrect.",
                        "Verification failed",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            accounts.setAdminPassword(newPassword);

            if (!accounts.persistAccountPasswords()) {
                accounts.setAdminPassword(currentPassword);
                return;
            }

            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Password changed successfully.",
                    "Password Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String storedValue =
                accounts.getStaffPasswords().get(normalizedUsername);

        if (storedValue == null
                || !accounts.passwordMatchesStoredValue(
                        currentPassword, storedValue)) {

            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Current password is incorrect.",
                    "Verification failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        accounts.getStaffPasswords().put(normalizedUsername, newPassword);

        if (!accounts.persistAccountPasswords()) {
            accounts.getStaffPasswords().put(normalizedUsername, storedValue);
            return;
        }

        JOptionPane.showMessageDialog(
                parentFrame,
                "Password changed successfully.",
                "Password Updated",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
