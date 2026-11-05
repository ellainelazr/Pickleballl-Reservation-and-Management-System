package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.Reservation;
import pickleballsystem.dsa.SortUtil;

import static pickleballsystem.ui.UiHelper.*;

/** Admin screen listing all past and current bookings. */
public class AdminBookingHistoryScreen extends BaseScreen {

    public AdminBookingHistoryScreen(PickleballMain app) {
        super(app);
    }

    public void showAdminBookingHistoryScreen() {
        JFrame historyFrame = new JFrame("SmashPoint Pickleball - Booking History");
        historyFrame.setSize(1420, 760);
        historyFrame.setMinimumSize(new Dimension(1100, 650));
        historyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        historyFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel description = new JLabel(
                "Online and onsite reservations, including cancelled and completed bookings.");
        description.setFont(new Font("Arial", Font.PLAIN, 14));
        description.setForeground(INTRO_MUTED);
        mainPanel.add(description, BorderLayout.NORTH);

        DefaultTableModel model = readOnlyTableModel(new String[]{
            "Customer / Booker", "Email", "Contact", "Players", "Court", "Date", "Time",
            "Booking Source", "Booking Reference", "Status"
        });
        JTable historyTable = new JTable(model);
        historyTable.setRowHeight(30);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(TEAL);
        historyTable.getTableHeader().setForeground(WHITE);
        refreshAdminBookingHistoryTable(model);
        mainPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JPanel page = new JPanel(new BorderLayout());
        page.add(app.createAdminSidebar(historyFrame, "Booking History"), BorderLayout.WEST);
        page.add(mainPanel, BorderLayout.CENTER);
        historyFrame.add(app.createHeaderPanel("Booking History", historyFrame, true), BorderLayout.NORTH);
        historyFrame.add(page, BorderLayout.CENTER);
        historyFrame.setVisible(true);
    }

    private void refreshAdminBookingHistoryTable(DefaultTableModel model) {
        model.setRowCount(0);
        ArrayList<Reservation> history = new ArrayList<>(data.getReservations());
        history.addAll(data.getCompletedReservations());
        SortUtil.mergeSortByDate(history); // DSA: sorting algorithm
        for (Reservation reservation : history) {
            String bookingReference = reservation.getBookingReference();
            model.addRow(new Object[]{
                reservation.getCustomerName(),
                emptyAsDash(reservation.getCustomerEmail()),
                emptyAsDash(reservation.getContactNumber()),
                reservation.getNumberOfPlayers(),
                reservation.getCourtName(),
                reservation.getReservationDate(),
                reservation.getTimeSlot(),
                reservation.getReservationSource(),
                bookingReference == null || bookingReference.isEmpty() ? "—" : bookingReference,
                reservation.getStatus()
            });
        }
    }
}
