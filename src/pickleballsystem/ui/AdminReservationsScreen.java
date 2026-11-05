package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import pickleballsystem.ui.BaseScreen;
import pickleballsystem.model.Court;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.Reservation;
import pickleballsystem.dsa.SearchUtil;
import pickleballsystem.service.SystemData;
import pickleballsystem.model.WaitingListEntry;

import static pickleballsystem.ui.UiHelper.*;

/** Admin screen for adding, editing, cancelling, and confirming reservations. */
public class AdminReservationsScreen extends BaseScreen {

    public AdminReservationsScreen(PickleballMain app) {
        super(app);
    }

    public void showAdminReservationsScreen() {
        JFrame adminFrame = new JFrame("SmashPoint Pickleball - Manage Reservations");
        adminFrame.setSize(1320, 720);
        adminFrame.setMinimumSize(new Dimension(1080, 650));
        adminFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        adminFrame.setLocationRelativeTo(null);

        JPanel headerPanel = app.createHeaderPanel("Manage Reservations", adminFrame, true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Customer", "Court", "Date", "Time", "Booking Type",
            "Expected", "Onsite", "Rented Equipment", "Total", "Status", "Confirm"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10 && "Confirm".equals(getValueAt(row, column));
            }
        };

        JTable reservationTable = new JTable(model);
        reservationTable.setBackground(WHITE);
        reservationTable.setRowHeight(30);
        reservationTable.setFont(new Font("Arial", Font.PLAIN, 11));
        reservationTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        reservationTable.getTableHeader().setBackground(TEAL);
        reservationTable.getTableHeader().setForeground(WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value == null ? "" : value.toString();
                if ("Status".equals(table.getColumnName(column))
                        && "Pending".equals(status)) {
                    c.setForeground(new Color(145, 93, 0));
                } else if ("Confirmed".equals(status)) {
                    c.setForeground(GREEN);
                } else if ("Cancelled".equals(status)) {
                    c.setForeground(RED);
                } else if ("Completed".equals(status)) {
                    c.setForeground(new Color(40, 100, 160));
                } else {
                    c.setForeground(DARK_TEXT);
                }
                ((JLabel) c).setHorizontalAlignment(CENTER);
                return c;
            }
        };

        for (int i = 0; i < reservationTable.getColumnCount(); i++) {
            reservationTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        reservationTable.getColumnModel().getColumn(10).setPreferredWidth(100);
        reservationTable.getColumnModel().getColumn(10).setCellRenderer(new ConfirmButtonRenderer());
        reservationTable.getColumnModel().getColumn(10).setCellEditor(
                new ConfirmButtonEditor(reservationTable, () -> {
                    confirmSelectedReservationFromAdmin(adminFrame, reservationTable);
                    refreshAdminReservationsTable(model);
                }));

        refreshAdminReservationsTable(model);

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.getViewport().setBackground(WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(WHITE);

        JButton addButton = new JButton("Add");
        styleButton(addButton, LIME, DARK_TEXT, 12, 120, 40);
        addButton.addActionListener(e -> {
            addReservationFromAdmin();
            refreshAdminReservationsTable(model);
        });

        JButton editButton = new JButton("Edit");
        styleButton(editButton, TEAL, WHITE, 12, 120, 40);
        editButton.addActionListener(e -> {
            editReservationFromAdmin(reservationTable);
            refreshAdminReservationsTable(model);
        });

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, RED, WHITE, 12, 120, 40);
        cancelButton.addActionListener(e -> {
            cancelSelectedReservationFromAdmin(reservationTable);
            refreshAdminReservationsTable(model);
        });

        JButton deleteCompletedButton = new JButton("Delete Completed");
        styleButton(deleteCompletedButton, RED, WHITE, 12, 150, 40);
        deleteCompletedButton.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(adminFrame, "Please select a reservation.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Reservation reservation = data.getReservations().get(selectedRow);
            if (!reservation.isCompleted()) {
                int confirm = JOptionPane.showConfirmDialog(adminFrame,
                        "Mark this booking as completed and delete it?",
                        "Complete Booking",
                        JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                reservation.setCompleted(true);
                reservation.setStatus("Completed");
            }

            data.getCompletedReservations().add(reservation);
            data.getReservations().remove(selectedRow);

            data.saveReservationRecords(adminFrame);
            refreshAdminReservationsTable(model);
            JOptionPane.showMessageDialog(adminFrame, "Completed booking removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton backButton = new JButton("Back to Dashboard");
        styleButton(backButton, new Color(100, 100, 100), WHITE, 12, 150, 40);
        backButton.addActionListener(e -> {
            adminFrame.dispose();
            app.showAdminDashboard();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteCompletedButton);
        JPanel reservationActions = new JPanel(new BorderLayout());
        reservationActions.setBackground(WHITE);
        reservationActions.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        reservationActions.add(buttonPanel, BorderLayout.WEST);
        reservationActions.add(backButton, BorderLayout.EAST);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(reservationActions, BorderLayout.SOUTH);

        JPanel page = new JPanel(new BorderLayout());
        page.add(app.createAdminSidebar(adminFrame, "Reservations"), BorderLayout.WEST);
        page.add(mainPanel, BorderLayout.CENTER);
        adminFrame.add(headerPanel, BorderLayout.NORTH);
        adminFrame.add(page, BorderLayout.CENTER);
        adminFrame.setVisible(true);
    }

    private void refreshAdminReservationsTable(DefaultTableModel model) {
        model.setRowCount(0);

        for (Reservation reservation : data.getReservations()) {
            String confirmAction = reservation.isActive() && "Pending".equals(reservation.getStatus())
                    ? "Confirm" : "—";
            model.addRow(new Object[]{
                    reservation.getCustomerName(),
                    reservation.getCourtName(),
                    reservation.getReservationDate(),
                    reservation.getTimeSlot(),
                    reservation.getReservationSource(),
                    reservation.getNumberOfPlayers(),
                    reservation.getPlayersOnsite(),
                    data.getReservationRentalSummary(reservation),
                    "₱" + String.format("%.0f", reservation.getTotalPrice()),
                    reservation.getStatus(),
                    confirmAction
            });
        }
    }

    private class ConfirmButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        private ConfirmButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setFont(new Font("Arial", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            boolean pending = "Confirm".equals(value);
            setText(pending ? "Confirm" : "—");
            setEnabled(pending);
            setBackground(pending ? new Color(46, 125, 50) : new Color(238, 238, 238));
            setForeground(pending ? WHITE : INTRO_MUTED);
            return this;
        }
    }

    private class ConfirmButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton("Confirm");
        private int editingRow;

        private ConfirmButtonEditor(JTable table, Runnable confirmAction) {
            button.setBackground(new Color(46, 125, 50));
            button.setForeground(WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 11));
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                fireEditingStopped();
                int selectedRow = table.convertRowIndexToView(editingRow);
                if (selectedRow >= 0) {
                    table.setRowSelectionInterval(selectedRow, selectedRow);
                    confirmAction.run();
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "Confirm";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            editingRow = table.convertRowIndexToModel(row);
            return button;
        }
    }

    private void addReservationFromAdmin() {
        String customerName = JOptionPane.showInputDialog(app, "Enter customer name:");
        if (customerName == null || customerName.trim().isEmpty()) {
            return;
        }

        String selectedCourtName = (String) JOptionPane.showInputDialog(
                app,
                "Choose a court:",
                "Add Reservation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                data.getCourtNames(),
                data.getCourtNames()[0]
        );

        if (selectedCourtName == null) return;

        JSpinner datePicker = app.createDatePicker(SystemData.FIRST_BOOKING_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Choose a date:"));
        datePanel.add(app.createDatePickerControl(app, datePicker));
        int dateAnswer = JOptionPane.showConfirmDialog(app, datePanel, "Add Reservation Date",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (dateAnswer != JOptionPane.OK_OPTION) return;
        String selectedBookingDate = app.getDateFromPicker(datePicker);

        String selectedTime = (String) JOptionPane.showInputDialog(
                app,
                "Choose a time slot:",
                "Add Reservation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                data.getAvailableTimeSlots(),
                data.getAvailableTimeSlots()[0]
        );

        if (selectedTime == null) return;

        try {
            Court selectedCourt = data.findCourtByName(selectedCourtName);

            if (selectedCourt == null) {
                JOptionPane.showMessageDialog(app, "Court not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (data.isReservationDateBlocked(selectedBookingDate)) {
                String reason = data.getDateBlockReasons().get(selectedBookingDate);
                JOptionPane.showMessageDialog(app,
                        "Reservations are blocked on the selected date."
                        + (reason == null ? "" : "\nReason: " + reason),
                        "Date Unavailable", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String blockReason = data.getReservationBlockReason(selectedCourtName, selectedBookingDate, selectedTime);
            if (blockReason != null) {
                JOptionPane.showMessageDialog(app,
                        selectedCourtName + " is unavailable on the selected date.\nReason: " + blockReason,
                        "Court Unavailable", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data.isCourtBooked(selectedCourtName, selectedBookingDate, selectedTime)) {
                JOptionPane.showMessageDialog(app,
                        "The selected court is already booked at that date and time.",
                        "Booking Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double price = data.getCourtRate(selectedCourtName, selectedTime);

            Reservation reservation = new Reservation(
                    customerName.trim(), selectedCourtName, selectedBookingDate, selectedTime, 2, price);
            reservation.setReservationSource("Onsite");
            reservation.setStatus("Confirmed");
            reservation.setNumberOfHours(1);
            reservation.setCourtPrice(price);
            reservation.setTotalPrice(price);

            data.getReservations().add(reservation);
            data.saveReservationRecords(app);

            JOptionPane.showMessageDialog(app,
                    "Reservation added successfully.\n\n" + reservation.getReservationDetails(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(app,
                    "Please enter valid values.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editReservationFromAdmin(JTable reservationTable) {
        int selectedRow = reservationTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(app, "Please select a reservation to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Reservation reservation = data.getReservations().get(selectedRow);

        String newCustomer = JOptionPane.showInputDialog(app, "Edit customer name:", reservation.getCustomerName());

        String newCourtName = (String) JOptionPane.showInputDialog(
                app,
                "Choose a court:",
                "Edit Reservation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                data.getCourtNames(),
                reservation.getCourtName()
        );

        JSpinner datePicker = app.createDatePicker(reservation.getReservationDate());
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Choose a date:"));
        datePanel.add(app.createDatePickerControl(app, datePicker));
        int dateAnswer = JOptionPane.showConfirmDialog(app, datePanel, "Edit Reservation Date",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        String newDate = reservation.getReservationDate();
        if (dateAnswer == JOptionPane.OK_OPTION) {
            newDate = app.getDateFromPicker(datePicker);
        }

        String targetCourtName = newCourtName == null ? reservation.getCourtName() : newCourtName;
        String dayBlockReason = data.getReservationBlockReason(targetCourtName, newDate);
        if (dayBlockReason != null) {
            JOptionPane.showMessageDialog(app,
                    "Reservations are blocked for " + targetCourtName + " on " + newDate
                    + ". Reason: " + dayBlockReason,
                    "Reservation Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newTime = (String) JOptionPane.showInputDialog(
                app,
                "Choose a time slot:",
                "Edit Reservation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                data.getAvailableTimeSlots(),
                reservation.getTimeSlot()
        );
        String targetTimeSlot = newTime == null ? reservation.getTimeSlot() : newTime;
        String timeBlockReason = data.getReservationBlockReason(targetCourtName, newDate, targetTimeSlot);
        if (timeBlockReason != null) {
            JOptionPane.showMessageDialog(app,
                    "Reservations are blocked for " + targetCourtName + " on " + newDate
                    + " at " + targetTimeSlot + ". Reason: " + timeBlockReason,
                    "Reservation Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newCustomer != null && !newCustomer.trim().isEmpty()) {
            reservation.setCustomerName(newCustomer.trim());
        }
        reservation.setCourtName(targetCourtName);
        reservation.setReservationDate(newDate);
        if (newTime != null) {
            reservation.setTimeSlot(newTime);
            double price = data.getCourtRate(targetCourtName, newTime);
            reservation.setCourtPrice(price);
            reservation.setTotalPrice(price);
        }

        int markComplete = JOptionPane.showConfirmDialog(app,
                "Mark this reservation as completed?",
                "Completed Booking",
                JOptionPane.YES_NO_OPTION);

        if (markComplete == JOptionPane.YES_OPTION) {
            reservation.setCompleted(true);
            reservation.setStatus("Completed");
        }

        data.saveReservationRecords(app);
        JOptionPane.showMessageDialog(app, "Reservation updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelSelectedReservationFromAdmin(JTable reservationTable) {
        int selectedRow = reservationTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(app, "Please select a reservation to cancel.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Reservation reservation = data.getReservations().get(selectedRow);

        if (!reservation.isActive()) {
            JOptionPane.showMessageDialog(app, "This reservation is already cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(app, "Are you sure you want to cancel this reservation?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            reservation.setActive(false);
            reservation.setStatus("Cancelled");
            data.saveReservationRecords(app);
            data.getReservationHistory().recordActivity("Cancelled reservation: " + reservation.getBookingReference());
            WaitingListEntry waiting = data.promoteWaitingList(reservation.getCourtName(),
                    reservation.getReservationDate(), reservation.getTimeSlot());
            String note = waiting == null ? "" : "\nNext on the waiting list: " + waiting.getCustomerName() + ".";
            JOptionPane.showMessageDialog(app, "Reservation cancelled successfully." + note, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void confirmSelectedReservationFromAdmin(JFrame parent, JTable reservationTable) {
        if (!accounts.isAdminLoggedIn()) {
            JOptionPane.showMessageDialog(parent, "Only an administrator can confirm reservations.",
                    "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Select a pending reservation first.",
                    "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Reservation selected = data.getReservations().get(selectedRow);
        if (!selected.isActive() || !"Pending".equals(selected.getStatus())) {
            JOptionPane.showMessageDialog(parent, "Only pending reservations can be confirmed.",
                    "Cannot Confirm", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingReference = selected.getBookingReference();
        String confirmationText = "Confirm the reservation for " + selected.getCustomerName() + "?"
                + (bookingReference == null || bookingReference.isEmpty()
                ? "" : "\nThis confirms all court times in booking " + bookingReference + ".");
        int answer = JOptionPane.showConfirmDialog(parent, confirmationText,
                "Confirm Reservation", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        for (Reservation reservation : SearchUtil.findSameBooking(data.getReservations(), selected)) { // DSA: searching
            if (reservation.isActive() && "Pending".equals(reservation.getStatus())) {
                reservation.setStatus("Confirmed");
            }
        }
        data.saveReservationRecords(parent);
        data.getReservationHistory().recordActivity("Confirmed reservation " + bookingReference);
        JOptionPane.showMessageDialog(parent, "Reservation confirmed. The booking is now approved.",
                "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }
}
