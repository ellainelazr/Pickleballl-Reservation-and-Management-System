package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.PickleballMain;

import static pickleballsystem.ui.UiHelper.*;

/** Dialog for choosing the payment method. */
public class PaymentDialog extends BaseScreen {

    public PaymentDialog(PickleballMain app) {
        super(app);
    }

    public void showPaymentMethodDialog(JFrame parentFrame, JDialog guestDialog,
                                         DefaultTableModel model, JTable availabilityTable,
                                         Runnable updateSelection, String customerName,
                                         String customerEmail, String contactNumber, int numberOfPlayers) {
        JDialog paymentDialog = new JDialog(guestDialog, "Payment", true);
        paymentDialog.setSize(640, 680);
        paymentDialog.setMinimumSize(new Dimension(560, 600));
        paymentDialog.setLocationRelativeTo(guestDialog);
        paymentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(INTRO_MINT);

        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setBackground(WHITE);
        headingPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        JLabel heading = new JLabel("Choose Payment Method");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(DARK_TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Select how you plan to pay at the venue.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(INTRO_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(subtitle);

        JPanel paymentContent = new JPanel();
        paymentContent.setLayout(new BoxLayout(paymentContent, BoxLayout.Y_AXIS));
        paymentContent.setBackground(INTRO_MINT);
        paymentContent.setBorder(BorderFactory.createEmptyBorder(16, 24, 18, 24));

        JPanel customerCard = new JPanel(new BorderLayout(12, 0));
        customerCard.setBackground(WHITE);
        customerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 232)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        customerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel customerDetails = new JPanel();
        customerDetails.setLayout(new BoxLayout(customerDetails, BoxLayout.Y_AXIS));
        customerDetails.setBackground(WHITE);
        JLabel customerNameLabel = new JLabel(customerName);
        customerNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        customerNameLabel.setForeground(DARK_TEXT);
        customerDetails.add(customerNameLabel);
        if (!customerEmail.isEmpty()) {
            JLabel emailLabel = new JLabel(customerEmail);
            emailLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            emailLabel.setForeground(INTRO_MUTED);
            customerDetails.add(Box.createVerticalStrut(4));
            customerDetails.add(emailLabel);
        }
        if (!contactNumber.isEmpty()) {
            JLabel contactLabel = new JLabel(contactNumber);
            contactLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            contactLabel.setForeground(INTRO_MUTED);
            customerDetails.add(Box.createVerticalStrut(2));
            customerDetails.add(contactLabel);
        }
        JPanel amount = new JPanel();
        amount.setLayout(new BoxLayout(amount, BoxLayout.Y_AXIS));
        amount.setOpaque(false);
        JLabel amountHeading = new JLabel("Total Amount");
        amountHeading.setFont(new Font("Arial", Font.PLAIN, 12));
        amountHeading.setForeground(INTRO_MUTED);
        JLabel amountValue = new JLabel("₱" + String.format("%.2f", data.getSelectedBookingTotal()));
        amountValue.setFont(new Font("Arial", Font.BOLD, 16));
        amountValue.setForeground(new Color(232, 106, 27));
        amount.add(amountHeading);
        amount.add(amountValue);
        customerCard.add(customerDetails, BorderLayout.CENTER);
        customerCard.add(amount, BorderLayout.EAST);
        paymentContent.add(customerCard);
        paymentContent.add(Box.createVerticalStrut(16));

        JLabel methodsHeading = new JLabel("Payment Method");
        methodsHeading.setFont(new Font("Arial", Font.BOLD, 15));
        methodsHeading.setForeground(DARK_TEXT);
        methodsHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentContent.add(methodsHeading);
        paymentContent.add(Box.createVerticalStrut(10));

        ButtonGroup methodsGroup = new ButtonGroup();
        JPanel methodsPanel = new JPanel();
        methodsPanel.setLayout(new BoxLayout(methodsPanel, BoxLayout.Y_AXIS));
        methodsPanel.setBackground(INTRO_MINT);
        methodsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] paymentMethods = accounts.isStaffLoggedIn()
                ? new String[] {"QRPh", "GCash", "Maya", "GrabPay", "Cash"}
                : new String[] {"QRPh", "GCash", "Maya", "GrabPay"};
        JRadioButton[] methodButtons = new JRadioButton[paymentMethods.length];
        for (int i = 0; i < paymentMethods.length; i++) {
            JRadioButton methodButton = new JRadioButton(paymentMethods[i]);
            methodButton.setActionCommand(paymentMethods[i]);
            methodButton.setFont(new Font("Arial", Font.PLAIN, 14));
            methodButton.setForeground(DARK_TEXT);
            methodButton.setBackground(WHITE);
            methodButton.setFocusPainted(false);
            methodButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 228, 232)),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            methodButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            methodButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            methodsGroup.add(methodButton);
            methodsPanel.add(methodButton);
            methodsPanel.add(Box.createVerticalStrut(8));
            methodButtons[i] = methodButton;
        }
        methodButtons[accounts.isStaffLoggedIn() ? paymentMethods.length - 1 : 0].setSelected(true);
        paymentContent.add(methodsPanel);
        paymentContent.add(Box.createVerticalStrut(4));

        JScrollPane paymentScrollPane = new JScrollPane(paymentContent);
        paymentScrollPane.setBorder(null);
        paymentScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        paymentScrollPane.getViewport().setBackground(INTRO_MINT);

        JPanel paymentButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        paymentButtons.setBackground(WHITE);
        paymentButtons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                new Color(225, 232, 235)));

        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(130, 42));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> paymentDialog.dispose());

        JButton completeButton = new JButton("Confirm Booking");
        styleButton(completeButton, TEAL, WHITE, 13, 190, 42);
        completeButton.addActionListener(e -> {
            String paymentMethod = methodsGroup.getSelection().getActionCommand();
            paymentDialog.dispose();
            guestDialog.dispose();
            app.confirmMultipleSlotReservation(parentFrame, model,
                    customerName, customerEmail, contactNumber, numberOfPlayers, paymentMethod);
            app.refreshAvailabilityTable(model, data.getSelectedReservationDate());
            availabilityTable.repaint();
            updateSelection.run();
        });

        paymentButtons.add(backButton);
        paymentButtons.add(completeButton);
        contentPanel.add(headingPanel, BorderLayout.NORTH);
        contentPanel.add(paymentScrollPane, BorderLayout.CENTER);
        contentPanel.add(paymentButtons, BorderLayout.SOUTH);
        paymentDialog.setContentPane(contentPanel);
        paymentDialog.setVisible(true);
    }
}
