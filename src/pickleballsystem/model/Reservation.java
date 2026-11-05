package pickleballsystem.model;

import pickleballsystem.model.Court;
import java.util.ArrayList;
import pickleballsystem.model.RentalRecord;

public class Reservation {
    private String customerName;
    private String courtName;
    private String reservationDate;
    private String timeSlot;
    private String customerEmail;
    private String contactNumber;
    private String paymentStatus;
    private String bookingReference;
    private String reservationSource;
    private String status;
    private int numberOfPlayers;
    private int playersOnsite;
    private int numberOfHours;
    private double courtPrice;
    private double totalPrice;
    private int paddleQuantity;
    private int ballQuantity;
    private boolean active;
    private boolean completed;
    private ArrayList<RentalRecord> rentedItems;

    public Reservation() {
        this.reservationSource = "Online";
        this.status = "Pending";
        this.playersOnsite = 0;
        this.active = true;
        this.completed = false;
        this.rentedItems = new ArrayList<>();
    }

    public Reservation(String customerName, String courtName, String reservationDate,
                      String timeSlot, int numberOfPlayers, double totalPrice) {
        this.customerName = customerName;
        this.courtName = courtName;
        this.reservationDate = reservationDate;
        this.timeSlot = timeSlot;
        this.numberOfPlayers = numberOfPlayers;
        this.totalPrice = totalPrice;
        this.reservationSource = "Online";
        this.status = "Pending";
        this.playersOnsite = 0;
        this.active = true;
        this.completed = false;
        this.rentedItems = new ArrayList<>();
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getReservationSource() {
        return reservationSource;
    }

    public void setReservationSource(String reservationSource) {
        this.reservationSource = reservationSource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public int getPlayersOnsite() {
        return playersOnsite;
    }

    public void setPlayersOnsite(int playersOnsite) {
        this.playersOnsite = playersOnsite;
    }

    public int getNumberOfHours() {
        return numberOfHours;
    }

    public void setNumberOfHours(int numberOfHours) {
        this.numberOfHours = numberOfHours;
    }

    public double getCourtPrice() {
        return courtPrice;
    }

    public void setCourtPrice(double courtPrice) {
        this.courtPrice = courtPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getPaddleQuantity() {
        return paddleQuantity;
    }

    public void setPaddleQuantity(int paddleQuantity) {
        this.paddleQuantity = paddleQuantity;
    }

    public int getBallQuantity() {
        return ballQuantity;
    }

    public void setBallQuantity(int ballQuantity) {
        this.ballQuantity = ballQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public ArrayList<RentalRecord> getRentedItems() {
        return rentedItems;
    }

    public void addRentedItem(String name, double hourlyRate, int quantity) {
        rentedItems.add(new RentalRecord(name, hourlyRate, quantity));
    }

    public double calculateTotalPrice(double ratePerHour) {
        return ratePerHour * numberOfHours;
    }

    public double calculateTotalPrice(double ratePerHour, int hours) {
        return ratePerHour * hours;
    }

    public double calculateTotalPrice(Court court) {
        if (court == null) return 0;
        return court.getRatePerHour() * numberOfHours;
    }

    public String getReservationDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Customer: ").append(customerName)
                .append("\nCourt: ").append(courtName)
                .append("\nDate: ").append(reservationDate)
                .append("\nTime: ").append(timeSlot)
                .append("\nPlayers: ").append(numberOfPlayers)
                .append("\nHours: ").append(numberOfHours)
                .append("\nPaddles: ").append(paddleQuantity)
                .append("\nBalls: ").append(ballQuantity)
                .append("\nCourt Price: ₱").append(String.format("%.0f", courtPrice))
                .append("\nTotal: ₱").append(String.format("%.0f", totalPrice));
        return details.toString();
    }
}
