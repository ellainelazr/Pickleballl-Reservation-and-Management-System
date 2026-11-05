package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import pickleballsystem.ui.BaseScreen;
import pickleballsystem.PickleballMain;
import pickleballsystem.service.SystemData;

/** Date picker, calendar dialog, and the open booking months setting. */
public class BookingCalendar extends BaseScreen {

    public BookingCalendar(PickleballMain app) {
        super(app);
    }

    public JSpinner createDatePicker(String isoDate) {
        LocalDate initialDate = LocalDate.parse(isoDate);
        LocalDate firstDate = SystemData.FIRST_BOOKING_DATE;
        LocalDate lastDate = data.getLastBookingDate();
        if (initialDate.isBefore(firstDate) || initialDate.isAfter(lastDate)) {
            initialDate = firstDate;
        }
        Date dateValue = Date.from(initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date minimumDate = Date.from(firstDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date maximumDate = Date.from(lastDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner datePicker = new JSpinner(
                new SpinnerDateModel(dateValue, minimumDate, maximumDate, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(datePicker, "yyyy-MM-dd");
        editor.getTextField().setEditable(true);
        datePicker.setEditor(editor);
        datePicker.setToolTipText("Use the arrows or type a date as yyyy-MM-dd.");
        datePicker.setPreferredSize(new Dimension(150, 30));
        return datePicker;
    }

    public boolean manageBookingMonths(JFrame parentFrame) {
        JComboBox<String> monthPicker = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        int[] years = new int[11];
        String[] yearNames = new String[years.length];
        for (int i = 0; i < years.length; i++) {
            years[i] = 2026 + i;
            yearNames[i] = String.valueOf(years[i]);
        }
        JComboBox<String> yearPicker = new JComboBox<>(yearNames);
        yearPicker.setSelectedItem(String.valueOf(YearMonth.now().getYear()));
        monthPicker.setSelectedItem(YearMonth.now().getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, Locale.ENGLISH));
        JLabel statusLabel = new JLabel();
        Runnable updateStatus = () -> {
            YearMonth selectedMonth = YearMonth.of(
                    Integer.parseInt((String) yearPicker.getSelectedItem()),
                    monthPicker.getSelectedIndex() + 1);
            statusLabel.setText(data.getOpenBookingMonths().contains(selectedMonth)
                    ? selectedMonth + " is open for reservations."
                    : selectedMonth + " is closed for reservations.");
        };
        yearPicker.addActionListener(event -> updateStatus.run());
        monthPicker.addActionListener(event -> updateStatus.run());
        updateStatus.run();

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectors.add(new JLabel("Year:"));
        selectors.add(yearPicker);
        selectors.add(new JLabel("Month:"));
        selectors.add(monthPicker);
        panel.add(selectors, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.CENTER);

        Object[] options = {"Open Month", "Close Month", "Done"};
        boolean changed = false;
        while (true) {
            int answer = JOptionPane.showOptionDialog(parentFrame, panel,
                    "Month Availability Management", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (answer == 2 || answer < 0) {
                return changed;
            }
            YearMonth selectedMonth = YearMonth.of(
                    Integer.parseInt((String) yearPicker.getSelectedItem()),
                    monthPicker.getSelectedIndex() + 1);
            if (selectedMonth.isBefore(YearMonth.from(SystemData.FIRST_BOOKING_DATE))) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Months before October 2026 are outside the booking calendar.",
                        "Month Not Available", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            Set<YearMonth> previousMonths = new HashSet<>(data.getOpenBookingMonths());
            if (answer == 0) {
                data.getOpenBookingMonths().add(selectedMonth);
            } else {
                data.getOpenBookingMonths().remove(selectedMonth);
            }
            try {
                data.getFileStorageService().saveOpenBookingMonths(data.getOpenBookingMonths());
                changed = true;
            } catch (IOException ex) {
                data.getOpenBookingMonths().clear();
                data.getOpenBookingMonths().addAll(previousMonths);
                JOptionPane.showMessageDialog(parentFrame,
                        "Month availability could not be saved. Please check the data folder.",
                        "Storage Error", JOptionPane.ERROR_MESSAGE);
            }
            updateStatus.run();
        }
    }

    public JPanel createDatePickerControl(JFrame parentFrame, JSpinner datePicker) {
        JPanel pickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pickerPanel.setOpaque(false);
        pickerPanel.add(datePicker);

        JButton calendarButton = new JButton("Calendar");
        calendarButton.addActionListener(e -> showCalendarDialog(parentFrame, datePicker));
        pickerPanel.add(calendarButton);
        return pickerPanel;
    }

    private void showCalendarDialog(JFrame parentFrame, JSpinner datePicker) {
        LocalDate selectedBookingDate = LocalDate.parse(getDateFromPicker(datePicker));
        JDialog calendarDialog = new JDialog(parentFrame, "Select Date", true);
        calendarDialog.setSize(410, 430);
        calendarDialog.setLocationRelativeTo(parentFrame);

        ArrayList<YearMonth> bookingMonths = new ArrayList<>(data.getOpenBookingMonths());
        ArrayList<String> monthNames = new ArrayList<>();
        for (YearMonth month : bookingMonths) {
            monthNames.add(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
        }
        JComboBox<String> monthPicker = new JComboBox<>(monthNames.toArray(new String[0]));
        if (bookingMonths.isEmpty()) {
            monthPicker.setEnabled(false);
        } else {
            int selectedMonth = bookingMonths.indexOf(YearMonth.from(selectedBookingDate));
            monthPicker.setSelectedIndex(selectedMonth < 0 ? 0 : selectedMonth);
        }

        JPanel calendarPanel = new JPanel(new BorderLayout(8, 8));
        JPanel monthPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        monthPanel.add(monthPicker);

        JPanel daysPanel = new JPanel(new GridLayout(7, 7, 4, 4));
        daysPanel.setPreferredSize(new Dimension(350, 280));
        Runnable refreshCalendar = () -> {
            daysPanel.removeAll();
            if (bookingMonths.isEmpty()) {
                daysPanel.add(new JLabel("No months are currently open for reservations.",
                        SwingConstants.CENTER));
                daysPanel.revalidate();
                daysPanel.repaint();
                return;
            }
            String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String weekday : weekdays) {
                JLabel weekdayLabel = new JLabel(weekday, SwingConstants.CENTER);
                weekdayLabel.setFont(new Font("Arial", Font.BOLD, 11));
                daysPanel.add(weekdayLabel);
            }

            YearMonth selectedMonth = bookingMonths.get(monthPicker.getSelectedIndex());
            LocalDate firstDay = selectedMonth.atDay(1);
            int leadingEmptyDays = firstDay.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < leadingEmptyDays; i++) {
                daysPanel.add(new JLabel(""));
            }
            for (int day = 1; day <= firstDay.lengthOfMonth(); day++) {
                int selectedDay = day;
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFocusPainted(false);
                dayButton.setPreferredSize(new Dimension(40, 32));
                dayButton.addActionListener(e -> {
                    datePicker.setValue(Date.from(
                            selectedMonth.atDay(selectedDay).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    calendarDialog.dispose();
                });
                daysPanel.add(dayButton);
            }
            while (daysPanel.getComponentCount() < 49) {
                daysPanel.add(new JLabel(""));
            }
            daysPanel.revalidate();
            daysPanel.repaint();
        };

        monthPicker.addActionListener(e -> refreshCalendar.run());
        refreshCalendar.run();

        calendarPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
        calendarPanel.add(monthPanel, BorderLayout.NORTH);
        calendarPanel.add(daysPanel, BorderLayout.CENTER);
        calendarDialog.add(calendarPanel);
        calendarDialog.setVisible(true);
    }

    public String getDateFromPicker(JSpinner datePicker) {
        Date selected = (Date) datePicker.getValue();
        return selected.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
