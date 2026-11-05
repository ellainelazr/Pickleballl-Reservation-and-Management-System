package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import pickleballsystem.PickleballMain;

import static pickleballsystem.ui.UiHelper.*;

/** Home screen shown to customers and staff after login. */
public class CustomerDashboardScreen extends BaseScreen {

    public CustomerDashboardScreen(PickleballMain app) {
        super(app);
    }

    public void showCustomerDashboard() {
        String dashboardTitle = accounts.isStaffLoggedIn() ? "Staff Dashboard" : "Customer Dashboard";
        JFrame dashboardFrame = new JFrame("SmashPoint Pickleball - " + dashboardTitle);
        dashboardFrame.setSize(1200, 700);
        dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboardFrame.setLocationRelativeTo(null);

        JPanel headerPanel = app.createHeaderPanel(dashboardTitle + " - " + accounts.getCurrentUserName(), dashboardFrame, false);
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel navPanel = app.createNavPanel(dashboardFrame, () -> {
            app.showCourtReservationScreen();
            dashboardFrame.dispose();
        }, () -> {
            app.showCourtLayoutDialog(dashboardFrame);
        }, accounts.isStaffLoggedIn() ? () -> {
            dashboardFrame.dispose();
            app.showStaffOnsiteScreen();
        } : null, () -> {
            dashboardFrame.dispose();
            app.showLoginScreen();
        });

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(LIGHT_GRAY);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeTitle = new JLabel("Welcome, " + accounts.getCurrentUserName() + "!");
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeTitle.setForeground(TEAL);

        JTextArea welcomeText = createWrappedTextArea(
                "Book a pickleball court today. Click available time slots to select your desired duration, then submit your reservation request.",
                new Font("Arial", Font.PLAIN, 14), DARK_TEXT);

        welcomePanel.add(welcomeTitle);
        welcomePanel.add(Box.createVerticalStrut(10));
        welcomePanel.add(welcomeText);

        contentPanel.add(navPanel, BorderLayout.NORTH);
        contentPanel.add(welcomePanel, BorderLayout.CENTER);

        dashboardFrame.add(headerPanel, BorderLayout.NORTH);
        dashboardFrame.add(contentPanel, BorderLayout.CENTER);
        dashboardFrame.setVisible(true);
    }
}
