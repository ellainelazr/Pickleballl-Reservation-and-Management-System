package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import pickleballsystem.ui.BaseScreen;
import pickleballsystem.PickleballMain;

import static pickleballsystem.ui.UiHelper.*;

/** Welcome screen with the venue introduction and court layout. */
public class IntroScreen extends BaseScreen {

    public IntroScreen(PickleballMain app) {
        super(app);
    }

    public void showIntroScreen() {
        JFrame introFrame = new JFrame("SmashPoint Pickleball");
        introFrame.setSize(1366, 768);
        introFrame.setMinimumSize(new Dimension(1000, 700));
        introFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        introFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(INTRO_MINT);

        JPanel headerPanel = new JPanel(new BorderLayout(30, 0));
        headerPanel.setBackground(INTRO_DARK_TEAL);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 48, 12, 42));

        JPanel logoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        logoPanel.setBackground(INTRO_DARK_TEAL);
        JLabel ballLogo = new JLabel("●");
        ballLogo.setFont(new Font("Arial", Font.BOLD, 48));
        ballLogo.setForeground(INTRO_YELLOW);
        JLabel brandLabel = new JLabel("SmashPoint Pickleball");
        brandLabel.setFont(new Font("Arial", Font.BOLD, 23));
        brandLabel.setForeground(WHITE);
        logoPanel.add(ballLogo);
        logoPanel.add(brandLabel);

        JPanel navPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        navPanel.setBackground(INTRO_DARK_TEAL);
        JButton homeButton = new JButton("  Home");
        JButton courtLayoutButton = new JButton("▦  Court Layout");
        JButton aboutButton = new JButton("ⓘ  How It Works");
        JButton[] navButtons = {homeButton, courtLayoutButton, aboutButton};
        for (JButton button : navButtons) {
            button.setFont(new Font("Arial", Font.BOLD, 13));
            button.setForeground(WHITE);
            button.setBackground(INTRO_DARK_TEAL);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        homeButton.setBackground(new Color(0, 133, 136));
        courtLayoutButton.addActionListener(e -> showCourtLayoutDialog(introFrame));
        aboutButton.addActionListener(e -> JOptionPane.showMessageDialog(introFrame,
                "Booking is simple:\n1. Choose a date and available court time.\n"
                        + "2. Choose optional paddle and ball rentals.\n"
                        + "3. Review the total, enter your details, and confirm.",
                "How to Book",
                JOptionPane.INFORMATION_MESSAGE));
        navPanel.add(homeButton);
        navPanel.add(courtLayoutButton);
        navPanel.add(aboutButton);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 13));
        loginButton.setForeground(WHITE);
        loginButton.setBackground(INTRO_DARK_TEAL);
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(139, 215, 211), 1, true));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(155, 42));
        loginButton.addActionListener(e -> {
            introFrame.dispose();
            app.showLoginScreen();
        });

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.CENTER);
        headerPanel.add(loginButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(INTRO_MINT);

        JPanel heroPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        heroPanel.setBackground(INTRO_MINT);
        heroPanel.setBorder(BorderFactory.createEmptyBorder(30, 72, 24, 72));

        JPanel heroTextPanel = new JPanel();
        heroTextPanel.setLayout(new BoxLayout(heroTextPanel, BoxLayout.Y_AXIS));
        heroTextPanel.setBackground(INTRO_MINT);
        JLabel welcomeTitle = new JLabel("Your court. Your time. Your game.");
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 39));
        welcomeTitle.setForeground(INTRO_TEAL);
        welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea welcomeText = createWrappedTextArea(
                "Book a court and rent paddles or balls in one easy reservation. No customer account or sign-in required.",
                new Font("Arial", Font.PLAIN, 17), INTRO_MUTED);
        welcomeText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton bookButton = new JButton("Book a Court");
        bookButton.setFont(new Font("Arial", Font.BOLD, 17));
        bookButton.setForeground(WHITE);
        bookButton.setBackground(INTRO_TEAL);
        bookButton.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        bookButton.setFocusPainted(false);
        bookButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookButton.setHorizontalAlignment(SwingConstants.CENTER);
        bookButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookButton.addActionListener(e -> {
            introFrame.dispose();
            app.showCourtReservationScreen(true);
        });

        heroTextPanel.add(welcomeTitle);
        heroTextPanel.add(Box.createVerticalStrut(14));
        heroTextPanel.add(welcomeText);
        heroTextPanel.add(Box.createVerticalStrut(20));
        heroTextPanel.add(bookButton);
        heroTextPanel.add(Box.createVerticalStrut(12));
        JLabel noAccountLabel = new JLabel("Pick a time first. Enter your name when you confirm.");
        noAccountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        noAccountLabel.setForeground(INTRO_MUTED);
        noAccountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroTextPanel.add(noAccountLabel);

        heroPanel.add(heroTextPanel);
        heroPanel.add(createCourtSummaryPanel());
        centerPanel.add(heroPanel, BorderLayout.CENTER);

        JPanel quickAccessPanel = new JPanel(new BorderLayout(0, 14));
        quickAccessPanel.setBackground(new Color(242, 248, 250));
        quickAccessPanel.setBorder(BorderFactory.createEmptyBorder(18, 72, 22, 72));
        JLabel quickAccessTitle = new JLabel("Book a court and rent equipment");
        quickAccessTitle.setFont(new Font("Arial", Font.BOLD, 22));
        quickAccessTitle.setForeground(INTRO_TEAL);
        quickAccessPanel.add(quickAccessTitle, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsPanel.setBackground(new Color(242, 248, 250));
        cardsPanel.add(createIntroCard("1", "Choose a date", "Pick the day you want to play.", new Color(18, 194, 159)));
        cardsPanel.add(createIntroCard("2", "Select a court time", "Click a green Available slot. Red slots are already booked.", new Color(16, 145, 235)));
        cardsPanel.add(createIntroCard("3", "Add gear & confirm", "Rent optional paddles or balls, then review the total and confirm.", new Color(135, 91, 239)));
        quickAccessPanel.add(cardsPanel, BorderLayout.CENTER);
        centerPanel.add(quickAccessPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(INTRO_DARK_TEAL);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 65, 15, 65));
        JLabel footerBrand = new JLabel("●   SmashPoint Pickleball");
        footerBrand.setFont(new Font("Arial", Font.BOLD, 13));
        footerBrand.setForeground(WHITE);
        JLabel footerTagline = new JLabel("Play  ·  Connect  ·  Enjoy");
        footerTagline.setFont(new Font("Arial", Font.BOLD, 12));
        footerTagline.setForeground(WHITE);
        footerPanel.add(footerBrand, BorderLayout.WEST);
        footerPanel.add(footerTagline, BorderLayout.EAST);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        introFrame.setContentPane(mainPanel);
        introFrame.setVisible(true);
    }

    private JPanel createCourtSummaryPanel() {
        JPanel courtSummaryPanel = new JPanel(new BorderLayout(0, 10));
        courtSummaryPanel.setBackground(WHITE);
        courtSummaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 232, 229)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel courtImage = createCourtImageLabel(540, 245);
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        detailsPanel.setBackground(WHITE);

        JLabel courtCount = new JLabel(data.getCourts().size() + " courts", SwingConstants.CENTER);
        courtCount.setFont(new Font("Arial", Font.BOLD, 38));
        courtCount.setForeground(INTRO_TEAL);
        JLabel rateDescription = new JLabel(
                "Rates from ₱300 per hour. See each time's price before booking.",
                SwingConstants.CENTER);
        rateDescription.setFont(new Font("Arial", Font.PLAIN, 15));
        rateDescription.setForeground(INTRO_TEAL);
        detailsPanel.add(courtCount);
        detailsPanel.add(rateDescription);
        courtSummaryPanel.add(courtImage, BorderLayout.CENTER);
        courtSummaryPanel.add(detailsPanel, BorderLayout.SOUTH);
        return courtSummaryPanel;
    }

    private JPanel createCourtLayoutPanel() {
        JPanel layoutPanel = new JPanel(new BorderLayout(0, 12));
        layoutPanel.setBackground(WHITE);
        layoutPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Court Layout", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(INTRO_TEAL);

        JLabel courtImage = createCourtImageLabel(760, 420);
        layoutPanel.add(title, BorderLayout.NORTH);
        layoutPanel.add(courtImage, BorderLayout.CENTER);
        return layoutPanel;
    }

    public void showCourtLayoutDialog(JFrame parentFrame) {
        JDialog layoutDialog = new JDialog(parentFrame, "Court Layout", true);
        layoutDialog.setSize(820, 560);
        layoutDialog.setLocationRelativeTo(parentFrame);
        layoutDialog.add(createCourtLayoutPanel());
        layoutDialog.setVisible(true);
    }
}
