package pickleballsystem.service;

import pickleballsystem.model.Reservation;
import pickleballsystem.model.Staff;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileStorageService {
    private final Path dataDirectory;

    public FileStorageService() {
        this(Paths.get("data"));
    }

    public FileStorageService(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        ensureDirectory();
    }


    public void saveSystemSettings(double regularRate, double peakRate,
            double operatingExpense, double paddleRate, double ballRate) throws IOException {
        Path path = dataDirectory.resolve("system-settings.txt");
        List<String> lines = new ArrayList<>();
        lines.add("regularRate=" + regularRate);
        lines.add("peakRate=" + peakRate);
        lines.add("operatingExpense=" + operatingExpense);
        lines.add("paddleRate=" + paddleRate);
        lines.add("ballRate=" + ballRate);
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public Map<String, Double> loadSystemSettings() throws IOException {
        Path path = dataDirectory.resolve("system-settings.txt");
        Map<String, Double> settings = new HashMap<>();
        if (!Files.exists(path)) {
            return settings;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                int separator = line.indexOf('=');
                if (separator > 0) {
                    settings.put(line.substring(0, separator).trim(),
                            Double.parseDouble(line.substring(separator + 1).trim()));
                }
            }
        } catch (NumberFormatException ex) {
            throw new IOException("Saved pricing settings contain an invalid number.", ex);
        }
        return settings;
    }

    public void saveRentalItems(ArrayList<pickleballsystem.model.RentalItem> items)
            throws IOException {
        Path path = dataDirectory.resolve("equipment.txt");
        List<String> lines = new ArrayList<>();
        for (pickleballsystem.model.RentalItem item : items) {
            lines.add(cleanField(item.getName()) + "|"
                    + item.getHourlyRate() + "|" + item.getTotalStock());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public ArrayList<pickleballsystem.model.RentalItem> loadRentalItems() throws IOException {
        ArrayList<pickleballsystem.model.RentalItem> items = new ArrayList<>();
        Path path = dataDirectory.resolve("equipment.txt");
        if (!Files.exists(path)) {
            return items;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length == 3 && !parts[0].trim().isEmpty()) {
                    items.add(new pickleballsystem.model.RentalItem(
                            parts[0].trim(), Double.parseDouble(parts[1].trim()),
                            Integer.parseInt(parts[2].trim())));
                }
            }
        } catch (NumberFormatException ex) {
            throw new IOException("Saved equipment data contains an invalid number.", ex);
        }
        return items;
    }

    public void saveAccounts(Map<String, String> accountsByUsername,
            Map<String, Staff> staffByUsername) throws IOException {
        Path path = dataDirectory.resolve("accounts.txt");
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : accountsByUsername.entrySet()) {
            Staff staff = staffByUsername.get(entry.getKey());
            if (staff == null) {
                lines.add(entry.getKey() + "|" + entry.getValue());
            } else {
                lines.add(entry.getKey() + "|" + entry.getValue() + "|"
                        + cleanField(staff.getName()) + "|" + staff.isActive());
            }
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public Map<String, String> loadAccounts() {
        Path path = dataDirectory.resolve("accounts.txt");
        Map<String, String> result = new HashMap<>();
        if (!Files.exists(path)) {
            return result;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 2) {
                    result.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        } catch (IOException ex) {
            return result;
        }
        return result;
    }

    public Map<String, Staff> loadStaffDetails() {
        Path path = dataDirectory.resolve("accounts.txt");
        Map<String, Staff> result = new HashMap<>();
        if (!Files.exists(path)) {
            return result;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4 && !parts[0].trim().isEmpty()) {
                    Staff staff = new Staff("", parts[2].trim());
                    staff.setActive(Boolean.parseBoolean(parts[3].trim()));
                    result.put(parts[0].trim().toLowerCase(), staff);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read saved staff details.", ex);
        }
        return result;
    }

    public Set<YearMonth> loadOpenBookingMonths(Set<YearMonth> defaultMonths) throws IOException {
        Path path = dataDirectory.resolve("open-booking-months.txt");
        Set<YearMonth> months = new HashSet<>(defaultMonths);
        if (Files.exists(path)) {
            months.clear();
            try {
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    if (!line.trim().isEmpty()) {
                        months.add(YearMonth.parse(line.trim()));
                    }
                }
            } catch (RuntimeException ex) {
                throw new IOException("Could not read the saved booking months.", ex);
            }
            return months;
        }

        Path oldPath = dataDirectory.resolve("booking-month.txt");
        if (!Files.exists(oldPath)) {
            return months;
        }
        try {
            YearMonth lastMonth = YearMonth.parse(
                    Files.readString(oldPath, StandardCharsets.UTF_8).trim());
            months.clear();
            for (YearMonth month = YearMonth.of(2026, 10);
                    !month.isAfter(lastMonth); month = month.plusMonths(1)) {
                months.add(month);
            }
        } catch (RuntimeException ex) {
            throw new IOException("Could not read the saved booking months.", ex);
        }
        return months;
    }

    public void saveOpenBookingMonths(Set<YearMonth> months) throws IOException {
        Path path = dataDirectory.resolve("open-booking-months.txt");
        List<String> lines = new ArrayList<>();
        for (YearMonth month : months) {
            lines.add(month.toString());
        }
        lines.sort(String::compareTo);
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public void saveReservations(ArrayList<Reservation> reservations) throws IOException {
        Path path = dataDirectory.resolve("reservations.txt");
        List<String> lines = new ArrayList<>();
        for (Reservation reservation : reservations) {
            String line = String.join("|",
                    cleanField(reservation.getCustomerName()),
                    cleanField(reservation.getCourtName()),
                    cleanField(reservation.getReservationDate()),
                    cleanField(reservation.getTimeSlot()),
                    cleanField(reservation.getBookingReference()),
                    cleanField(reservation.getPaymentStatus()),
                    cleanField(reservation.getCustomerEmail()),
                    cleanField(reservation.getContactNumber()),
                    cleanField(reservation.getReservationSource()),
                    String.valueOf(reservation.getNumberOfPlayers()),
                    String.valueOf(reservation.getPlayersOnsite()),
                    String.valueOf(reservation.getNumberOfHours()),
                    String.valueOf(reservation.getCourtPrice()),
                    String.valueOf(reservation.getTotalPrice()),
                    String.valueOf(reservation.getPaddleQuantity()),
                    String.valueOf(reservation.getBallQuantity()),
                    String.valueOf(reservation.isActive()),
                    String.valueOf(reservation.isCompleted()),
                    cleanField(reservation.getStatus()));
            lines.add(line);
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public ArrayList<Reservation> loadReservations() throws IOException {
        ArrayList<Reservation> result = new ArrayList<>();
        Path path = dataDirectory.resolve("reservations.txt");
        if (!Files.exists(path)) {
            return result;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) {
                    Reservation reservation = new Reservation();
                    reservation.setCustomerName(parts[0]);
                    reservation.setCourtName(parts[1]);
                    reservation.setReservationDate(parts[2]);
                    reservation.setTimeSlot(parts[3]);
                    reservation.setBookingReference(parts[4]);
                    reservation.setPaymentStatus(parts[5]);
                    if (parts.length >= 19) {
                        reservation.setCustomerEmail(parts[6]);
                        reservation.setContactNumber(parts[7]);
                        reservation.setReservationSource(parts[8]);
                        reservation.setNumberOfPlayers(parseInt(parts[9]));
                        reservation.setPlayersOnsite(parseInt(parts[10]));
                        reservation.setNumberOfHours(parseInt(parts[11]));
                        reservation.setCourtPrice(parseDouble(parts[12]));
                        reservation.setTotalPrice(parseDouble(parts[13]));
                        reservation.setPaddleQuantity(parseInt(parts[14]));
                        reservation.setBallQuantity(parseInt(parts[15]));
                        reservation.setActive(Boolean.parseBoolean(parts[16]));
                        reservation.setCompleted(Boolean.parseBoolean(parts[17]));
                        reservation.setStatus(parts[18]);
                    } else {
                        reservation.setActive(Boolean.parseBoolean(parts[6]));
                        reservation.setCompleted(Boolean.parseBoolean(parts[7]));
                        if (reservation.isCompleted()) {
                            reservation.setStatus("Completed");
                        } else if (!reservation.isActive()) {
                            reservation.setStatus("Cancelled");
                        } else {
                            reservation.setStatus("Confirmed");
                        }
                    }
                    result.add(reservation);
                }
            }
        } catch (NumberFormatException ex) {
            throw new IOException("A saved reservation contains an invalid number.", ex);
        }
        return result;
    }

    private String cleanField(String value) {
        return value == null ? "" : value.replace('|', ' ');
    }

    private int parseInt(String value) {
        return Integer.parseInt(value);
    }

    private double parseDouble(String value) {
        return Double.parseDouble(value);
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create data directory", e);
        }
    }
}
