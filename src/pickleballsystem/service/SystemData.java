package pickleballsystem.service;

import pickleballsystem.service.ReservationService;
import pickleballsystem.model.ReservationHistory;
import pickleballsystem.model.WaitingListEntry;
import pickleballsystem.model.RentalRecord;
import pickleballsystem.model.RentalItem;
import pickleballsystem.model.Reservation;
import pickleballsystem.model.Court;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import pickleballsystem.dsa.CustomQueue;
import pickleballsystem.service.FileStorageService;
import pickleballsystem.dsa.SearchUtil;


/** Holds the courts, reservations, rental stock, pricing, and booking rules (the system's data model). */
public class SystemData {

    public static final LocalDate FIRST_BOOKING_DATE = LocalDate.of(2026, 10, 1);
    private final ArrayList<Court> courts = new ArrayList<>();
    private final ArrayList<Reservation> reservations = new ArrayList<>();
    private final ArrayList<Reservation> completedReservations = new ArrayList<>();
    private final ArrayList<RentalItem> rentalItems = new ArrayList<>();
    private final Set<String> unavailableDates = new HashSet<>();
    private final Map<String, String> dateBlockReasons = new HashMap<>();
    private final Map<String, String> courtDateBlockReasons = new HashMap<>();
    private final Map<String, String> courtTimeBlockReasons = new HashMap<>();
    private final FileStorageService fileStorageService = new FileStorageService();
    // DSA (LINKED LIST): activity log kept in a singly linked list.
    private final ReservationHistory reservationHistory = new ReservationHistory();
    private final ReservationService reservationService = new ReservationService();
    // DSA (QUEUE): waiting list. Customers are enqueued first-come, first-served.
    private final CustomQueue<WaitingListEntry> waitingListQueue = new CustomQueue<>();
    private final ArrayList<Integer> extraRentalQuantities = new ArrayList<>();
    private final TreeSet<YearMonth> openBookingMonths = new TreeSet<>();
    private final Set<String> selectedCourtSlots = new HashSet<>();

    private double regularHourlyRate = 300.0;
    private double peakHourlyRate = 350.0;
    private double operatingExpense = 1500.0;
    private double paddleRentalRate = 50.0;
    private double ballRentalRate = 20.0;
    private int availablePaddles = 5;
    private int availableBalls = 3;
    private int selectedPaddleQuantity = 0;
    private int selectedBallQuantity = 0;
    private String selectedBookingDate = "";
    private String selectedReservationDate = "";
    private int dailyBookingNumber = 0;
    private String lastBookingDate = "";

    private final String[] availableTimeSlots = {
            "6:00 AM - 7:00 AM",
            "7:00 AM - 8:00 AM",
            "8:00 AM - 9:00 AM",
            "9:00 AM - 10:00 AM",
            "10:00 AM - 11:00 AM",
            "1:00 PM - 2:00 PM",
            "2:00 PM - 3:00 PM",
            "3:00 PM - 4:00 PM",
            "4:00 PM - 5:00 PM",
            "5:00 PM - 6:00 PM",
            "6:00 PM - 7:00 PM",
            "7:00 PM - 8:00 PM"
    };

    public SystemData() {
        Set<YearMonth> defaultMonths = new HashSet<>();
        defaultMonths.add(YearMonth.of(2026, 10));
        defaultMonths.add(YearMonth.of(2026, 11));
        openBookingMonths.addAll(defaultMonths);
        try {
            Set<YearMonth> savedMonths = fileStorageService.loadOpenBookingMonths(defaultMonths);
            openBookingMonths.clear();
            openBookingMonths.addAll(savedMonths);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "Booking Month Warning", JOptionPane.WARNING_MESSAGE);
        }
        selectedBookingDate = getFirstBookingDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        selectedReservationDate = selectedBookingDate;
        loadHardCodedData();
        loadSavedSettingsAndEquipment();
        loadPersistedReservations();
        reservationHistory.recordActivity("System initialized");
    }

    public ArrayList<Court> getCourts() {
        return courts;
    }

    public ArrayList<Reservation> getReservations() {
        return reservations;
    }

    public ArrayList<Reservation> getCompletedReservations() {
        return completedReservations;
    }

    public ArrayList<RentalItem> getRentalItems() {
        return rentalItems;
    }

    public Set<String> getUnavailableDates() {
        return unavailableDates;
    }

    public Map<String, String> getDateBlockReasons() {
        return dateBlockReasons;
    }

    public Map<String, String> getCourtDateBlockReasons() {
        return courtDateBlockReasons;
    }

    public Map<String, String> getCourtTimeBlockReasons() {
        return courtTimeBlockReasons;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public ReservationHistory getReservationHistory() {
        return reservationHistory;
    }

    public ArrayList<Integer> getExtraRentalQuantities() {
        return extraRentalQuantities;
    }

    public TreeSet<YearMonth> getOpenBookingMonths() {
        return openBookingMonths;
    }

    public Set<String> getSelectedCourtSlots() {
        return selectedCourtSlots;
    }

    public double getRegularHourlyRate() {
        return regularHourlyRate;
    }

    public void setRegularHourlyRate(double regularHourlyRate) {
        this.regularHourlyRate = regularHourlyRate;
    }

    public double getPeakHourlyRate() {
        return peakHourlyRate;
    }

    public void setPeakHourlyRate(double peakHourlyRate) {
        this.peakHourlyRate = peakHourlyRate;
    }

    public double getOperatingExpense() {
        return operatingExpense;
    }

    public void setOperatingExpense(double operatingExpense) {
        this.operatingExpense = operatingExpense;
    }

    public double getPaddleRentalRate() {
        return paddleRentalRate;
    }

    public void setPaddleRentalRate(double paddleRentalRate) {
        this.paddleRentalRate = paddleRentalRate;
    }

    public double getBallRentalRate() {
        return ballRentalRate;
    }

    public void setBallRentalRate(double ballRentalRate) {
        this.ballRentalRate = ballRentalRate;
    }

    public void setAvailablePaddles(int availablePaddles) {
        this.availablePaddles = availablePaddles;
    }

    public void setAvailableBalls(int availableBalls) {
        this.availableBalls = availableBalls;
    }

    public int getSelectedPaddleQuantity() {
        return selectedPaddleQuantity;
    }

    public void setSelectedPaddleQuantity(int selectedPaddleQuantity) {
        this.selectedPaddleQuantity = selectedPaddleQuantity;
    }

    public int getSelectedBallQuantity() {
        return selectedBallQuantity;
    }

    public void setSelectedBallQuantity(int selectedBallQuantity) {
        this.selectedBallQuantity = selectedBallQuantity;
    }

    public String getSelectedBookingDate() {
        return selectedBookingDate;
    }

    public String getSelectedReservationDate() {
        return selectedReservationDate;
    }

    public void setSelectedReservationDate(String selectedReservationDate) {
        this.selectedReservationDate = selectedReservationDate;
    }

    public String[] getAvailableTimeSlots() {
        return availableTimeSlots;
    }

    private void loadPersistedReservations() {
        try {
            for (Reservation reservation : fileStorageService.loadReservations()) {
                if (reservation.isCompleted()) {
                    completedReservations.add(reservation);
                } else {
                    reservations.add(reservation);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Saved reservations could not be loaded from data/reservations.txt.\n" + ex.getMessage(),
                    "Storage Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void saveReservationRecords(JFrame parentFrame) {
        ArrayList<Reservation> allReservations = new ArrayList<>(reservations);
        allReservations.addAll(completedReservations);
        try {
            fileStorageService.saveReservations(allReservations);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parentFrame,
                    "The reservation change is in memory, but could not be saved to data/reservations.txt.",
                    "Storage Warning", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void loadSavedSettingsAndEquipment() {
        try {
            Map<String, Double> settings = fileStorageService.loadSystemSettings();
            if (settings.containsKey("regularRate")) {
                regularHourlyRate = settings.get("regularRate");
            }
            if (settings.containsKey("peakRate")) {
                peakHourlyRate = settings.get("peakRate");
            }
            if (settings.containsKey("operatingExpense")) {
                operatingExpense = settings.get("operatingExpense");
            }
            if (settings.containsKey("paddleRate")) {
                paddleRentalRate = settings.get("paddleRate");
            }
            if (settings.containsKey("ballRate")) {
                ballRentalRate = settings.get("ballRate");
            }

            for (Court court : courts) {
                court.setRegularRatePerHour(regularHourlyRate);
                court.setPeakRatePerHour(peakHourlyRate);
            }

            ArrayList<RentalItem> savedItems = fileStorageService.loadRentalItems();
            if (!savedItems.isEmpty()) {
                rentalItems.clear();
                rentalItems.addAll(savedItems);
                for (RentalItem item : rentalItems) {
                    if ("Pickleball Paddle".equalsIgnoreCase(item.getName())) {
                        availablePaddles = item.getTotalStock();
                        paddleRentalRate = item.getHourlyRate();
                    } else if ("Pickleball Ball".equalsIgnoreCase(item.getName())) {
                        availableBalls = item.getTotalStock();
                        ballRentalRate = item.getHourlyRate();
                    }
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Saved pricing or equipment could not be loaded. Default values will be used.\n"
                    + ex.getMessage(),
                    "Data Loading Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void saveSystemSettings() throws IOException {
        fileStorageService.saveSystemSettings(regularHourlyRate, peakHourlyRate,
                operatingExpense, paddleRentalRate, ballRentalRate);
        fileStorageService.saveRentalItems(rentalItems);
    }

    public void saveEquipment() throws IOException {
        fileStorageService.saveRentalItems(rentalItems);
        fileStorageService.saveSystemSettings(regularHourlyRate, peakHourlyRate,
                operatingExpense, paddleRentalRate, ballRentalRate);
    }

    private void loadHardCodedData() {
        rentalItems.clear();
        rentalItems.add(new RentalItem("Pickleball Paddle", paddleRentalRate, availablePaddles));
        rentalItems.add(new RentalItem("Pickleball Ball", ballRentalRate, availableBalls));
        courts.add(new Court("Court 1", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 2", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 3", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 4", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 5", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 6", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 7", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 8", regularHourlyRate, peakHourlyRate));
        courts.add(new Court("Court 9", regularHourlyRate, peakHourlyRate));
    }

    /** DSA (QUEUE): adds the customer to the end of the waiting list. */
    public void addWaitingListEntry(String customerName, String courtName, String date, String timeSlot) {
        if (customerName == null || customerName.isBlank() || courtName == null || timeSlot == null || date == null) {
            return;
        }
        WaitingListEntry waitEntry = new WaitingListEntry(customerName, courtName, date, timeSlot);
        waitingListQueue.enqueue(waitEntry);
        reservationHistory.recordActivity("Waiting list: " + customerName + " on " + courtName + " " + date + " " + timeSlot);
    }

    /**
     * DSA (QUEUE): called when a slot is freed. Takes the first customer waiting for that
     * exact court, date, and time off the queue (first in, first out). Everyone else keeps their place.
     */
    public WaitingListEntry promoteWaitingList(String courtName, String date, String timeSlot) {
        WaitingListEntry promoted = null;
        int waiting = waitingListQueue.size();
        for (int i = 0; i < waiting; i++) {
            WaitingListEntry next = waitingListQueue.dequeue();
            boolean matches = next.getCourtName().equals(courtName)
                    && next.getDate().equals(date) && next.getTimeSlot().equals(timeSlot);
            if (promoted == null && matches) {
                promoted = next;
            } else {
                waitingListQueue.enqueue(next);
            }
        }
        if (promoted != null) {
            reservationHistory.recordActivity("Waiting list slot opened for " + promoted.getCustomerName()
                    + ": " + courtName + " " + date + " " + timeSlot);
        }
        return promoted;
    }

    public void updateBookingPlayersOnsite(Reservation selectedReservation, int playersOnsite) {
        // DSA: searching algorithm finds every slot that belongs to this booking
        for (Reservation reservation : SearchUtil.findSameBooking(reservations, selectedReservation)) {
            reservation.setPlayersOnsite(playersOnsite);
        }
    }

    public ArrayList<String> getSelectedSlotKeysInOrder() {
        ArrayList<String> orderedSlots = new ArrayList<>();
        for (String timeSlot : availableTimeSlots) {
            for (Court court : courts) {
                String key = court.getCourtName() + "|" + selectedReservationDate + "|" + timeSlot;
                if (selectedCourtSlots.contains(key)) {
                    orderedSlots.add(key);
                }
            }
        }
        return orderedSlots;
    }

    public int getMaximumAvailableEquipment(boolean paddles) {
        int maximumAvailable = paddles ? availablePaddles : availableBalls;
        for (String timeSlot : availableTimeSlots) {
            int selectedCourtsAtThisTime = getSelectedCourtCountAtTime(timeSlot);
            if (selectedCourtsAtThisTime > 0) {
                maximumAvailable = Math.min(maximumAvailable,
                        getAvailableEquipmentCount(timeSlot, paddles) / selectedCourtsAtThisTime);
            }
        }
        return maximumAvailable;
    }

    public int getMaximumAvailableEquipment(RentalItem item) {
        int maximumAvailable = item.getTotalStock();
        for (String timeSlot : availableTimeSlots) {
            int selectedCourtsAtThisTime = getSelectedCourtCountAtTime(timeSlot);
            if (selectedCourtsAtThisTime > 0) {
                maximumAvailable = Math.min(maximumAvailable,
                        getAvailableEquipmentCount(item.getName(), timeSlot) / selectedCourtsAtThisTime);
            }
        }
        return maximumAvailable;
    }

    public ArrayList<RentalItem> getAdditionalRentalItems() {
        ArrayList<RentalItem> additionalItems = new ArrayList<>();
        for (RentalItem item : rentalItems) {
            if (!isStandardRentalItem(item)) {
                additionalItems.add(item);
            }
        }
        return additionalItems;
    }

    public int getSelectedCourtCountAtTime(String timeSlot) {
        int selectedCount = 0;
        for (Court court : courts) {
            String key = court.getCourtName() + "|" + selectedReservationDate + "|" + timeSlot;
            if (selectedCourtSlots.contains(key)) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    public double getPaddleRentalTotal() {
        return selectedPaddleQuantity * paddleRentalRate * selectedCourtSlots.size();
    }

    public double getBallRentalTotal() {
        return selectedBallQuantity * ballRentalRate * selectedCourtSlots.size();
    }

    public double getEquipmentRentalTotal() {
        double total = getPaddleRentalTotal() + getBallRentalTotal();
        ArrayList<RentalItem> additionalItems = getAdditionalRentalItems();
        for (int i = 0; i < additionalItems.size() && i < extraRentalQuantities.size(); i++) {
            total += getExtraRentalTotal(additionalItems.get(i), extraRentalQuantities.get(i));
        }
        return total;
    }

    public double getExtraRentalTotal(RentalItem item, int quantity) {
        return quantity * item.getHourlyRate() * selectedCourtSlots.size();
    }

    public double getSelectedBookingTotal() {
        return calculateSelectedPrice() + getEquipmentRentalTotal();
    }

    // Get price for a specific time slot
    public String getTimeSlotPrice(int rowIndex, String courtName) {
        if (rowIndex < 0 || rowIndex >= availableTimeSlots.length) {
            return "₱300/hr";
        }
        String timeSlot = availableTimeSlots[rowIndex];
        double rate = getCourtRate(courtName, timeSlot);
        return "₱" + String.format("%.0f", rate) + "/hr";
    }

    // Calculate total price of all selected slots
    public double calculateSelectedPrice() {
        double totalPrice = 0;
        for (String slotKey : selectedCourtSlots) {
            String[] parts = slotKey.split("\\|");
            if (parts.length == 3) {
                String timeSlot = parts[2];
                double rate = getCourtRate(parts[0], timeSlot);
                totalPrice += rate;
            }
        }
        return totalPrice;
    }

    // Initialize daily booking counter
    public void initializeDailyBookingCounter(String date) {
        if (!date.equals(lastBookingDate)) {
            dailyBookingNumber = 0;
            lastBookingDate = date;
        }
    }

    // Get next sequential booking number for the date
    private int getNextBookingNumber(String date) {
        if (!date.equals(lastBookingDate)) {
            dailyBookingNumber = 0;
            lastBookingDate = date;
        }
        dailyBookingNumber++;
        return dailyBookingNumber;
    }

    // Generate booking reference with format: BK-YYYYMMDD-###
    public String generateBookingReference(String dateString) {
        String formattedDate = dateString.replace("-", "");
        int bookingNumber = getNextBookingNumber(dateString);
        return String.format("BK-%s-%03d", formattedDate, bookingNumber);
    }

    public int getAvailableEquipmentCount(String timeSlot, boolean paddles) {
        return getAvailableEquipmentCount(paddles ? "Pickleball Paddle" : "Pickleball Ball", timeSlot);
    }

    public int getAvailableEquipmentCount(String equipmentName, String timeSlot) {
        RentalItem item = findRentalItemByName(equipmentName);
        if (item == null) {
            return 0;
        }
        int rented = 0;
        for (Reservation reservation : reservations) {
            if (reservation.isActive()
                    && reservation.getReservationDate().equals(selectedReservationDate)
                    && reservation.getTimeSlot().equals(timeSlot)) {
                if ("Pickleball Paddle".equalsIgnoreCase(equipmentName)) {
                    rented += reservation.getPaddleQuantity();
                } else if ("Pickleball Ball".equalsIgnoreCase(equipmentName)) {
                    rented += reservation.getBallQuantity();
                }
                for (RentalRecord rentedItem : reservation.getRentedItems()) {
                    if (rentedItem.getItemName().equalsIgnoreCase(equipmentName)) {
                        rented += rentedItem.getQuantity();
                    }
                }
            }
        }
        return Math.max(0, item.getTotalStock() - rented);
    }

    private RentalItem findRentalItemByName(String equipmentName) {
        for (RentalItem item : rentalItems) {
            if (item.getName().equalsIgnoreCase(equipmentName)) {
                return item;
            }
        }
        return null;
    }

    public int getRentedItemCount(RentalItem item) {
        int highestConcurrentRentals = 0;
        Set<String> reservationDates = new HashSet<>();
        for (Reservation reservation : reservations) {
            reservationDates.add(reservation.getReservationDate());
        }
        for (String date : reservationDates) {
            for (String timeSlot : availableTimeSlots) {
                int rented = 0;
                for (Reservation reservation : reservations) {
                    if (reservation.isActive()
                            && reservation.getReservationDate().equals(date)
                            && reservation.getTimeSlot().equals(timeSlot)) {
                        if ("Pickleball Paddle".equalsIgnoreCase(item.getName())) {
                            rented += reservation.getPaddleQuantity();
                        } else if ("Pickleball Ball".equalsIgnoreCase(item.getName())) {
                            rented += reservation.getBallQuantity();
                        }
                        for (RentalRecord rentedItem : reservation.getRentedItems()) {
                            if (rentedItem.getItemName().equalsIgnoreCase(item.getName())) {
                                rented += rentedItem.getQuantity();
                            }
                        }
                    }
                }
                highestConcurrentRentals = Math.max(highestConcurrentRentals, rented);
            }
        }
        return highestConcurrentRentals;
    }

    public boolean isStandardRentalItem(RentalItem item) {
        return "Pickleball Paddle".equalsIgnoreCase(item.getName())
                || "Pickleball Ball".equalsIgnoreCase(item.getName());
    }

    public boolean isReservationDateBlocked(String date) {
        return unavailableDates.contains(date);
    }

    public boolean isCourtReservationBlocked(String courtName, String date) {
        return courtDateBlockReasons.containsKey(getCourtDateKey(courtName, date));
    }

    public boolean isCourtReservationBlocked(String courtName, String date, String timeSlot) {
        return getReservationBlockReason(courtName, date, timeSlot) != null;
    }

    public boolean isAdminBlockTargetBlocked(String courtName, String date, String timeSlot) {
        if ("All Day".equals(timeSlot)) {
            return "All Courts".equals(courtName)
                    ? isReservationDateBlocked(date)
                    : isCourtReservationBlocked(courtName, date);
        }
        return courtTimeBlockReasons.containsKey(getCourtTimeKey(courtName, date, timeSlot));
    }

    public String getCourtDateKey(String courtName, String date) {
        return courtName + "|" + date;
    }

    public String getCourtTimeKey(String courtName, String date, String timeSlot) {
        return courtName + "|" + date + "|" + timeSlot;
    }

    public String getReservationBlockReason(String courtName, String date) {
        String dateReason = dateBlockReasons.get(date);
        if (dateReason != null) {
            return dateReason;
        }
        return courtDateBlockReasons.get(getCourtDateKey(courtName, date));
    }

    public String getReservationBlockReason(String courtName, String date, String timeSlot) {
        String dateReason = getReservationBlockReason(courtName, date);
        if (dateReason != null) {
            return dateReason;
        }
        String allCourtTimeReason = courtTimeBlockReasons.get(
                getCourtTimeKey("All Courts", date, timeSlot));
        if (allCourtTimeReason != null) {
            return allCourtTimeReason;
        }
        return courtTimeBlockReasons.get(getCourtTimeKey(courtName, date, timeSlot));
    }

    public LocalDate getLastBookingDate() {
        return openBookingMonths.isEmpty()
                ? FIRST_BOOKING_DATE : openBookingMonths.last().atEndOfMonth();
    }

    public LocalDate getFirstBookingDate() {
        return openBookingMonths.isEmpty()
                ? FIRST_BOOKING_DATE : openBookingMonths.first().atDay(1);
    }

    public boolean isBookingMonthOpen(String date) {
        return openBookingMonths.contains(YearMonth.from(LocalDate.parse(date)));
    }

    public String getReservationRentalSummary(Reservation reservation) {
        ArrayList<String> itemNames = new ArrayList<>();
        if (reservation.getPaddleQuantity() > 0) {
            itemNames.add("Paddles x" + reservation.getPaddleQuantity());
        }
        if (reservation.getBallQuantity() > 0) {
            itemNames.add("Balls x" + reservation.getBallQuantity());
        }
        for (RentalRecord item : reservation.getRentedItems()) {
            itemNames.add(item.getItemName() + " x" + item.getQuantity());
        }
        if (itemNames.isEmpty()) {
            return "None";
        }
        return String.join(", ", itemNames);
    }

    public Court findCourtByName(String courtName) {
        for (Court court : courts) {
            if (court.getCourtName().equalsIgnoreCase(courtName)) {
                return court;
            }
        }
        return null;
    }

    public String[] getCourtNames() {
        String[] names = new String[courts.size()];
        for (int i = 0; i < courts.size(); i++) {
            names[i] = courts.get(i).getCourtName();
        }
        return names;
    }

    public boolean isCourtBooked(String courtName, String date, String timeSlot) {
        return reservationService.isCourtBooked(courtName, date, timeSlot, reservations);
    }

    public String getSlotStatus(String courtName, String date, String timeSlot) {
        for (Reservation reservation : reservations) {
            if (reservation.isActive()
                    && courtName.equalsIgnoreCase(reservation.getCourtName())
                    && date.equals(reservation.getReservationDate())
                    && timeSlot.equalsIgnoreCase(reservation.getTimeSlot())) {
                return "Pending".equals(reservation.getStatus()) ? "Pending" : "Booked";
            }
        }
        return "Available";
    }

    public double getCourtRate(String courtName, String timeSlot) {
        Court court = findCourtByName(courtName);
        if (court == null) {
            return reservationService.isPeakHour(timeSlot) ? peakHourlyRate : regularHourlyRate;
        }
        return reservationService.calculateCourtRate(court, timeSlot);
    }

    public boolean isValidNonNegativeAmount(double amount) {
        return !Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0;
    }

    public double getTotalRevenue() {
        double totalRevenue = 0.0;
        for (Reservation reservation : reservations) {
            if (reservation.isActive()) {
                totalRevenue += reservation.getTotalPrice();
            }
        }
        for (Reservation reservation : completedReservations) {
            if (reservation.isActive()) {
                totalRevenue += reservation.getTotalPrice();
            }
        }
        return totalRevenue;
    }

    public int getExpectedGuestCount() {
        int guestCount = 0;
        Set<String> countedBookingReferences = new HashSet<>();
        for (Reservation reservation : reservations) {
            if (reservation.isActive()) {
                String bookingReference = reservation.getBookingReference();
                if (bookingReference == null || bookingReference.isEmpty()
                        || countedBookingReferences.add(bookingReference)) {
                    guestCount += reservation.getNumberOfPlayers();
                }
            }
        }
        return guestCount;
    }

    public int getActivePlayersOnsite() {
        int playerCount = 0;
        Set<String> countedBookingReferences = new HashSet<>();
        for (Reservation reservation : reservations) {
            if (!reservation.isActive() || reservation.getPlayersOnsite() <= 0) {
                continue;
            }
            String bookingReference = reservation.getBookingReference();
            if (bookingReference == null || bookingReference.isEmpty()
                    || countedBookingReferences.add(bookingReference)) {
                playerCount += reservation.getPlayersOnsite();
            }
        }
        return playerCount;
    }

    public int getActiveBookingCount(String source) {
        int bookingCount = 0;
        Set<String> countedBookingReferences = new HashSet<>();
        for (Reservation reservation : reservations) {
            if (reservation.isActive()
                    && source.equalsIgnoreCase(reservation.getReservationSource())) {
                String bookingReference = reservation.getBookingReference();
                if (bookingReference == null || bookingReference.isEmpty()
                        || countedBookingReferences.add(bookingReference)) {
                    bookingCount++;
                }
            }
        }
        return bookingCount;
    }
}
