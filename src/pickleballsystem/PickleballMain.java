package pickleballsystem;

import pickleballsystem.service.ReceiptWriter;
import pickleballsystem.service.SystemData;
import pickleballsystem.service.AccountManager;
import pickleballsystem.model.ReservationSlot;
import pickleballsystem.model.Staff;
import pickleballsystem.ui.AdminBookingHistoryScreen;
import pickleballsystem.ui.AdminEquipmentScreen;
import pickleballsystem.ui.AdminStaffScreen;
import pickleballsystem.ui.AdminPricingScreen;
import pickleballsystem.ui.PaymentDialog;
import pickleballsystem.ui.GuestBookingDialog;
import pickleballsystem.ui.EquipmentRentalDialog;
import pickleballsystem.ui.CustomerDashboardScreen;
import pickleballsystem.ui.CourtReservationScreen;
import pickleballsystem.ui.BookingConfirmationDialog;
import pickleballsystem.ui.BookingCalendar;
import pickleballsystem.ui.AdminReservationsScreen;
import pickleballsystem.ui.IntroScreen;
import pickleballsystem.ui.AdminDashboardScreen;
import pickleballsystem.ui.LoginScreen;
import pickleballsystem.ui.ReservationSummaryDialog;
import pickleballsystem.ui.StaffOnsiteScreen;
import pickleballsystem.ui.AdminCourtScreen;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import static pickleballsystem.ui.UiHelper.*;

/** Program entry point and navigator: creates the data objects and screens, and moves between screens. */
public class PickleballMain extends JFrame {

    private final SystemData data = new SystemData();
    private final AccountManager accounts = new AccountManager();
    private final IntroScreen introScreen = new IntroScreen(this);
    private final LoginScreen loginScreen = new LoginScreen(this);
    private final CustomerDashboardScreen customerDashboardScreen = new CustomerDashboardScreen(this);
    private final StaffOnsiteScreen staffOnsiteScreen = new StaffOnsiteScreen(this);
    private final CourtReservationScreen courtReservationScreen = new CourtReservationScreen(this);
    private final EquipmentRentalDialog equipmentRentalDialog = new EquipmentRentalDialog(this);
    private final ReservationSummaryDialog reservationSummaryDialog = new ReservationSummaryDialog(this);
    private final GuestBookingDialog guestBookingDialog = new GuestBookingDialog(this);
    private final PaymentDialog paymentDialog = new PaymentDialog(this);
    private final BookingConfirmationDialog bookingConfirmationDialog = new BookingConfirmationDialog(this);
    private final ReceiptWriter receiptWriter = new ReceiptWriter(this);
    private final AdminDashboardScreen adminDashboardScreen = new AdminDashboardScreen(this);
    private final AdminStaffScreen adminStaffScreen = new AdminStaffScreen(this);
    private final AdminEquipmentScreen adminEquipmentScreen = new AdminEquipmentScreen(this);
    private final AdminReservationsScreen adminReservationsScreen = new AdminReservationsScreen(this);
    private final AdminBookingHistoryScreen adminBookingHistoryScreen = new AdminBookingHistoryScreen(this);
    private final AdminCourtScreen adminCourtScreen = new AdminCourtScreen(this);
    private final AdminPricingScreen adminPricingScreen = new AdminPricingScreen(this);
    private final BookingCalendar bookingCalendar = new BookingCalendar(this);

    public SystemData getData() {
        return data;
    }

    public AccountManager getAccounts() {
        return accounts;
    }

    public boolean addStaffFromAdmin(JFrame parent) {
        return adminStaffScreen.addStaffFromAdmin(parent);
    }

    public void confirmMultipleSlotReservation(JFrame parentFrame, DefaultTableModel model, String customerName, String customerEmail, String contactNumber, int numberOfPlayers, String paymentMethod) {
        bookingConfirmationDialog.confirmMultipleSlotReservation(parentFrame, model, customerName, customerEmail, contactNumber, numberOfPlayers, paymentMethod);
    }

    public JPanel createAdminSidebar(JFrame currentFrame, String selectedPage) {
        return adminDashboardScreen.createAdminSidebar(currentFrame, selectedPage);
    }

    public JSpinner createDatePicker(String isoDate) {
        return bookingCalendar.createDatePicker(isoDate);
    }

    public JPanel createDatePickerControl(JFrame parentFrame, JSpinner datePicker) {
        return bookingCalendar.createDatePickerControl(parentFrame, datePicker);
    }

    public boolean deleteStaffFromAdmin(JFrame parent, Staff staff) {
        return adminStaffScreen.deleteStaffFromAdmin(parent, staff);
    }

    public boolean editStaffFromAdmin(JFrame parent, Staff staff) {
        return adminStaffScreen.editStaffFromAdmin(parent, staff);
    }

    public String getDateFromPicker(JSpinner datePicker) {
        return bookingCalendar.getDateFromPicker(datePicker);
    }

    public boolean manageBookingMonths(JFrame parentFrame) {
        return bookingCalendar.manageBookingMonths(parentFrame);
    }

    public void refreshAvailabilityTable(DefaultTableModel model, String selectedBookingDate) {
        courtReservationScreen.refreshAvailabilityTable(model, selectedBookingDate);
    }

    public void saveReceiptFile(JDialog parentDialog, String bookingReference, ArrayList<ReservationSlot> bookingDetails, double totalPrice, String customerEmail, String contactNumber, String paymentMethod) {
        receiptWriter.saveReceiptFile(parentDialog, bookingReference, bookingDetails, totalPrice, customerEmail, contactNumber, paymentMethod);
    }

    public void showAdminBookingHistoryScreen() {
        adminBookingHistoryScreen.showAdminBookingHistoryScreen();
    }

    public void showAdminCourtAvailabilityScreen() {
        adminCourtScreen.showAdminCourtAvailabilityScreen();
    }

    public void showAdminDashboard() {
        adminDashboardScreen.showAdminDashboard();
    }

    public void showAdminEquipmentScreen() {
        adminEquipmentScreen.showAdminEquipmentScreen();
    }

    public void showAdminPricingScreen() {
        adminPricingScreen.showAdminPricingScreen();
    }

    public void showAdminReservationsScreen() {
        adminReservationsScreen.showAdminReservationsScreen();
    }

    public void showAdminStaffScreen() {
        adminStaffScreen.showAdminStaffScreen();
    }

    public void showCourtLayoutDialog(JFrame parentFrame) {
        introScreen.showCourtLayoutDialog(parentFrame);
    }

    public void showCourtReservationScreen() {
        courtReservationScreen.showCourtReservationScreen();
    }

    public void showCourtReservationScreen(boolean returnToHome) {
        courtReservationScreen.showCourtReservationScreen(returnToHome);
    }

    public void showCustomerDashboard() {
        customerDashboardScreen.showCustomerDashboard();
    }

    public void showEquipmentRentalDialog(JFrame parentFrame, DefaultTableModel model, JTable availabilityTable, Runnable updateSelection) {
        equipmentRentalDialog.showEquipmentRentalDialog(parentFrame, model, availabilityTable, updateSelection);
    }

    public void showGuestBookingDialog(JFrame parentFrame, DefaultTableModel model, JTable availabilityTable, Runnable updateSelection) {
        guestBookingDialog.showGuestBookingDialog(parentFrame, model, availabilityTable, updateSelection);
    }

    public void showIntroScreen() {
        introScreen.showIntroScreen();
    }

    public void showLoginScreen() {
        loginScreen.showLoginScreen();
    }

    public void showPasswordChangeDialog(JFrame parentFrame) {
        loginScreen.showPasswordChangeDialog(parentFrame);
    }

    public void showPaymentMethodDialog(JFrame parentFrame, JDialog guestDialog, DefaultTableModel model, JTable availabilityTable, Runnable updateSelection, String customerName, String customerEmail, String contactNumber, int numberOfPlayers) {
        paymentDialog.showPaymentMethodDialog(parentFrame, guestDialog, model, availabilityTable, updateSelection, customerName, customerEmail, contactNumber, numberOfPlayers);
    }

    public void showReservationSummaryDialog(JFrame parentFrame, DefaultTableModel model, JTable availabilityTable, Runnable updateSelection) {
        reservationSummaryDialog.showReservationSummaryDialog(parentFrame, model, availabilityTable, updateSelection);
    }

    public void showStaffOnsiteScreen() {
        staffOnsiteScreen.showStaffOnsiteScreen();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PickleballMain().showIntroScreen());
    }

    public JPanel createHeaderPanel(String title, JFrame frame, boolean isAdmin) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(isAdmin ? INTRO_DARK_TEAL : CYAN);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 70));

        JLabel titleLabel = new JLabel(isAdmin ? "  SmashPoint  |  " + title : title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    public JPanel createNavPanel(JFrame parentFrame, Runnable action1, Runnable action2,
                                  Runnable action3, Runnable logoutAction) {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        navPanel.setBackground(LIGHT_GRAY);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton button1 = createNavButton(accounts.isAdminLoggedIn() ? "View Reservations" : "Book a Court", TEAL, action1);
        JButton button2 = createNavButton(accounts.isAdminLoggedIn() ? "Court Availability" : "Court Layout", TEAL, action2);
        JButton button3 = null;

        if (action3 != null) {
            button3 = createNavButton(accounts.isStaffLoggedIn() ? "Onsite Check-in" : "Manage Reservations", TEAL, action3);
            navPanel.add(button1);
            navPanel.add(button2);
            navPanel.add(button3);
        } else {
            navPanel.add(button1);
            navPanel.add(button2);
        }

        if (accounts.isStaffLoggedIn()) {
            navPanel.add(createNavButton("Change Password", INTRO_TEAL,
                    () -> showPasswordChangeDialog(parentFrame)));
        }
        JButton logoutButton = createNavButton("Logout", RED, logoutAction);
        navPanel.add(logoutButton);

        return navPanel;
    }
}
