package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.model.Court;
import pickleballsystem.PickleballMain;

import static pickleballsystem.ui.UiHelper.*;

/** Dialog that collects the guest details and validates them. */
public class GuestBookingDialog extends BaseScreen {

    public GuestBookingDialog(PickleballMain app) {
        super(app);
    }

    public void showGuestBookingDialog(JFrame parentFrame, DefaultTableModel model,
                                        JTable availabilityTable, Runnable updateSelection) {
        JDialog guestDialog = new JDialog(parentFrame, "Guest Booking", true);
        guestDialog.setSize(720, 780);
        guestDialog.setMinimumSize(new Dimension(620, 650));
        guestDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        guestDialog.setLocationRelativeTo(parentFrame);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(INTRO_MINT);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 26, 16, 26));
        JLabel titleLabel = new JLabel("Complete Your Booking");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 23));
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitleLabel = new JLabel("Enter your details and review the pricing");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(INTRO_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(INTRO_MINT);
        formPanel.setBorder(BorderFactory.createEmptyBorder(18, 26, 20, 26));

        JPanel bookingCard = createGuestBookingCard("Selected Courts & Times");
        String[] columns = {"Court", "Date", "Time", "Rate"};
        DefaultTableModel bookingModel = readOnlyTableModel(columns);
        for (String timeSlot : data.getAvailableTimeSlots()) {
            for (Court court : data.getCourts()) {
                String slotKey = court.getCourtName() + "|" + data.getSelectedReservationDate() + "|" + timeSlot;
                if (data.getSelectedCourtSlots().contains(slotKey)) {
                    double rate = data.getCourtRate(court.getCourtName(), timeSlot);
                    bookingModel.addRow(new Object[] {
                        court.getCourtName(), formatBookingDate(data.getSelectedReservationDate()),
                        timeSlot, "₱" + String.format("%.2f", rate)
                    });
                }
            }
        }
        JTable bookingTable = new JTable(bookingModel);
        bookingTable.setRowHeight(28);
        bookingTable.setFont(new Font("Arial", Font.PLAIN, 12));
        bookingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        bookingTable.getTableHeader().setForeground(INTRO_MUTED);
        bookingTable.setFillsViewportHeight(true);
        JScrollPane bookingScrollPane = new JScrollPane(bookingTable);
        bookingScrollPane.setBorder(BorderFactory.createLineBorder(new Color(225, 232, 235)));
        bookingScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookingScrollPane.setPreferredSize(new Dimension(590,
                Math.min(145, Math.max(58, (data.getSelectedCourtSlots().size() + 1) * 28))));
        bookingCard.add(bookingScrollPane);
        formPanel.add(bookingCard);
        formPanel.add(Box.createVerticalStrut(12));

        JPanel pricingCard = createGuestBookingCard("Pricing Summary");
        JLabel rentalLabel = new JLabel("Court rental (" + data.getSelectedCourtSlots().size() + " hour"
                + (data.getSelectedCourtSlots().size() == 1 ? "" : "s") + "):  ₱"
                + String.format("%.2f", data.calculateSelectedPrice()));
        rentalLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        rentalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel paddleRentalLabel = new JLabel("Paddle rental:  ₱"
                + String.format("%.2f", data.getPaddleRentalTotal()));
        paddleRentalLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        paddleRentalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ballRentalLabel = new JLabel("Ball rental:  ₱"
                + String.format("%.2f", data.getBallRentalTotal()));
        ballRentalLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        ballRentalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel totalLabel = new JLabel("Total due:  ₱" + String.format("%.2f", data.getSelectedBookingTotal()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 17));
        totalLabel.setForeground(TEAL);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pricingCard.add(rentalLabel);
        pricingCard.add(Box.createVerticalStrut(7));
        pricingCard.add(paddleRentalLabel);
        pricingCard.add(Box.createVerticalStrut(7));
        pricingCard.add(ballRentalLabel);
        pricingCard.add(Box.createVerticalStrut(7));
        pricingCard.add(Box.createVerticalStrut(9));
        pricingCard.add(new JSeparator());
        pricingCard.add(Box.createVerticalStrut(9));
        pricingCard.add(totalLabel);
        formPanel.add(pricingCard);
        formPanel.add(Box.createVerticalStrut(12));

        JPanel guestCard = createGuestBookingCard("Your Information");
        JLabel instructionLabel = new JLabel("We will use these details to identify your reservation.");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionLabel.setForeground(INTRO_MUTED);
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        guestCard.add(instructionLabel);
        guestCard.add(Box.createVerticalStrut(10));

        JTextField fullNameField = new JTextField(
                accounts.isAdminLoggedIn() || accounts.isStaffLoggedIn() ? "" : accounts.getCurrentUserName());
        JTextField emailField = new JTextField();
        JTextField confirmEmailField = new JTextField();
        JTextField contactField = new JTextField();
        ((AbstractDocument) contactField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text,
                                     AttributeSet attributes) throws BadLocationException {
                replace(fb, offset, 0, text, attributes);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text,
                                AttributeSet attributes) throws BadLocationException {
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String updated = current.substring(0, offset)
                        + (text == null ? "" : text)
                        + current.substring(offset + length);
                if (updated.matches("[0-9+() \\-]*") && updated.length() <= 20) {
                    fb.replace(offset, length, text, attributes);
                }
            }
        });
        fullNameField.setToolTipText("Enter your first and last name.");
        emailField.setToolTipText("Example: name@example.com");
        confirmEmailField.setToolTipText("Enter the same email address again.");
        contactField.setToolTipText("Enter a phone number where we can reach you.");

        guestCard.add(createGuestInputRow("Customer Name *", fullNameField));
        if (!accounts.isStaffLoggedIn()) {
            guestCard.add(Box.createVerticalStrut(10));
            guestCard.add(createGuestInputRow("Email *", emailField));
            guestCard.add(Box.createVerticalStrut(10));
            guestCard.add(createGuestInputRow("Confirm Email *", confirmEmailField));
            guestCard.add(Box.createVerticalStrut(10));
            guestCard.add(createGuestInputRow("Contact Number *", contactField));
        }
        guestCard.add(Box.createVerticalStrut(12));

        JPanel playersPanel = new JPanel(new BorderLayout(12, 0));
        playersPanel.setBackground(WHITE);
        playersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel playersLabel = new JLabel("Number of Players");
        playersLabel.setFont(new Font("Arial", Font.BOLD, 13));
        SpinnerNumberModel playersModel = new SpinnerNumberModel(2, 1, 20, 1);
        JSpinner playersSpinner = new JSpinner(playersModel);
        playersSpinner.setPreferredSize(new Dimension(90, 34));
        playersPanel.add(playersLabel, BorderLayout.WEST);
        playersPanel.add(playersSpinner, BorderLayout.EAST);
        guestCard.add(playersPanel);
        formPanel.add(guestCard);
        formPanel.add(Box.createVerticalStrut(12));

        final JCheckBox waiverCheck;
        final JCheckBox cancellationCheck;
        if (!accounts.isStaffLoggedIn()) {
            JPanel policiesCard = createGuestBookingCard("Booking Policies");
            waiverCheck = createGuestPolicyCheckBox("I accept the pickleball venue waiver policy.");
            cancellationCheck = createGuestPolicyCheckBox("I have read and accept the cancellation policy.");
            JButton waiverButton = createPolicyLink("Read waiver", "Waiver Policy",
                    "All players are expected to follow venue safety rules and staff instructions. "
                    + "Players are responsible for using the courts and equipment safely.");
            JButton cancellationButton = createPolicyLink("Read policy", "Cancellation Policy",
                    "To request a cancellation, please contact us at smashpoint@gmail.com. "
                        + "Refund requests are subject to review and approval by venue staff.");
            policiesCard.add(createPolicyRow(waiverCheck, waiverButton));
            policiesCard.add(Box.createVerticalStrut(8));
            policiesCard.add(createPolicyRow(cancellationCheck, cancellationButton));
            formPanel.add(policiesCard);
        } else {
            waiverCheck = null;
            cancellationCheck = null;
        }

        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.getViewport().setBackground(INTRO_MINT);
        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(formScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 232, 235)));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 13));
        cancelButton.setPreferredSize(new Dimension(115, 42));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> guestDialog.dispose());

        JButton paymentButton = new JButton("Select Payment Method");
        paymentButton.setFont(new Font("Arial", Font.BOLD, 13));
        paymentButton.setBackground(TEAL);
        paymentButton.setForeground(WHITE);
        paymentButton.setPreferredSize(new Dimension(205, 42));
        paymentButton.setFocusPainted(false);
        paymentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        paymentButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String confirmEmail = confirmEmailField.getText().trim();
            String contactNumber = contactField.getText().trim();
            String validationMessage = accounts.isStaffLoggedIn()
                    ? (fullName.length() < 2 ? "Please enter the customer's name." : null)
                    : validateGuestDetails(fullName, email, confirmEmail, contactNumber);
            if (validationMessage != null) {
                JOptionPane.showMessageDialog(guestDialog, validationMessage,
                        "Check Your Details", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!accounts.isStaffLoggedIn() && (!waiverCheck.isSelected() || !cancellationCheck.isSelected())) {
                JOptionPane.showMessageDialog(guestDialog,
                        "Please review and accept each booking policy before continuing.",
                        "Policy Agreement Required", JOptionPane.ERROR_MESSAGE);
                return;
            }

            app.showPaymentMethodDialog(parentFrame, guestDialog, model, availabilityTable,
                    updateSelection, fullName, email, contactNumber, (Integer) playersSpinner.getValue());
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(paymentButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        guestDialog.setContentPane(contentPanel);
        guestDialog.setVisible(true);
    }

    private String validateGuestDetails(String fullName, String email, String confirmEmail,
                                        String contactNumber) {
        if (fullName.length() < 2) {
            return "Please enter your full name.";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Please enter a valid email address, such as name@example.com.";
        }
        if (!email.equalsIgnoreCase(confirmEmail)) {
            return "The email addresses do not match. Please check and try again.";
        }
        String digitsOnly = contactNumber.replaceAll("\\D", "");
        if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
            return "Please enter a valid contact number with 7 to 15 digits.";
        }
        return null;
    }
}
