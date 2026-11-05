package pickleballsystem;

import pickleballsystem.dsa.SortUtil;
import pickleballsystem.dsa.SinglyLinkedList;
import pickleballsystem.dsa.SearchUtil;
import pickleballsystem.dsa.CustomStack;
import pickleballsystem.dsa.CustomQueue;
import pickleballsystem.service.FileStorageService;
import pickleballsystem.service.SystemData;
import pickleballsystem.service.AuthService;
import pickleballsystem.model.WaitingListEntry;
import pickleballsystem.model.Reservation;
import pickleballsystem.model.Staff;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DSATests {
    public static void main(String[] args) throws IOException {
        testArrayList();
        testLinkedList();
        testStack();
        testQueue();
        testMergeSort();
        testLinearSearch();
        testWaitingListPromotion();
        testAuthService();
        testReservationStatus();
        testStaffAccountPersistence();
        System.out.println("All project tests passed.");
    }

    private static void testArrayList() {
        ArrayList<String> items = new ArrayList<>();
        items.add("Court 1");
        items.add("Court 2");
        items.add("Court 3");
        assertTrue(items.contains("Court 2"), "ArrayList should contain the added court");
        items.remove("Court 2");
        assertTrue(!items.contains("Court 2"), "ArrayList should remove the existing court");
        assertEquals(2, items.size(), "ArrayList size should be updated after removal");
    }

    private static void testLinkedList() {
        SinglyLinkedList<Integer> list = new SinglyLinkedList<>();
        assertTrue(list.isEmpty(), "Fresh linked list should be empty");
        list.addLast(10);
        list.addLast(20);
        list.addFirst(5);
        assertEquals(3, list.size(), "Linked list size should count each node");
        assertTrue(list.contains(20), "Linked list should find the value in the middle");
        list.remove(10);
        assertTrue(!list.contains(10), "Linked list should remove the middle node");
        list.removeFirst();
        assertEquals(1, list.size(), "Linked list should shrink after removing a node");
        list.removeLast();
        assertTrue(list.isEmpty(), "Linked list should be empty after removing the last node");
    }


    private static Reservation reservationOn(String name, String date, String reference) {
        Reservation reservation = new Reservation(name, "Court 1", date, "8:00 AM - 9:00 AM", 2, 300.0);
        reservation.setBookingReference(reference);
        return reservation;
    }

    private static void testMergeSort() {
        ArrayList<Reservation> list = new ArrayList<>();
        list.add(reservationOn("C", "2026-10-09", "R3"));
        list.add(reservationOn("A", "2026-10-02", "R1"));
        list.add(reservationOn("B", "2026-10-02", "R2"));
        list.add(reservationOn("D", "2026-10-01", "R4"));
        SortUtil.mergeSortByDate(list);
        assertEquals("D", list.get(0).getCustomerName(), "Merge sort should put the oldest date first");
        assertEquals("A", list.get(1).getCustomerName(), "Merge sort should keep equal dates in original order (A before B)");
        assertEquals("B", list.get(2).getCustomerName(), "Merge sort should keep equal dates in original order (A before B)");
        assertEquals("C", list.get(3).getCustomerName(), "Merge sort should put the newest date last");
    }

    private static void testLinearSearch() {
        ArrayList<Reservation> list = new ArrayList<>();
        list.add(reservationOn("A", "2026-10-02", "BK-1"));
        list.add(reservationOn("B", "2026-10-02", "BK-2"));
        list.add(reservationOn("A", "2026-10-02", "BK-1"));
        assertEquals(2, SearchUtil.findSameBooking(list, list.get(0)).size(), "Search should find both slots of booking BK-1");
        assertEquals(1, SearchUtil.findSameBooking(list, list.get(1)).size(), "Search should find only the single slot of BK-2");
    }

    private static void testWaitingListPromotion() {
        SystemData data = new SystemData();
        data.addWaitingListEntry("First", "Court 1", "2026-10-05", "8:00 AM - 9:00 AM");
        data.addWaitingListEntry("Other", "Court 2", "2026-10-05", "8:00 AM - 9:00 AM");
        data.addWaitingListEntry("Second", "Court 1", "2026-10-05", "8:00 AM - 9:00 AM");
        WaitingListEntry promoted = data.promoteWaitingList("Court 1", "2026-10-05", "8:00 AM - 9:00 AM");
        assertEquals("First", promoted.getCustomerName(), "Queue should promote the earliest matching customer");
        promoted = data.promoteWaitingList("Court 1", "2026-10-05", "8:00 AM - 9:00 AM");
        assertEquals("Second", promoted.getCustomerName(), "Queue should then promote the next matching customer");
        assertTrue(data.promoteWaitingList("Court 3", "2026-10-05", "8:00 AM - 9:00 AM") == null,
                "No customer should be promoted for a slot nobody waits for");
    }

    private static void testStack() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");
        assertEquals("C", stack.peek(), "Peek should return the last pushed item");
        assertEquals("C", stack.pop(), "Pop should return last-in-first-out item");
        assertEquals("B", stack.peek(), "Stack should maintain LIFO order");
        stack.pop();
        stack.pop();
        assertTrue(stack.isEmpty(), "Stack should be empty after draining it");
        stack.push(null);
        assertEquals(null, stack.peek(), "Stack should preserve a null item");
        assertEquals(null, stack.pop(), "Pop should return a null item");
        assertTrue(stack.isEmpty(), "Stack should remove a null item");
    }

    private static void testQueue() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("first");
        queue.enqueue("second");
        assertEquals("first", queue.peek(), "Queue should keep first item at the front");
        assertEquals("first", queue.dequeue(), "Queue should return FIFO item");
        assertEquals("second", queue.peek(), "Queue should advance to next item");
        queue.dequeue();
        assertTrue(queue.isEmpty(), "Queue should be empty after dequeueing all items");
        queue.enqueue(null);
        assertEquals(null, queue.peek(), "Queue should preserve a null item");
        assertEquals(null, queue.dequeue(), "Dequeue should return a null item");
        assertTrue(queue.isEmpty(), "Queue should remove a null item");
    }

    private static void testAuthService() {
        AuthService authService = new AuthService();
        assertTrue(authService.registerCustomer("guest", "guest123"), "Customer registration should succeed");
        assertTrue(!authService.registerCustomer("guest", "different123"), "Duplicate usernames should be rejected");
        assertEquals("Customer", authService.authenticate("guest", "guest123"), "Customer login should authenticate");
        assertEquals("INVALID", authService.authenticate("guest", "wrong-password-1"), "Invalid credentials should be rejected");
        assertEquals("INVALID", authService.authenticate("guest", "wrong-password-2"), "Second invalid attempt should still be tracked");
        assertEquals("LOCKED", authService.authenticate("guest", "wrong-password-3"), "Third invalid attempt should lock the account");
        assertTrue(authService.unlockAccount("guest"), "Unlock should succeed for a locked account");
        assertTrue(authService.changePassword("guest", "guest123", "newPass123", "newPass123"), "Password change should validate the current password");
        assertEquals("Customer", authService.authenticate("guest", "newPass123"), "Customer should login with the updated password");
    }

    private static void testReservationStatus() {
        Reservation reservation = new Reservation();
        assertEquals("Pending", reservation.getStatus(), "New reservations should wait for admin approval");
        reservation.setStatus("Confirmed");
        assertEquals("Confirmed", reservation.getStatus(), "Admin approval should update reservation status");
    }

    private static void testStaffAccountPersistence() throws IOException {
        Path dataDirectory = Files.createTempDirectory("pickleball-staff-test");
        Path accountsFile = dataDirectory.resolve("accounts.txt");
        Path bookingMonthFile = dataDirectory.resolve("booking-month.txt");
        Path openMonthsFile = dataDirectory.resolve("open-booking-months.txt");
        try {
            FileStorageService storage = new FileStorageService(dataDirectory);
            Set<YearMonth> defaultMonths = new HashSet<>();
            defaultMonths.add(YearMonth.of(2026, 10));
            defaultMonths.add(YearMonth.of(2026, 11));
            assertEquals(defaultMonths, storage.loadOpenBookingMonths(defaultMonths),
                    "October and November should be open by default");

            Set<YearMonth> openMonths = new HashSet<>(defaultMonths);
            openMonths.add(YearMonth.of(2026, 12));
            openMonths.add(YearMonth.of(2027, 1));
            openMonths.remove(YearMonth.of(2026, 11));
            storage.saveOpenBookingMonths(openMonths);
            assertEquals(openMonths, storage.loadOpenBookingMonths(defaultMonths),
                    "Open and closed month selections should persist");

            storage.saveOpenBookingMonths(new HashSet<>());
            assertTrue(storage.loadOpenBookingMonths(defaultMonths).isEmpty(),
                    "An empty saved month list should keep all months closed");

            Files.delete(openMonthsFile);
            Files.writeString(bookingMonthFile, "2027-01", StandardCharsets.UTF_8);
            Set<YearMonth> oldSettings = storage.loadOpenBookingMonths(defaultMonths);
            assertTrue(oldSettings.contains(YearMonth.of(2026, 10))
                    && oldSettings.contains(YearMonth.of(2026, 11))
                    && oldSettings.contains(YearMonth.of(2027, 1)),
                    "Previous last-month settings should continue to load");
            Files.write(accountsFile, List.of("legacy|legacy123"), StandardCharsets.UTF_8);
            assertEquals("legacy123", storage.loadAccounts().get("legacy"),
                    "Accounts saved by the previous two-column format should still load");

            Map<String, String> passwords = new HashMap<>();
            passwords.put("admin", "admin123");
            passwords.put("coach", "coach123");
            Map<String, Staff> staffAccounts = new HashMap<>();
            Staff coach = new Staff("STF-001", "Coach Lee");
            coach.setActive(false);
            staffAccounts.put("coach", coach);

            storage.saveAccounts(passwords, staffAccounts);

            Map<String, String> loadedPasswords = storage.loadAccounts();
            Map<String, Staff> loadedStaff = storage.loadStaffDetails();
            assertEquals("coach123", loadedPasswords.get("coach"),
                    "Staff passwords should remain readable when details are saved");
            assertEquals("admin123", loadedPasswords.get("admin"),
                    "Admin password should remain readable when staff details are saved");
            assertEquals("Coach Lee", loadedStaff.get("coach").getName(),
                    "Staff names should persist");
            assertTrue(!loadedStaff.get("coach").isActive(),
                    "Inactive status should persist");

            loadedStaff.get("coach").setActive(true);
            storage.saveAccounts(passwords, loadedStaff);
            assertTrue(storage.loadStaffDetails().get("coach").isActive(),
                    "Reactivated status should persist");

            passwords.remove("coach");
            loadedStaff.clear();
            storage.saveAccounts(passwords, loadedStaff);
            assertTrue(!storage.loadAccounts().containsKey("coach"),
                    "Deleted staff accounts should not return after restart");
        } finally {
            Files.deleteIfExists(accountsFile);
            Files.deleteIfExists(bookingMonthFile);
            Files.deleteIfExists(openMonthsFile);
            Files.deleteIfExists(dataDirectory);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + " Actual: " + actual);
        }
    }
}
