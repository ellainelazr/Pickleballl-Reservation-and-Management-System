package pickleballsystem.dsa;

import pickleballsystem.model.Reservation;
import java.util.ArrayList;

/**
 * DSA: SEARCHING ALGORITHM (linear search).
 * Walks through the list one reservation at a time and collects every reservation
 * that belongs to the same booking (a booking with several slots shares one reference).
 */
public final class SearchUtil {

    private SearchUtil() {
    }

    /** True when both reservations are the same object or share a non-empty booking reference. */
    public static boolean isSameBooking(Reservation selected, Reservation other) {
        String reference = selected.getBookingReference();
        return other == selected
                || (reference != null && !reference.isEmpty() && reference.equals(other.getBookingReference()));
    }

    /** Linear search: returns every reservation in the list that is part of the selected booking. */
    public static ArrayList<Reservation> findSameBooking(ArrayList<Reservation> list, Reservation selected) {
        ArrayList<Reservation> matches = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (isSameBooking(selected, list.get(i))) {
                matches.add(list.get(i));
            }
        }
        return matches;
    }
}
