package pickleballsystem.dsa;

import pickleballsystem.model.Reservation;
import java.util.ArrayList;
import pickleballsystem.model.Reservation;

/**
 * DSA: SORTING ALGORITHM (merge sort).
 * Sorts reservations by date, oldest first. Merge sort is stable, so reservations
 * with the same date keep their original order.
 */
public final class SortUtil {

    private SortUtil() {
    }

    /** Sorts the list in place by reservation date (ISO dates compare correctly as text). */
    public static void mergeSortByDate(ArrayList<Reservation> list) {
        if (list.size() < 2) {
            return;
        }
        ArrayList<Reservation> left = new ArrayList<>(list.subList(0, list.size() / 2));
        ArrayList<Reservation> right = new ArrayList<>(list.subList(list.size() / 2, list.size()));
        mergeSortByDate(left);
        mergeSortByDate(right);

        int l = 0;
        int r = 0;
        int k = 0;
        while (l < left.size() && r < right.size()) {
            // <= takes from the left on ties, which keeps the sort stable.
            if (left.get(l).getReservationDate().compareTo(right.get(r).getReservationDate()) <= 0) {
                list.set(k++, left.get(l++));
            } else {
                list.set(k++, right.get(r++));
            }
        }
        while (l < left.size()) {
            list.set(k++, left.get(l++));
        }
        while (r < right.size()) {
            list.set(k++, right.get(r++));
        }
    }
}
