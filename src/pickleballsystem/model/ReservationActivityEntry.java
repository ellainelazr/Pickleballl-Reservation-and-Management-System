package pickleballsystem.model;

public class ReservationActivityEntry {
    private final String message;
    private final String timestamp;

    public ReservationActivityEntry(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " - " + message;
    }
}
