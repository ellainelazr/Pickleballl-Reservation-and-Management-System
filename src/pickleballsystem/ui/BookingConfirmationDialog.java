package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.RentalItem;
import pickleballsystem.model.Reservation;
import pickleballsystem.model.ReservationSlot;

import static pickleballsystem.ui.UiHelper.*;

/** Creates the reservations and shows the booking confirmation. */
public class BookingConfirmationDialog extends BaseScreen {

    public BookingConfirmationDialog(PickleballMain app) {
        super(app);
    }

    // Confirm multiple slot reservation
    public void confirmMultipleSlotReservation(JFrame parentFrame, DefaultTableModel model,
                                                String customerName, String customerEmail,
                                                String contactNumber, int numberOfPlayers,
                                                String paymentMethod) {
        if (data.getSelectedCourtSlots().isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Please select at least one court time slot.",
                    "Selection Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (data.isReservationDateBlocked(data.getSelectedReservationDate())) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Reservations are not accepted on " + data.getSelectedReservationDate() + ".",
                    "Date Unavailable",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!accounts.isStaffLoggedIn() && !data.isBookingMonthOpen(data.getSelectedReservationDate())) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Online reservations are not open for "
                    + YearMonth.from(LocalDate.parse(data.getSelectedReservationDate())) + ".",
                    "Month Closed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate all slots before confirming
        for (String slotKey : data.getSelectedSlotKeysInOrder()) {
            String[] parts = slotKey.split("\\|");
            if (parts.length == 3) {
                String courtName = parts[0];
                String date = parts[1];
                String timeSlot = parts[2];

                if (!accounts.isStaffLoggedIn() && !data.isBookingMonthOpen(date)) {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Online reservations are not open for "
                            + YearMonth.from(LocalDate.parse(date)) + ".",
                            "Month Closed", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (data.isCourtBooked(courtName, date, timeSlot)) {
                    data.addWaitingListEntry(customerName, courtName, date, timeSlot);
                    JOptionPane.showMessageDialog(parentFrame,
                            "One or more selected slots are already booked.\nYou have been added to the waiting list for the next available opening.",
                            "Waiting List Added",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String blockReason = data.getReservationBlockReason(courtName, date, timeSlot);
                if (blockReason != null) {
                    JOptionPane.showMessageDialog(parentFrame,
                            courtName + " is unavailable on " + date + ". Reason: " + blockReason,
                            "Court Unavailable", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        for (String timeSlot : data.getAvailableTimeSlots()) {
            int selectedCourtsAtThisTime = data.getSelectedCourtCountAtTime(timeSlot);
            if (selectedCourtsAtThisTime > 0) {
                int availablePaddlesForTime = data.getAvailableEquipmentCount(timeSlot, true);
                int availableBallsForTime = data.getAvailableEquipmentCount(timeSlot, false);
                if (data.getSelectedPaddleQuantity() * selectedCourtsAtThisTime > availablePaddlesForTime
                        || data.getSelectedBallQuantity() * selectedCourtsAtThisTime > availableBallsForTime) {
                    JOptionPane.showMessageDialog(parentFrame,
                            "The selected equipment is no longer available for every chosen time.\n"
                                    + "Please update your rental quantities and try again.",
                            "Equipment Availability Changed",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ArrayList<RentalItem> additionalItems = data.getAdditionalRentalItems();
                for (int i = 0; i < additionalItems.size() && i < data.getExtraRentalQuantities().size(); i++) {
                    RentalItem item = additionalItems.get(i);
                    int requestedQuantity = data.getExtraRentalQuantities().get(i) * selectedCourtsAtThisTime;
                    if (requestedQuantity > data.getAvailableEquipmentCount(item.getName(), timeSlot)) {
                        JOptionPane.showMessageDialog(parentFrame,
                                "There is not enough " + item.getName() + " stock for every chosen time.\n"
                                        + "Please update your rental quantities and try again.",
                                "Equipment Availability Changed",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
        }

        double totalPrice = data.getSelectedBookingTotal();
        String bookingReference = data.generateBookingReference(data.getSelectedReservationDate());
        ArrayList<ReservationSlot> bookingDetails = new ArrayList<>();

        for (String slotKey : data.getSelectedSlotKeysInOrder()) {
            String[] parts = slotKey.split("\\|");
            if (parts.length == 3) {
                String courtName = parts[0];
                String date = parts[1];
                String timeSlot = parts[2];

                Reservation reservation = new Reservation(
                        customerName,
                        courtName,
                        date,
                        timeSlot,
                        numberOfPlayers,
                        0 // Individual slot price, will be calculated
                );
                reservation.setBookingReference(bookingReference);
                reservation.setReservationSource(accounts.isStaffLoggedIn() ? "Onsite" : "Online");
                reservation.setCustomerEmail(customerEmail);
                reservation.setContactNumber(contactNumber);
                reservation.setPaymentStatus("Due at venue - " + paymentMethod);
                reservation.setStatus(accounts.isStaffLoggedIn() ? "Confirmed" : "Pending");
                reservation.setNumberOfHours(1);
                reservation.setPaddleQuantity(data.getSelectedPaddleQuantity());
                reservation.setBallQuantity(data.getSelectedBallQuantity());
                
                double slotPrice = data.getCourtRate(courtName, timeSlot);
                double slotEquipmentPrice = data.getSelectedPaddleQuantity() * data.getPaddleRentalRate()
                        + data.getSelectedBallQuantity() * data.getBallRentalRate();
                ArrayList<RentalItem> additionalItems = data.getAdditionalRentalItems();
                for (int i = 0; i < additionalItems.size() && i < data.getExtraRentalQuantities().size(); i++) {
                    RentalItem item = additionalItems.get(i);
                    int quantity = data.getExtraRentalQuantities().get(i);
                    if (quantity > 0) {
                        reservation.addRentedItem(item.getName(), item.getHourlyRate(), quantity);
                        slotEquipmentPrice += item.getHourlyRate() * quantity;
                    }
                }
                reservation.setCourtPrice(slotPrice);
                reservation.setTotalPrice(slotPrice + slotEquipmentPrice);

                data.getReservations().add(reservation);
                data.getReservationHistory().recordActivity("Booked reservation " + bookingReference + " for " + customerName + " at " + courtName + " " + timeSlot);
                bookingDetails.add(new ReservationSlot(courtName, timeSlot, slotPrice));
            }
        }

        data.saveReservationRecords(parentFrame);

        // Show improved confirmation dialog
        accounts.setCurrentUserName(customerName);
        accounts.setAdminLoggedIn(false);
        showBookingConfirmationDialog(parentFrame, bookingReference, bookingDetails, totalPrice,
                customerEmail, contactNumber, paymentMethod);

        // Clear selection
        data.getSelectedCourtSlots().clear();
    }

    private void showBookingConfirmationDialog(JFrame parentFrame, String bookingReference,
                                               ArrayList<ReservationSlot> bookingDetails, double totalPrice,
                                               String customerEmail, String contactNumber,
                                               String paymentMethod) {
        String confirmationTitle = accounts.isStaffLoggedIn() ? "Reservation Confirmed" : "Reservation Request Submitted";
        JDialog confirmDialog = new JDialog(parentFrame, confirmationTitle, true);
        confirmDialog.setSize(760, 760);
        confirmDialog.setMinimumSize(new Dimension(620, 580));
        confirmDialog.setLocationRelativeTo(parentFrame);
        confirmDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        confirmDialog.setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(INTRO_MINT);

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(22, 26, 20, 26));
        JLabel checkmarkLabel = new JLabel("✓", SwingConstants.CENTER);
        checkmarkLabel.setFont(new Font("Arial", Font.BOLD, 19));
        checkmarkLabel.setForeground(new Color(0, 135, 95));
        checkmarkLabel.setPreferredSize(new Dimension(34, 34));
        checkmarkLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 135, 95), 2, true));
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(WHITE);
        JLabel titleLabel = new JLabel(confirmationTitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(DARK_TEXT);
        JLabel subtitleLabel = new JLabel(accounts.isStaffLoggedIn()
                ? "This reservation has been confirmed."
                : "An administrator must approve this request before it becomes a confirmed booking.");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(INTRO_MUTED);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitleLabel);
        headerPanel.add(checkmarkLabel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(245, 245, 245));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 222, 220)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bookingHeader = createConfirmationSectionHeading("Booking Information");
        detailsPanel.add(bookingHeader);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(createConfirmationRow("Booking Reference", bookingReference, true));
        detailsPanel.add(createConfirmationRow("Booking Date", formatConfirmationDate(data.getSelectedReservationDate()), false));
        detailsPanel.add(createConfirmationRow("Customer", accounts.getCurrentUserName(), false));
        if (!customerEmail.isEmpty()) {
            detailsPanel.add(createConfirmationRow("Email", customerEmail, false));
        }
        if (!contactNumber.isEmpty()) {
            detailsPanel.add(createConfirmationRow("Contact Number", contactNumber, false));
        }
        detailsPanel.add(createConfirmationRow("Payment Method", paymentMethod, false));
        detailsPanel.add(Box.createVerticalStrut(12));
        detailsPanel.add(createConfirmationDivider());
        detailsPanel.add(Box.createVerticalStrut(14));

        detailsPanel.add(createConfirmationSectionHeading("Court Reservation Details"));
        detailsPanel.add(Box.createVerticalStrut(10));
        for (ReservationSlot slot : bookingDetails) {
            JPanel courtPanel = new JPanel();
            courtPanel.setLayout(new BoxLayout(courtPanel, BoxLayout.Y_AXIS));
            courtPanel.setBackground(WHITE);
            courtPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(224, 229, 226)),
                    BorderFactory.createEmptyBorder(11, 13, 11, 13)));
            courtPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JPanel courtTitleRow = new JPanel(new BorderLayout(8, 0));
            courtTitleRow.setBackground(WHITE);
            JLabel courtLabel = new JLabel(slot.getCourtName());
            courtLabel.setFont(new Font("Arial", Font.BOLD, 14));
            courtLabel.setForeground(DARK_TEXT);
            JLabel confirmedLabel = new JLabel(accounts.isStaffLoggedIn() ? "Confirmed" : "Pending");
            confirmedLabel.setFont(new Font("Arial", Font.BOLD, 10));
            confirmedLabel.setForeground(accounts.isStaffLoggedIn() ? new Color(0, 120, 70) : new Color(145, 93, 0));
            confirmedLabel.setOpaque(true);
            confirmedLabel.setBackground(accounts.isStaffLoggedIn() ? new Color(214, 242, 225) : LIGHT_GRAY);
            confirmedLabel.setBorder(BorderFactory.createEmptyBorder(4, 9, 4, 9));
            courtTitleRow.add(courtLabel, BorderLayout.WEST);
            courtTitleRow.add(confirmedLabel, BorderLayout.EAST);
            courtPanel.add(courtTitleRow);
            courtPanel.add(Box.createVerticalStrut(5));
            courtPanel.add(createConfirmationMutedLabel(slot.getTimeSlot()));
            courtPanel.add(Box.createVerticalStrut(8));
            courtPanel.add(createConfirmationRow("Court charge", formatCurrency(slot.getCourtPrice()), true));
            detailsPanel.add(courtPanel);
            detailsPanel.add(Box.createVerticalStrut(8));
        }

        detailsPanel.add(createConfirmationRow("Paddle Rental",
                data.getSelectedPaddleQuantity() + " × " + formatCurrency(data.getPaddleRentalRate()) + " / hour",
                formatCurrency(data.getPaddleRentalTotal())));
        detailsPanel.add(createConfirmationRow("Ball Rental",
                data.getSelectedBallQuantity() + " × " + formatCurrency(data.getBallRentalRate()) + " / hour",
                formatCurrency(data.getBallRentalTotal())));
        ArrayList<RentalItem> additionalItems = data.getAdditionalRentalItems();
        for (int i = 0; i < additionalItems.size() && i < data.getExtraRentalQuantities().size(); i++) {
            RentalItem item = additionalItems.get(i);
            int quantity = data.getExtraRentalQuantities().get(i);
            if (quantity > 0) {
                detailsPanel.add(createConfirmationRow(item.getName() + " Rental",
                        quantity + " × " + formatCurrency(item.getHourlyRate()) + " / hour",
                        formatCurrency(data.getExtraRentalTotal(item, quantity))));
            }
        }

        detailsPanel.add(Box.createVerticalStrut(12));
        detailsPanel.add(createConfirmationDivider());
        detailsPanel.add(Box.createVerticalStrut(12));
        JPanel totalPanel = new JPanel(new BorderLayout(12, 0));
        totalPanel.setBackground(new Color(245, 245, 245));
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel totalTextPanel = new JPanel();
        totalTextPanel.setLayout(new BoxLayout(totalTextPanel, BoxLayout.Y_AXIS));
        totalTextPanel.setBackground(new Color(245, 245, 245));
        JLabel totalLabel = new JLabel("Total Amount");
        totalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        totalLabel.setForeground(INTRO_MUTED);
        JLabel totalValue = new JLabel(formatCurrency(totalPrice));
        totalValue.setFont(new Font("Arial", Font.BOLD, 23));
        totalValue.setForeground(new Color(0, 135, 95));
        totalTextPanel.add(totalLabel);
        totalTextPanel.add(Box.createVerticalStrut(4));
        totalTextPanel.add(totalValue);
        JLabel bookingSuccessLabel = new JLabel(accounts.isStaffLoggedIn() ? "Confirmed" : "Pending");
        bookingSuccessLabel.setFont(new Font("Arial", Font.BOLD, 10));
        bookingSuccessLabel.setForeground(accounts.isStaffLoggedIn() ? new Color(0, 120, 70) : new Color(145, 93, 0));
        bookingSuccessLabel.setOpaque(true);
        bookingSuccessLabel.setBackground(accounts.isStaffLoggedIn() ? new Color(214, 242, 225) : LIGHT_GRAY);
        bookingSuccessLabel.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
        totalPanel.add(totalTextPanel, BorderLayout.WEST);
        totalPanel.add(bookingSuccessLabel, BorderLayout.EAST);
        detailsPanel.add(totalPanel);
        detailsPanel.add(Box.createVerticalStrut(14));

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(INTRO_MINT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(INTRO_MINT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 232, 235)));

        JButton okButton = new JButton("Done");
        styleButton(okButton, TEAL, WHITE, 13, 125, 40);
        okButton.addActionListener(e -> confirmDialog.dispose());

        JButton printButton = new JButton("Print Receipt");
        if (!accounts.isStaffLoggedIn()) {
            printButton.setText("Print Request");
        }
        printButton.setFont(new Font("Arial", Font.BOLD, 13));
        printButton.setPreferredSize(new Dimension(140, 40));
        printButton.setFocusPainted(false);
        printButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printButton.addActionListener(e -> app.saveReceiptFile(confirmDialog, bookingReference,
                bookingDetails, totalPrice, customerEmail, contactNumber, paymentMethod));

        buttonPanel.add(printButton);
        buttonPanel.add(okButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        confirmDialog.add(mainPanel);
        confirmDialog.setVisible(true);
    }
}
