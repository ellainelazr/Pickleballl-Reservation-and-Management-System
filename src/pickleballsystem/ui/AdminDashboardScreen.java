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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import pickleballsystem.ui.BaseScreen;
import pickleballsystem.PickleballMain;

import static pickleballsystem.ui.UiHelper.*;

/** Admin home screen, sidebar, and the account unlock dialog. */
public class AdminDashboardScreen extends BaseScreen {

    public AdminDashboardScreen(PickleballMain app) {
        super(app);
    }

    public void showAdminDashboard() {
        JFrame dashboardFrame = new JFrame("SmashPoint Pickleball - Admin Dashboard");
        dashboardFrame.setSize(1320, 780);
        dashboardFrame.setMinimumSize(new Dimension(1050, 650));
        dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboardFrame.setLocationRelativeTo(null);

        JPanel headerPanel = app.createHeaderPanel("Admin Dashboard", dashboardFrame, true);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createAdminSidebar(dashboardFrame, "Dashboard"), BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 22));
        contentPanel.setBackground(INTRO_MINT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(INTRO_MINT);
        JLabel welcomeTitle = new JLabel("Dashboard Overview");
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 27));
        welcomeTitle.setForeground(INTRO_TEAL);
        JLabel welcomeText = new JLabel("Monitor court bookings, rental equipment, and business performance.");
        welcomeText.setFont(new Font("Arial", Font.PLAIN, 14));
        welcomeText.setForeground(INTRO_MUTED);
        titlePanel.add(welcomeTitle);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(welcomeText);

        JPanel summaryPanel = new JPanel(new GridLayout(3, 2, 18, 18));
        summaryPanel.setBackground(INTRO_MINT);
        summaryPanel.add(createAdminSummaryCard("TOTAL REVENUE",
                "₱" + String.format("%.2f", data.getTotalRevenue()), "From active and completed bookings", TEAL));
        summaryPanel.add(createAdminSummaryCard("ESTIMATED PROFIT",
                "₱" + String.format("%.2f", data.getTotalRevenue() - data.getOperatingExpense()),
                "Revenue minus ₱" + String.format("%.2f", data.getOperatingExpense()) + " expenses", GREEN));
        summaryPanel.add(createAdminSummaryCard("EXPECTED GUESTS",
                String.valueOf(data.getExpectedGuestCount()), "Players expected from active reservations", INTRO_TEAL));
        summaryPanel.add(createAdminSummaryCard("ACTIVE PLAYERS ONSITE",
                String.valueOf(data.getActivePlayersOnsite()), "Players checked in by staff", new Color(16, 145, 235)));
        summaryPanel.add(createAdminSummaryCard("ACTIVE COURTS",
                String.valueOf(data.getCourts().size()), "Courts available for booking", new Color(220, 160, 40)));
        summaryPanel.add(createAdminSummaryCard("ONLINE / ONSITE BOOKINGS",
                data.getActiveBookingCount("Online") + " / " + data.getActiveBookingCount("Onsite"),
                "Active reservations by booking source", new Color(135, 91, 239)));

        JPanel dashboardContent = new JPanel(new BorderLayout(0, 24));
        dashboardContent.setBackground(INTRO_MINT);
        dashboardContent.add(titlePanel, BorderLayout.NORTH);
        dashboardContent.add(summaryPanel, BorderLayout.CENTER);

        JButton manageRatesButton = createAdminActionButton("Manage Prices & Expenses");
        manageRatesButton.addActionListener(e -> {
            dashboardFrame.dispose();
            app.showAdminPricingScreen();
        });
        JButton unlockButton = createAdminActionButton("Unlock Locked Accounts");
        unlockButton.addActionListener(e -> showAccountUnlockDialog(dashboardFrame));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setBackground(INTRO_MINT);
        actionPanel.add(manageRatesButton);
        actionPanel.add(unlockButton);
        dashboardContent.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(dashboardContent, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        dashboardFrame.add(headerPanel, BorderLayout.NORTH);
        dashboardFrame.add(mainPanel, BorderLayout.CENTER);
        dashboardFrame.setVisible(true);
    }

    private void showAccountUnlockDialog(JFrame parentFrame) {
        if (accounts.getLockedAccounts().isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "No locked accounts are currently waiting for review.", "No Locked Accounts", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] usernames = accounts.getLockedAccounts().toArray(new String[0]);
        JComboBox<String> selector = new JComboBox<>(usernames);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Select a locked account to unlock:"), BorderLayout.NORTH);
        panel.add(selector, BorderLayout.CENTER);

        int answer = JOptionPane.showConfirmDialog(parentFrame, panel, "Unlock Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer == JOptionPane.OK_OPTION) {
            String selected = (String) selector.getSelectedItem();
            if (selected != null && !selected.isBlank()) {
                accounts.getLockedAccounts().remove(selected.toLowerCase(Locale.ROOT));
                accounts.getFailedLoginAttempts().remove(selected.toLowerCase(Locale.ROOT));
                JOptionPane.showMessageDialog(parentFrame, "The account has been unlocked.", "Account Unlocked", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public JPanel createAdminSidebar(JFrame currentFrame, String selectedPage) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(INTRO_MINT);
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 10, 20, 10));
        sidebar.setPreferredSize(new Dimension(225, 0));

        JLabel menuTitle = new JLabel("  ADMIN MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 12));
        menuTitle.setForeground(INTRO_MUTED);
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(menuTitle);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createAdminSidebarButton("Dashboard", "Dashboard".equals(selectedPage), () -> {
            currentFrame.dispose();
            showAdminDashboard();
        }));
        sidebar.add(createAdminSidebarButton("Court Management", "Court Management".equals(selectedPage), () -> {
            currentFrame.dispose();
            app.showAdminCourtAvailabilityScreen();
        }));
        sidebar.add(createAdminSidebarButton("Month Availability", "Month Availability".equals(selectedPage), () -> {
            app.manageBookingMonths(currentFrame);
        }));
        sidebar.add(createAdminSidebarButton("Equipment Management", "Equipment Management".equals(selectedPage), () -> {
            currentFrame.dispose();
            app.showAdminEquipmentScreen();
        }));
        sidebar.add(createAdminSidebarButton("Reservations", "Reservations".equals(selectedPage), () -> {
            currentFrame.dispose();
            app.showAdminReservationsScreen();
        }));
        sidebar.add(createAdminSidebarButton("Booking History", "Booking History".equals(selectedPage), () -> {
            currentFrame.dispose();
            app.showAdminBookingHistoryScreen();
        }));
        sidebar.add(createAdminSidebarButton("Staff Management", "Staff Management".equals(selectedPage), () -> {
            currentFrame.dispose();
            app.showAdminStaffScreen();
        }));
        sidebar.add(createAdminSidebarButton("Pricing & Expenses", "Pricing & Expenses".equals(selectedPage), () -> {
            currentFrame.dispose();
            app.showAdminPricingScreen();
        }));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createAdminSidebarButton("Change Password", false, () -> {
            app.showPasswordChangeDialog(currentFrame);
        }));
        sidebar.add(createAdminSidebarButton("Logout", false, () -> {
            currentFrame.dispose();
            app.showLoginScreen();
        }));
        return sidebar;
    }
}
