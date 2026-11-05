package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.model.Court;
import pickleballsystem.PickleballMain;
import pickleballsystem.service.SystemData;

import static pickleballsystem.ui.UiHelper.*;

/** Admin screen for court availability, blocking dates, and adding courts. */
public class AdminCourtScreen extends BaseScreen {

    public AdminCourtScreen(PickleballMain app) {
        super(app);
    }

    public void showAdminCourtAvailabilityScreen() {
        JFrame courtFrame = new JFrame("SmashPoint Pickleball - Court Availability");
        courtFrame.setSize(1400, 800);
        courtFrame.setMinimumSize(new Dimension(1100, 650));
        courtFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        courtFrame.setLocationRelativeTo(null);

        JPanel headerPanel = app.createHeaderPanel("Court Availability", courtFrame, true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(LIGHT_GRAY);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JSpinner datePicker = app.createDatePicker(SystemData.FIRST_BOOKING_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE));

        filterPanel.add(dateLabel);
        filterPanel.add(app.createDatePickerControl(courtFrame, datePicker));
        ArrayList<String> blockTargets = new ArrayList<>();
        blockTargets.add("All Courts");
        blockTargets.addAll(java.util.Arrays.asList(data.getCourtNames()));
        JComboBox<String> courtBlockPicker = new JComboBox<>(blockTargets.toArray(new String[0]));
        filterPanel.add(new JLabel("Court:"));
        filterPanel.add(courtBlockPicker);
        String[] timeBlockTargets = new String[data.getAvailableTimeSlots().length + 1];
        timeBlockTargets[0] = "All Day";
        System.arraycopy(data.getAvailableTimeSlots(), 0, timeBlockTargets, 1, data.getAvailableTimeSlots().length);
        JComboBox<String> timeBlockPicker = new JComboBox<>(timeBlockTargets);
        filterPanel.add(new JLabel("Time:"));
        filterPanel.add(timeBlockPicker);
        JButton blockDateButton = createAdminActionButton("");
        filterPanel.add(blockDateButton);
        JButton bookingMonthsButton = createAdminActionButton("Month Availability Management");
        filterPanel.add(bookingMonthsButton);
        JButton addCourtButton = createAdminActionButton("+  Add Court");
        addCourtButton.addActionListener(e -> {
            if (addCourtFromAdmin(courtFrame)) {
                courtFrame.dispose();
                showAdminCourtAvailabilityScreen();
            }
        });
        filterPanel.add(addCourtButton);

        String[] columns = new String[data.getCourts().size() + 1];
        columns[0] = "Time";
        for (int i = 0; i < data.getCourts().size(); i++) {
            columns[i + 1] = data.getCourts().get(i).getCourtName();
        }

        DefaultTableModel model = readOnlyTableModel(columns);

        JTable availabilityTable = new JTable(model);
        bookingMonthsButton.addActionListener(e -> {
            if (app.manageBookingMonths(courtFrame)) {
                Date maximumDate = Date.from(data.getLastBookingDate()
                        .atStartOfDay(ZoneId.systemDefault()).toInstant());
                ((SpinnerDateModel) datePicker.getModel()).setEnd(maximumDate);
                Date minimumDate = Date.from(SystemData.FIRST_BOOKING_DATE
                        .atStartOfDay(ZoneId.systemDefault()).toInstant());
                ((SpinnerDateModel) datePicker.getModel()).setStart(minimumDate);
                LocalDate selectedDate = LocalDate.parse(app.getDateFromPicker(datePicker));
                if (!data.getOpenBookingMonths().contains(YearMonth.from(selectedDate))
                        || selectedDate.isBefore(SystemData.FIRST_BOOKING_DATE)
                        || selectedDate.isAfter(data.getLastBookingDate())) {
                    datePicker.setValue(Date.from(data.getFirstBookingDate()
                            .atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                app.refreshAvailabilityTable(model, app.getDateFromPicker(datePicker));
            }
        });
        availabilityTable.setRowHeight(40);
        availabilityTable.setFont(new Font("Arial", Font.PLAIN, 12));
        availabilityTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        availabilityTable.getTableHeader().setBackground(TEAL);
        availabilityTable.getTableHeader().setForeground(WHITE);

        AdminPriceAwareCellRenderer cellRenderer = new AdminPriceAwareCellRenderer();
        for (int i = 0; i < availabilityTable.getColumnCount(); i++) {
            availabilityTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        app.refreshAvailabilityTable(model, app.getDateFromPicker(datePicker));
        Runnable updateBlockDateButton = () -> {
            String date = app.getDateFromPicker(datePicker);
            String courtName = (String) courtBlockPicker.getSelectedItem();
            String timeSlot = (String) timeBlockPicker.getSelectedItem();
            boolean blocked = data.isAdminBlockTargetBlocked(courtName, date, timeSlot);
            String target = "All Courts".equals(courtName) ? "All Courts" : courtName;
            blockDateButton.setText(blocked ? "Allow " + target : "Block " + target);
        };
        updateBlockDateButton.run();
        blockDateButton.addActionListener(e -> {
            String date = app.getDateFromPicker(datePicker);
            String courtName = (String) courtBlockPicker.getSelectedItem();
            String timeSlot = (String) timeBlockPicker.getSelectedItem();
            boolean allCourts = "All Courts".equals(courtName);
            boolean allDay = "All Day".equals(timeSlot);
            boolean blocked = data.isAdminBlockTargetBlocked(courtName, date, timeSlot);
            if (blocked) {
                if (allDay && allCourts) {
                    data.getUnavailableDates().remove(date);
                    data.getDateBlockReasons().remove(date);
                } else if (allDay) {
                    data.getCourtDateBlockReasons().remove(data.getCourtDateKey(courtName, date));
                } else {
                    data.getCourtTimeBlockReasons().remove(data.getCourtTimeKey(courtName, date, timeSlot));
                }
                JOptionPane.showMessageDialog(courtFrame,
                        "Reservations are now allowed for " + courtName + " on " + date
                        + ("All Day".equals(timeSlot) ? "." : " at " + timeSlot + "."),
                        "Date Available", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String target = allCourts ? "all courts" : courtName;
                String reason = JOptionPane.showInputDialog(courtFrame,
                        "Enter the reason for blocking " + target + " on " + date
                        + ("All Day".equals(timeSlot) ? " (all day)" : " at " + timeSlot) + ":",
                        "Reason Required", JOptionPane.QUESTION_MESSAGE);
                if (reason == null) {
                    return;
                }
                reason = reason.trim();
                if (reason.isEmpty()) {
                    JOptionPane.showMessageDialog(courtFrame, "A reason is required before blocking reservations.",
                            "Reason Required", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (allDay && allCourts) {
                    data.getUnavailableDates().add(date);
                    data.getDateBlockReasons().put(date, reason);
                } else if (allDay) {
                    data.getCourtDateBlockReasons().put(data.getCourtDateKey(courtName, date), reason);
                } else {
                    data.getCourtTimeBlockReasons().put(data.getCourtTimeKey(courtName, date, timeSlot), reason);
                }
                JOptionPane.showMessageDialog(courtFrame,
                        "Reservations are now blocked for " + target + " on " + date
                        + ("All Day".equals(timeSlot) ? "." : " at " + timeSlot) + ".",
                        "Date Blocked", JOptionPane.INFORMATION_MESSAGE);
            }
            updateBlockDateButton.run();
            app.refreshAvailabilityTable(model, date);
        });
        courtBlockPicker.addActionListener(e -> updateBlockDateButton.run());
        timeBlockPicker.addActionListener(e -> updateBlockDateButton.run());
        datePicker.addChangeListener(e -> {
            updateBlockDateButton.run();
            app.refreshAvailabilityTable(model, app.getDateFromPicker(datePicker));
        });

        JScrollPane tableScrollPane = new JScrollPane(availabilityTable);
        tableScrollPane.getViewport().setBackground(WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(WHITE);

        JButton backButton = new JButton("Back to Dashboard");
        styleButton(backButton, TEAL, WHITE, 12, 150, 40);
        backButton.addActionListener(e -> {
            courtFrame.dispose();
            app.showAdminDashboard();
        });

        buttonPanel.add(backButton);

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel page = new JPanel(new BorderLayout());
        page.add(app.createAdminSidebar(courtFrame, "Court Management"), BorderLayout.WEST);
        page.add(mainPanel, BorderLayout.CENTER);
        courtFrame.add(headerPanel, BorderLayout.NORTH);
        courtFrame.add(page, BorderLayout.CENTER);
        courtFrame.add(buttonPanel, BorderLayout.SOUTH);
        courtFrame.setVisible(true);
    }

    private boolean addCourtFromAdmin(JFrame parent) {
        JTextField nameField = new JTextField(16);
        JTextField regularRateField = createDecimalOnlyField(10);
        JTextField peakRateField = createDecimalOnlyField(10);
        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 10));
        fields.add(new JLabel("Court name:"));
        fields.add(nameField);
        fields.add(new JLabel("Regular rate per hour (₱):"));
        fields.add(regularRateField);
        fields.add(new JLabel("Peak rate per hour (₱):"));
        fields.add(peakRateField);

        int answer = JOptionPane.showConfirmDialog(parent, fields, "Add Court",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return false;
        }

        String name = nameField.getText().trim();
        try {
            double regularRate = Double.parseDouble(regularRateField.getText().trim());
            double peakRate = Double.parseDouble(peakRateField.getText().trim());
            if (name.isEmpty() || !data.isValidNonNegativeAmount(regularRate)
                    || !data.isValidNonNegativeAmount(peakRate)) {
                throw new NumberFormatException();
            }
            if (data.findCourtByName(name) != null) {
                JOptionPane.showMessageDialog(parent, "A court with that name already exists.",
                        "Duplicate Court", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            data.getCourts().add(new Court(name, regularRate, peakRate));
            JOptionPane.showMessageDialog(parent, name + " was added successfully.",
                    "Court Added", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Enter a court name and non-negative hourly rates.",
                    "Invalid Court Details", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Admin version of cell renderer
    private class AdminPriceAwareCellRenderer extends DefaultTableCellRenderer {
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
                
                if (text.equalsIgnoreCase("Blocked")) {
                    label.setBackground(new Color(255, 224, 178));
                    label.setForeground(new Color(153, 82, 0));
                    label.setText(text);
                } else if (text.equalsIgnoreCase("Unavailable")) {
                    label.setBackground(new Color(224, 224, 224));
                    label.setForeground(new Color(90, 90, 90));
                    label.setText(text);
                } else if (text.equalsIgnoreCase("Pending")) {
                    label.setBackground(PENDING_YELLOW);
                    label.setForeground(new Color(145, 93, 0));
                    label.setText("Pending");
                } else if (text.equalsIgnoreCase("Booked")) {
                    label.setBackground(LIGHT_RED);
                    label.setForeground(RED);
                    label.setText("Booked");
                } else if (text.equalsIgnoreCase("Available")) {
                    label.setBackground(LIGHT_GREEN);
                    label.setForeground(GREEN);
                    String price = data.getTimeSlotPrice(row, table.getColumnName(column));
                    label.setText("Available - " + price);
                } else {
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
}
