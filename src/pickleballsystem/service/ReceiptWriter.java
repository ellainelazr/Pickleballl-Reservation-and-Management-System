package pickleballsystem.service;

import pickleballsystem.model.ReservationSlot;
import pickleballsystem.model.RentalItem;
import pickleballsystem.ui.BaseScreen;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import pickleballsystem.PickleballMain;

import static pickleballsystem.ui.UiHelper.*;

/** Builds the receipt text and saves it as a file in the receipts folder. */
public class ReceiptWriter extends BaseScreen {

    public ReceiptWriter(PickleballMain app) {
        super(app);
    }

    public void saveReceiptFile(JDialog parentDialog, String bookingReference,
                                 ArrayList<ReservationSlot> bookingDetails, double totalPrice,
                                 String customerEmail, String contactNumber, String paymentMethod) {
        JFileChooser fileChooser = new JFileChooser();
        File documentsFolder = new File(System.getProperty("user.home"), "Documents");
        if (documentsFolder.isDirectory()) {
            fileChooser.setCurrentDirectory(documentsFolder);
        }
        fileChooser.setDialogTitle(accounts.isStaffLoggedIn() ? "Save Receipt File" : "Save Reservation Request");
        fileChooser.setSelectedFile(new File(bookingReference + "-receipt.txt"));

        if (fileChooser.showSaveDialog(parentDialog) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".txt")) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".txt");
        }

        Path receiptPath = selectedFile.toPath();
        if (Files.exists(receiptPath)) {
            int overwrite = JOptionPane.showConfirmDialog(parentDialog,
                    "A file with this name already exists. Replace it?",
                    "Replace Existing Receipt", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String receiptText = createReceiptText(bookingReference, bookingDetails, totalPrice,
                customerEmail, contactNumber, paymentMethod);
        try {
            Files.write(receiptPath, receiptText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | SecurityException ex) {
            JOptionPane.showMessageDialog(parentDialog,
                    "The receipt could not be saved.\n" + ex.getMessage(),
                    "Receipt Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(parentDialog,
                "Receipt saved to:\n" + receiptPath.toAbsolutePath(),
                "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private String createReceiptText(String bookingReference, ArrayList<ReservationSlot> bookingDetails,
                                     double totalPrice, String customerEmail, String contactNumber,
                                     String paymentMethod) {
        double courtTotal = 0;
        for (ReservationSlot slot : bookingDetails) {
            courtTotal += slot.getCourtPrice();
        }
        double equipmentTotal = data.getPaddleRentalTotal() + data.getBallRentalTotal();
        ArrayList<RentalItem> additionalItems = data.getAdditionalRentalItems();
        for (int i = 0; i < additionalItems.size() && i < data.getExtraRentalQuantities().size(); i++) {
            RentalItem item = additionalItems.get(i);
            int quantity = data.getExtraRentalQuantities().get(i);
            if (quantity > 0) {
                equipmentTotal += data.getExtraRentalTotal(item, quantity);
            }
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("============================================================\n")
                .append("                  SMASHPOINT PICKLEBALL\n")
                .append(accounts.isStaffLoggedIn()
                        ? "                    RESERVATION RECEIPT\n"
                        : "                    RESERVATION REQUEST\n")
                .append("============================================================\n\n")
                .append("BOOKING INFORMATION\n")
                .append("------------------------------------------------------------\n")
                .append(String.format("%-22s %s%n", "Booking Reference:", bookingReference))
                .append(String.format("%-22s %s%n", "Status:", accounts.isStaffLoggedIn() ? "Confirmed" : "Pending"))
                .append(String.format("%-22s %s%n", "Booking Date:", formatConfirmationDate(data.getSelectedReservationDate())))
                .append(String.format("%-22s %s%n", "Receipt Date:", formatConfirmationDate(LocalDate.now().toString())))
                .append(String.format("%-22s %s%n", "Customer:", accounts.getCurrentUserName()));
        if (customerEmail != null && !customerEmail.trim().isEmpty()) {
            receipt.append(String.format("%-22s %s%n", "Email:", customerEmail));
        }
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            receipt.append(String.format("%-22s %s%n", "Contact Number:", contactNumber));
        }
        receipt.append(String.format("%-22s %s%n", "Payment Method:", paymentMethod))
                .append("\nRESERVATION AND RENTALS\n")
                .append("------------------------------------------------------------\n")
                .append(String.format("%-20s %-25s %12s%n", "Description", "Details", "Amount"))
                .append("------------------------------------------------------------\n");

        for (ReservationSlot slot : bookingDetails) {
            appendReceiptLine(receipt, slot.getCourtName(), slot.getTimeSlot(), slot.getCourtPrice());
        }
        appendReceiptLine(receipt, "Paddle Rental",
                data.getSelectedPaddleQuantity() + " × " + formatCurrency(data.getPaddleRentalRate())
                        + " per hour × " + data.getSelectedCourtSlots().size() + " court-hour(s)",
                data.getPaddleRentalTotal());
        appendReceiptLine(receipt, "Ball Rental",
                data.getSelectedBallQuantity() + " × " + formatCurrency(data.getBallRentalRate())
                        + " per hour × " + data.getSelectedCourtSlots().size() + " court-hour(s)",
                data.getBallRentalTotal());
        for (int i = 0; i < additionalItems.size() && i < data.getExtraRentalQuantities().size(); i++) {
            RentalItem item = additionalItems.get(i);
            int quantity = data.getExtraRentalQuantities().get(i);
            if (quantity > 0) {
                appendReceiptLine(receipt, item.getName(),
                        quantity + " × " + formatCurrency(item.getHourlyRate())
                                + " per hour × " + data.getSelectedCourtSlots().size() + " court-hour(s)",
                        data.getExtraRentalTotal(item, quantity));
            }
        }
        receipt.append("------------------------------------------------------------\n")
                .append(String.format("%-35s %25s%n", "Court Charges:", formatCurrency(courtTotal)))
                .append(String.format("%-35s %25s%n", "Equipment Rental:", formatCurrency(equipmentTotal)))
                .append("============================================================\n")
                .append(String.format("%-35s %25s%n", "TOTAL AMOUNT:", formatCurrency(totalPrice)))
                .append("============================================================\n\n")
                .append("        Thank you for choosing SmashPoint Pickleball.\n");
        return receipt.toString();
    }

    private void appendReceiptLine(StringBuilder receipt, String itemName, String details, double amount) {
        receipt.append(String.format("%-20.20s %-25.25s %12s%n",
                itemName, details, formatCurrency(amount)));
    }
}
