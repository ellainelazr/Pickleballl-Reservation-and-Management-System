package pickleballsystem.model;

public class WaitingListEntry {
    private final String customerName;
    private final String courtName;
    private final String date;
    private final String timeSlot;
    private final String requestedAt;

    public WaitingListEntry(String customerName, String courtName, String date, String timeSlot) {
        this.customerName = customerName;
        this.courtName = courtName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.requestedAt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCourtName() {
        return courtName;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getRequestedAt() {
        return requestedAt;
    }
}
