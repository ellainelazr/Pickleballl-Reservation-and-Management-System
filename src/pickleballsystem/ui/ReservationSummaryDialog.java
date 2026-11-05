package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.ui.BaseScreen;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.RentalItem;

import static pickleballsystem.ui.UiHelper.*;

/** Dialog that summarizes the selected slots and rentals before booking. */
public class ReservationSummaryDialog extends BaseScreen {

    public ReservationSummaryDialog(PickleballMain app) {
        super(app);
    }

    public void showReservationSummaryDialog(JFrame parentFrame, DefaultTableModel model,
                                              JTable availabilityTable, Runnable updateSelection) {
        JDialog summaryDialog = new JDialog(parentFrame, "Booking Summary", true);
        summaryDialog.setSize(620, 620);
        summaryDialog.setMinimumSize(new Dimension(560, 520));
        summaryDialog.setLocationRelativeTo(parentFrame);
        summaryDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(INTRO_MINT);
        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setBackground(WHITE);
        headingPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        JLabel heading = new JLabel("Booking Summary");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(DARK_TEXT);
        JLabel subtitle = new JLabel("Review your reservation details before confirming.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(INTRO_MUTED);
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(subtitle);

        JPanel summaryContent = new JPanel();
        summaryContent.setLayout(new BoxLayout(summaryContent, BoxLayout.Y_AXIS));
        summaryContent.setBackground(INTRO_MINT);
        summaryContent.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel courtCard = createGuestBookingCard("Court Details");
        courtCard.add(createDetailRow("Date:", formatBookingDate(data.getSelectedReservationDate()), DARK_TEXT));
        courtCard.add(createDetailRow("Selected court-hours:",
                String.valueOf(data.getSelectedCourtSlots().size()), DARK_TEXT));
        for (String slotKey : data.getSelectedSlotKeysInOrder()) {
            String[] parts = slotKey.split("\\|");
            courtCard.add(createDetailRow(parts[0] + " · " + parts[2],
                    "₱" + String.format("%.2f", data.getCourtRate(parts[0], parts[2])), DARK_TEXT));
        }
        summaryContent.add(courtCard);
        summaryContent.add(Box.createVerticalStrut(12));

        JPanel rentalCard = createGuestBookingCard("Equipment Rental");
        rentalCard.add(createDetailRow("Paddles:",
                data.getSelectedPaddleQuantity() + " × ₱" + String.format("%.2f", data.getPaddleRentalRate())
                        + "/hour = ₱" + String.format("%.2f", data.getPaddleRentalTotal()), DARK_TEXT));
        rentalCard.add(createDetailRow("Balls:",
                data.getSelectedBallQuantity() + " × ₱" + String.format("%.2f", data.getBallRentalRate())
                        + "/hour = ₱" + String.format("%.2f", data.getBallRentalTotal()), DARK_TEXT));
        ArrayList<RentalItem> additionalItems = data.getAdditionalRentalItems();
        for (int i = 0; i < additionalItems.size() && i < data.getExtraRentalQuantities().size(); i++) {
            RentalItem item = additionalItems.get(i);
            int quantity = data.getExtraRentalQuantities().get(i);
            if (quantity > 0) {
                rentalCard.add(createDetailRow(item.getName() + ":",
                        quantity + " × ₱" + String.format("%.2f", item.getHourlyRate())
                                + "/hour = ₱" + String.format("%.2f", data.getExtraRentalTotal(item, quantity)),
                        DARK_TEXT));
            }
        }
        summaryContent.add(rentalCard);
        summaryContent.add(Box.createVerticalStrut(12));

        JPanel totalCard = createGuestBookingCard("Total Amount");
        JLabel total = new JLabel("₱" + String.format("%.2f", data.getSelectedBookingTotal()));
        total.setFont(new Font("Arial", Font.BOLD, 23));
        total.setForeground(TEAL);
        totalCard.add(total);
        summaryContent.add(totalCard);

        JScrollPane summaryScrollPane = new JScrollPane(summaryContent);
        summaryScrollPane.setBorder(null);
        summaryScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        summaryScrollPane.getViewport().setBackground(INTRO_MINT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(WHITE);
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(120, 42));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            summaryDialog.dispose();
            app.showEquipmentRentalDialog(parentFrame, model, availabilityTable, updateSelection);
        });
        JButton confirmButton = new JButton("Confirm Reservation");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 13));
        confirmButton.setBackground(TEAL);
        confirmButton.setForeground(WHITE);
        confirmButton.setPreferredSize(new Dimension(220, 42));
        confirmButton.setFocusPainted(false);
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.addActionListener(e -> {
            summaryDialog.dispose();
            app.showGuestBookingDialog(parentFrame, model, availabilityTable, updateSelection);
        });
        buttonPanel.add(backButton);
        buttonPanel.add(confirmButton);
        contentPanel.add(headingPanel, BorderLayout.NORTH);
        contentPanel.add(summaryScrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        summaryDialog.setContentPane(contentPanel);
        summaryDialog.setVisible(true);
    }
}
