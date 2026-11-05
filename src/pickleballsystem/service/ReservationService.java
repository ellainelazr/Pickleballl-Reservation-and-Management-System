package pickleballsystem.service;

import java.util.ArrayList;
import java.util.Locale;
import pickleballsystem.model.Court;
import pickleballsystem.model.Reservation;

public class ReservationService {
    public boolean isCourtBooked(String courtName, String date, String timeSlot,
                                 ArrayList<Reservation> reservations) {
        if (courtName == null || date == null || timeSlot == null || reservations == null) {
            return false;
        }

        for (Reservation reservation : reservations) {
            if (reservation == null || !reservation.isActive()) {
                continue;
            }
            if (courtName.equalsIgnoreCase(reservation.getCourtName())
                    && date.equals(reservation.getReservationDate())
                    && timeSlot.equalsIgnoreCase(reservation.getTimeSlot())) {
                return true;
            }
        }
        return false;
    }

    public double calculateCourtRate(Court court, String timeSlot) {
        if (court == null) {
            return 0.0;
        }
        if (!isPeakHour(timeSlot)) {
            return court.getRegularRatePerHour();
        }
        return court.getPeakRatePerHour();
    }

    public boolean isPeakHour(String timeSlot) {
        if (timeSlot == null || timeSlot.isBlank()) {
            return false;
        }

        try {
            String startPart = timeSlot.split("-")[0].trim();
            String[] parts = startPart.split(" ");
            if (parts.length < 2) {
                return false;
            }

            String timePart = parts[0];
            String amPm = parts[1].toUpperCase(Locale.ROOT);
            String[] hourMinute = timePart.split(":");
            int hour = Integer.parseInt(hourMinute[0]);
            int minute = hourMinute.length > 1 ? Integer.parseInt(hourMinute[1]) : 0;

            if ("AM".equals(amPm)) {
                return false;
            }
            if ("PM".equals(amPm)) {
                return hour >= 1 && hour <= 7 && minute >= 0;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return false;
    }
}
