package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.Reservation;

import static pickleballsystem.ui.UiHelper.*;

/** Staff screen for checking in players and checking out onsite bookings. */
public class StaffOnsiteScreen extends BaseScreen {

    public StaffOnsiteScreen(PickleballMain app) {
        super(app);
    }

    public void showStaffOnsiteScreen() {
        JFrame staffFrame = new JFrame("SmashPoint Pickleball - Onsite Check-in");
        staffFrame.setSize(1100, 650);
        staffFrame.setLocationRelativeTo(null);
        staffFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Customer", "Court", "Date", "Time", "Expected Players", "Booking Source"};
        DefaultTableModel model = readOnlyTableModel(columns);
        JTable reservationTable = new JTable(model);
        reservationTable.setRowHeight(30);
        reservationTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        reservationTable.getTableHeader().setBackground(TEAL);
        reservationTable.getTableHeader().setForeground(WHITE);
        refreshStaffOnsiteTable(model);

        JButton checkOutButton = createNavButton("Check Out", RED, () -> {
            int row = reservationTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(staffFrame, "Select a reservation first.",
                        "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Reservation reservation = getActiveReservationAtRow(row);
            if (reservation != null) {
                int confirm = JOptionPane.showConfirmDialog(staffFrame,
                        "Check out this reservation and mark it completed?",
                        "Confirm Check Out", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                String bookingReference = reservation.getBookingReference();
                for (int i = data.getReservations().size() - 1; i >= 0; i--) {
                    Reservation booking = data.getReservations().get(i);
                    boolean sameBooking = booking == reservation
                            || (bookingReference != null && !bookingReference.isEmpty()
                            && bookingReference.equals(booking.getBookingReference()));
                    if (booking.isActive() && sameBooking) {
                        booking.setPlayersOnsite(0);
                        booking.setCompleted(true);
                        booking.setStatus("Completed");
                        data.getCompletedReservations().add(booking);
                        data.getReservations().remove(i);
                    }
                }
                data.saveReservationRecords(staffFrame);
                refreshStaffOnsiteTable(model);
                JOptionPane.showMessageDialog(staffFrame, "Reservation checked out successfully.",
                        "Check Out Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton backButton = createNavButton("Back to Dashboard", new Color(100, 100, 100), () -> {
            staffFrame.dispose();
            app.showCustomerDashboard();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBackground(WHITE);
        buttons.add(checkOutButton);
        buttons.add(backButton);

        JLabel title = new JLabel("Reservations and Players Onsite");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(INTRO_TEAL);
        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
        mainPanel.add(buttons, BorderLayout.SOUTH);
        staffFrame.add(app.createHeaderPanel("Onsite Check-in", staffFrame, false), BorderLayout.NORTH);
        staffFrame.add(mainPanel, BorderLayout.CENTER);
        staffFrame.setVisible(true);
    }

    private void refreshStaffOnsiteTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Reservation reservation : data.getReservations()) {
            if (reservation.isActive() && "Confirmed".equals(reservation.getStatus())) {
                model.addRow(new Object[]{
                    reservation.getCustomerName(),
                    reservation.getCourtName(),
                        reservation.getReservationDate(),
                        reservation.getTimeSlot(),
                        reservation.getNumberOfPlayers(),
                        reservation.getReservationSource()
                });
            }
        }
    }

    private Reservation getActiveReservationAtRow(int selectedRow) {
        int row = 0;
        for (Reservation reservation : data.getReservations()) {
            if (reservation.isActive() && "Confirmed".equals(reservation.getStatus())) {
                if (row == selectedRow) {
                    return reservation;
                }
                row++;
            }
        }
        return null;
    }
}
