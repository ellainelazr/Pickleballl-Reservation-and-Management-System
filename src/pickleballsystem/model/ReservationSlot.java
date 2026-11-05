package pickleballsystem.model;

/** One booked court time slot (court, time, and price) used while confirming a booking and printing receipts. */
public class ReservationSlot {
    private final String courtName;
    private final String timeSlot;
    private final double courtPrice;

    public ReservationSlot(String courtName, String timeSlot, double courtPrice) {
        this.courtName = courtName;
        this.timeSlot = timeSlot;
        this.courtPrice = courtPrice;
    }

    public String getCourtName() {
        return courtName;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public double getCourtPrice() {
        return courtPrice;
    }
}
