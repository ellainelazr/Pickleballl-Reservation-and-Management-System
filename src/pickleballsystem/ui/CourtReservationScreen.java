package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Date;

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
import pickleballsystem.model.Court;
import pickleballsystem.PickleballMain;

import static pickleballsystem.ui.UiHelper.*;

/** Court availability grid where customers pick their time slots. */
public class CourtReservationScreen extends BaseScreen {

    public CourtReservationScreen(PickleballMain app) {
        super(app);
    }

    public void showCourtReservationScreen() {
        showCourtReservationScreen(false);
    }

    public void showCourtReservationScreen(boolean returnToHome) {
        JFrame reservationFrame = new JFrame("SmashPoint Pickleball - Book a Court");
        reservationFrame.setSize(1450, 850);
        reservationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        reservationFrame.setLocationRelativeTo(null);

        // Reset selections for new booking
        data.getSelectedCourtSlots().clear();
        data.setSelectedPaddleQuantity(0);
        data.setSelectedBallQuantity(0);
        data.getExtraRentalQuantities().clear();
        data.setSelectedReservationDate(data.getSelectedBookingDate());
        data.initializeDailyBookingCounter(data.getSelectedReservationDate());

        JPanel headerPanel = app.createHeaderPanel("Court Reservation", reservationFrame, false);
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(LIGHT_GRAY);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JSpinner datePicker = app.createDatePicker(data.getSelectedReservationDate());

        filterPanel.add(dateLabel);
        filterPanel.add(app.createDatePickerControl(reservationFrame, datePicker));

        String[] columns = new String[data.getCourts().size() + 1];
        columns[0] = "Time";
        for (int i = 0; i < data.getCourts().size(); i++) {
            columns[i + 1] = data.getCourts().get(i).getCourtName();
        }

        DefaultTableModel model = readOnlyTableModel(columns);

        JTable availabilityTable = new JTable(model);
        availabilityTable.setRowHeight(45);
        availabilityTable.setFont(new Font("Arial", Font.PLAIN, 12));
        availabilityTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        availabilityTable.getTableHeader().setBackground(TEAL);
        availabilityTable.getTableHeader().setForeground(WHITE);

        // Custom cell renderer for multiple selection
        MultiSelectCellRenderer cellRenderer = new MultiSelectCellRenderer();
        for (int i = 0; i < availabilityTable.getColumnCount(); i++) {
            availabilityTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        refreshAvailabilityTable(model, data.getSelectedReservationDate());

        datePicker.addChangeListener(e -> {
            data.setSelectedReservationDate(app.getDateFromPicker(datePicker));
            data.initializeDailyBookingCounter(data.getSelectedReservationDate());
            data.getSelectedCourtSlots().clear();
            refreshAvailabilityTable(model, data.getSelectedReservationDate());
            availabilityTable.repaint();
        });

        // Mouse listener for multi-cell selection
        availabilityTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = availabilityTable.rowAtPoint(e.getPoint());
                int col = availabilityTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col > 0) {
                    if (data.isReservationDateBlocked(data.getSelectedReservationDate())) {
                        String reason = data.getDateBlockReasons().get(data.getSelectedReservationDate());
                        JOptionPane.showMessageDialog(reservationFrame,
                                "Reservations are not accepted on " + data.getSelectedReservationDate()
                                + (reason == null ? "." : ". Reason: " + reason),
                                "Date Unavailable",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (!data.isBookingMonthOpen(data.getSelectedReservationDate())) {
                        JOptionPane.showMessageDialog(reservationFrame,
                                "Reservations are not open for "
                                + YearMonth.from(LocalDate.parse(data.getSelectedReservationDate())) + ".",
                                "Month Closed", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String timeSlot = data.getAvailableTimeSlots()[row];
                    String courtName = columns[col];
                    String blockReason = data.getReservationBlockReason(
                            courtName, data.getSelectedReservationDate(), data.getAvailableTimeSlots()[row]);
                    if (blockReason != null) {
                        JOptionPane.showMessageDialog(reservationFrame,
                                courtName + " is unavailable on " + data.getSelectedReservationDate()
                                + ". Reason: " + blockReason,
                                "Court Unavailable", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Check if already booked
                    if (data.isCourtBooked(courtName, data.getSelectedReservationDate(), timeSlot)) {
                        JOptionPane.showMessageDialog(reservationFrame,
                                "This court is already booked for that date and time.\nPlease choose another slot.",
                                "Booking Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Toggle selection
                    String slotKey = courtName + "|" + data.getSelectedReservationDate() + "|" + timeSlot;
                    if (data.getSelectedCourtSlots().contains(slotKey)) {
                        data.getSelectedCourtSlots().remove(slotKey);
                    } else {
                        data.getSelectedCourtSlots().add(slotKey);
                    }

                    availabilityTable.repaint();
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(availabilityTable);
        tableScrollPane.getViewport().setBackground(WHITE);

        JPanel selectionPanel = new JPanel(new GridLayout(0, 2, 15, 12));
        selectionPanel.setBackground(LIGHT_GRAY);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel selectedSlotsLabel = new JLabel("Selected Slots: 0");
        selectedSlotsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        selectedSlotsLabel.setForeground(DARK_TEXT);

        JLabel courtHoursLabel = new JLabel("Total Court-Hours: 0");
        courtHoursLabel.setFont(new Font("Arial", Font.BOLD, 12));
        courtHoursLabel.setForeground(DARK_TEXT);

        JLabel priceLabel = new JLabel("Total Price:");
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel totalPriceLabel = new JLabel("₱0.00");
        totalPriceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPriceLabel.setForeground(TEAL);

        selectionPanel.add(selectedSlotsLabel);
        selectionPanel.add(courtHoursLabel);
        selectionPanel.add(priceLabel);
        selectionPanel.add(totalPriceLabel);

        // Update selection display on any interaction
        Runnable updateSelection = () -> {
            selectedSlotsLabel.setText("Selected Slots: " + data.getSelectedCourtSlots().size());
            courtHoursLabel.setText("Total Court-Hours: " + data.getSelectedCourtSlots().size());
            double totalPrice = data.calculateSelectedPrice();
            totalPriceLabel.setText("₱" + String.format("%.0f", totalPrice));
        };
        availabilityTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateSelection.run();
            }
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setBackground(WHITE);

        JButton confirmButton = new JButton("Next: Equipment Rental");
        styleButton(confirmButton, LIME, DARK_TEXT, 12, 210, 40);
        confirmButton.addActionListener(e -> {
            if (data.getSelectedCourtSlots().isEmpty()) {
                JOptionPane.showMessageDialog(reservationFrame,
                        "Please select at least one court time slot.",
                        "Selection Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            app.showEquipmentRentalDialog(reservationFrame, model, availabilityTable, updateSelection);
        });

        JButton clearButton = new JButton("Clear Selection");
        styleButton(clearButton, TEAL, WHITE, 12, 140, 40);
        clearButton.addActionListener(e -> {
            data.getSelectedCourtSlots().clear();
            availabilityTable.repaint();
            updateSelection.run();
        });

        JButton backButton = new JButton(returnToHome ? "Back to Home" : "Back to Dashboard");
        styleButton(backButton, new Color(100, 100, 100), WHITE, 12, 140, 40);
        backButton.addActionListener(e -> {
            data.getSelectedCourtSlots().clear();
            reservationFrame.dispose();
            if (returnToHome) {
                app.showIntroScreen();
            } else {
                app.showCustomerDashboard();
            }
        });

        actionPanel.add(confirmButton);
        actionPanel.add(clearButton);
        actionPanel.add(backButton);

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(selectionPanel, BorderLayout.SOUTH);

        reservationFrame.add(headerPanel, BorderLayout.NORTH);
        reservationFrame.add(mainPanel, BorderLayout.CENTER);
        reservationFrame.add(actionPanel, BorderLayout.SOUTH);

        // Initial update
        updateSelection.run();

        // Add table model listener to update display when model changes
        model.addTableModelListener(e -> updateSelection.run());

        reservationFrame.setVisible(true);
    }

    // Custom cell renderer for multiple cell selection
    private class MultiSelectCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(CENTER);
            label.setVerticalAlignment(CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.PLAIN, 11));

            if (value != null) {
                String text = value.toString();
                
                if (text.equalsIgnoreCase("Pending")) {
                    label.setBackground(PENDING_YELLOW);
                    label.setForeground(new Color(145, 93, 0));
                    label.setText("Pending");
                } else if (text.equalsIgnoreCase("Booked")) {
                    label.setBackground(LIGHT_RED);
                    label.setForeground(RED);
                    label.setText("Booked");
                } else if (text.equalsIgnoreCase("Blocked")) {
                    label.setBackground(new Color(255, 224, 178));
                    label.setForeground(new Color(153, 82, 0));
                    label.setText("Blocked");
                } else if (text.equalsIgnoreCase("Unavailable")) {
                    label.setBackground(new Color(224, 224, 224));
                    label.setForeground(new Color(90, 90, 90));
                    label.setText(text);
                } else if (text.equalsIgnoreCase("Available")) {
                    String courtName = table.getColumnName(column);
                    String timeSlot = (String) table.getValueAt(row, 0);
                    String slotKey = courtName + "|" + data.getSelectedReservationDate() + "|" + timeSlot;

                    if (data.getSelectedCourtSlots().contains(slotKey)) {
                        label.setBackground(SELECTED_BLUE);
                        label.setForeground(WHITE);
                        label.setText("Selected");
                    } else {
                        label.setBackground(LIGHT_GREEN);
                        label.setForeground(GREEN);
                        String price = data.getTimeSlotPrice(row, courtName);
                        label.setText("Available - " + price);
                    }
                } else {
                    // Time slot column
                    label.setBackground(WHITE);
                    label.setForeground(DARK_TEXT);
                    label.setText(text);
                }
            } else {
                label.setBackground(WHITE);
                label.setText("");
            }

            return label;
        }
    }

    public void refreshAvailabilityTable(DefaultTableModel model, String selectedBookingDate) {
        model.setRowCount(0);

        for (String timeSlot : data.getAvailableTimeSlots()) {
            Object[] row = new Object[data.getCourts().size() + 1];
            row[0] = timeSlot;

            for (int i = 0; i < data.getCourts().size(); i++) {
                Court court = data.getCourts().get(i);
                if (data.isReservationDateBlocked(selectedBookingDate)) {
                    row[i + 1] = "Unavailable";
                } else if (data.isCourtReservationBlocked(court.getCourtName(), selectedBookingDate, timeSlot)) {
                    row[i + 1] = "Blocked";
                } else if (data.isCourtBooked(court.getCourtName(), selectedBookingDate, timeSlot)) {
                    row[i + 1] = data.getSlotStatus(court.getCourtName(), selectedBookingDate, timeSlot);
                } else if (!data.isBookingMonthOpen(selectedBookingDate)) {
                    row[i + 1] = "Unavailable";
                } else {
                    row[i + 1] = "Available";
                }
            }

            model.addRow(row);
        }
    }
}
