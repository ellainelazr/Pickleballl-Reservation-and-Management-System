package pickleballsystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import pickleballsystem.model.ReservationActivityEntry;
import pickleballsystem.dsa.SinglyLinkedList;

/**
 * DSA: LINKED LIST.
 * Keeps the activity log (logins, bookings, cancellations) with the newest entry first.
 */
public class ReservationHistory {
    private final SinglyLinkedList<ReservationActivityEntry> entries = new SinglyLinkedList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void recordActivity(String message) {
        entries.addFirst(new ReservationActivityEntry(message, LocalDateTime.now().format(FORMATTER)));
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public ArrayList<ReservationActivityEntry> getRecentActivities(int count) {
        ArrayList<ReservationActivityEntry> result = new ArrayList<>();
        ArrayList<ReservationActivityEntry> all = entries.toArrayList();
        int max = Math.min(count, all.size());
        for (int i = 0; i < max; i++) {
            result.add(all.get(i));
        }
        return result;
    }
}
