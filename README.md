# SmashPoint Pickleball Reservation System

**One system to book every court, rent every paddle, and run the whole venue.**

Double bookings, lost slots, handwritten receipts and last-minute court closures cost a pickleball venue both revenue and goodwill. SmashPoint puts the entire operation in a single desktop application: customers reserve courts in seconds, staff check players in at the door, and admins keep full control over pricing, availability and equipment.

Built with Java 17 and Swing. No database and no installation beyond the JDK. Everything is stored in simple text files.

---

## Why SmashPoint

| For the customer | For the staff | For the admin |
|------------------|---------------|---------------|
| See live availability for all 9 courts | Create onsite bookings for walk-ins | Block courts, dates and time slots with a recorded reason |
| Book several hourly slots in one transaction | Check players in against the expected headcount | Set regular and peak pricing |
| Add paddle and ball rentals | Work from a focused check-in screen | Manage equipment stock, staff accounts and booking months |
| Receive a receipt and booking reference instantly | | Confirm, cancel and reschedule reservations, and review full booking history |

---

## How It Works

1. A customer (online) or a staff member (onsite) selects a **date, court and one or more hourly slots**.
2. Optional **equipment rentals** are added, with stock checked per time slot.
3. The system calculates the total using **regular or peak rates** and issues a unique **booking reference**, such as `BK-20261028-001`.
4. A **receipt** is generated and saved automatically.
5. The admin **confirms** the booking, staff **check in** the players on arrival, and the booking is marked **Completed**.

### User Roles

| Role | Capabilities |
|------|--------------|
| **Customer / Guest** | Browse availability, book multiple slots, rent equipment, choose a payment method (Cash, GCash, QRPh), receive a receipt |
| **Staff** | Make onsite bookings and record players present versus players expected |
| **Admin** | Full venue control: courts, blocking, pricing and expenses, equipment, staff accounts, reservations, booking history and account unlocks |

### Feature Highlights

- Live availability grid showing Available, Pending, Booked and Blocked slots
- Multi-slot booking with automatic booking references
- Peak pricing from 1:00 PM to 7:00 PM and regular pricing otherwise
- Equipment rental with per-slot stock validation
- Automatic receipt generation
- Role-based login with a three-attempt lockout and password change
- Activity log of logins, bookings and cancellations, newest first
- Admin dashboard showing revenue, expected guests, players onsite and booking counts
- File-based persistence in the `data/` and `receipts/` folders

---

## Court Blocking

Maintenance, tournaments, private events and holidays should never turn into double bookings. Admins can close any part of the schedule from **Court Management**, and the system enforces it everywhere a reservation can be made.

### Three levels of control

| Block target | Court selection | Time selection | Result |
|--------------|-----------------|----------------|--------|
| Entire venue for a day | All Courts | All Day | No court can be booked on that date |
| One court for a day | A specific court | All Day | That court is closed for the full day |
| One hour | A specific court or All Courts | A specific slot, e.g. 4:00 PM - 5:00 PM | Only that slot is closed |

### Behavior

1. The admin selects a date, a court (or All Courts) and a time slot (or All Day).
2. A single button toggles between **Block** and **Allow**, depending on the current state.
3. A **reason is required** before anything can be blocked.
4. Blocked cells appear immediately as **Blocked** in the availability grid.
5. Any attempt to book a blocked slot is refused, and the reason is shown to the user.
6. The same rules apply when an admin **reschedules** an existing reservation.

### Rule precedence

When several blocks overlap, the first match below is applied and its reason is displayed.

```
1. Whole-day block for all courts
2. Whole-day block for the selected court
3. Time-slot block for all courts
4. Time-slot block for the selected court
```

### Implementation

- `SystemData` holds the rules: `isReservationDateBlocked`, `isCourtReservationBlocked`, `isAdminBlockTargetBlocked` and `getReservationBlockReason`.
- `AdminCourtScreen` provides the block and allow interface and the reason prompt.
- `CourtReservationScreen`, `BookingConfirmationDialog` and `AdminReservationsScreen` enforce the rules during booking and rescheduling.
- Blocks are stored in three `Map<String, String>` structures keyed by `date`, `court|date` and `court|date|timeSlot`, with the admin's reason as the value.

---

## Object-Oriented Design

### Encapsulation
Model classes keep their fields private and expose them through getters and setters (`Reservation`, `Court`, `RentalItem`, `User`). Several classes are immutable, using `final` fields and no setters: `RentalRecord`, `ReservationSlot` and `ReservationActivityEntry`.

```java
public class Court {
    private String courtName;
    private double regularRatePerHour;
    private double peakRatePerHour;
    private boolean available;
}
```

### Abstraction
- `User` is abstract and declares `getRole()`, which every subclass must implement.
- `BaseScreen` is abstract and provides every screen with shared access to the application, system data and account manager.

```java
public abstract class User {
    private String name;
    public abstract String getRole();
}
```

### Inheritance
```
User (abstract)
 |-- Admin     getRole() returns "Admin"
 |-- Customer  getRole() returns "Customer"
 `-- Staff     getRole() returns "Staff" (adds staffId and active status)

BaseScreen (abstract)
 |-- LoginScreen, IntroScreen, CustomerDashboardScreen
 |-- CourtReservationScreen, BookingConfirmationDialog, PaymentDialog
 |-- GuestBookingDialog, EquipmentRentalDialog, ReservationSummaryDialog
 |-- StaffOnsiteScreen, ReceiptWriter, BookingCalendar
 `-- AdminDashboardScreen, AdminCourtScreen, AdminReservationsScreen,
     AdminBookingHistoryScreen, AdminEquipmentScreen,
     AdminPricingScreen, AdminStaffScreen
```
`PickleballMain` extends `JFrame`, and the table renderers and editors extend Swing's `DefaultTableCellRenderer`, `JButton` and `AbstractCellEditor`.

### Polymorphism

**Overriding.** Each `User` subclass overrides `getRole()` and `toString()`.

**Overloading.**

| Class | Overloaded members |
|-------|--------------------|
| `Court` | Four constructors with increasing detail (name and rate, regular and peak rates, availability, full) |
| `Reservation` | `calculateTotalPrice(double ratePerHour)` and `calculateTotalPrice(Court court)` |
| `SystemData` | `isCourtReservationBlocked` and `getReservationBlockReason`, each with and without a time slot; `getAvailableEquipmentCount` by type or by name |

### Composition
- `SystemData` is composed of courts, reservations, rental items, the activity history and a `FileStorageService`.
- `Reservation` contains a list of `RentalRecord` objects.
- `ReservationHistory` contains a `SinglyLinkedList`.
- `PickleballMain` contains every screen and dialog and delegates to them.

### Generics
`SinglyLinkedList<T>` is a type-safe, reusable structure. `ReservationHistory` uses it as `SinglyLinkedList<ReservationActivityEntry>`.

### Separation of Concerns
```
src/pickleballsystem/
|-- PickleballMain.java     Application entry point and screen coordinator
|-- model/                  Data classes: User, Reservation, Court, RentalItem
|-- service/                Business logic: SystemData, ReservationService, AuthService,
|                           AccountManager, FileStorageService, ReceiptWriter
|-- ui/                     Swing screens and dialogs
|-- dsa/                    Custom data structures and algorithms
`-- images/                 Court, paddle and ball artwork
```

### Additional Techniques
- **Nested classes:** the private static `Node<T>` inside `SinglyLinkedList` and the private `AccountRecord` inside `AuthService`
- **Utility classes:** `final` classes with private constructors (`SortUtil`, `SearchUtil`, `UiHelper`)
- **Single responsibility:** for example, `ReservationService` is limited to slot checks and peak-hour pricing

---

## Data Structures and Algorithms

| Concept | Class | Purpose |
|---------|-------|---------|
| Singly linked list | `SinglyLinkedList` | Core storage structure |
| Linked list as a log | `ReservationHistory` | Activity log with the newest entry first |
| Merge sort (stable) | `SortUtil.mergeSortByDate` | Sorts booking history by date |
| Linear search | `SearchUtil.findSameBooking` | Finds every slot belonging to one booking reference |
| HashMap, HashSet, TreeSet | `SystemData`, `AuthService` | Block reasons, accounts, locked users and open booking months |

---

## Project Structure

```
PickleballSystem_Fixeddd/
|-- src/pickleballsystem/        Source code
|-- data/
|   |-- accounts.txt             Login accounts
|   |-- equipment.txt            Rental items and stock
|   |-- open-booking-months.txt  Months open for booking
|   |-- reservations.txt         All reservations
|   `-- system-settings.txt      Rates and expenses
|-- receipts/                    Generated booking receipts
|-- nbproject/                   NetBeans project configuration
|-- build.xml
`-- manifest.mf
```

---

## Getting Started

**Requirement:** JDK 17 or newer

**NetBeans**
1. Choose *File, Open Project* and select `PickleballSystem_Fixeddd`.
2. Right-click the project and choose **Run**. The main class is `pickleballsystem.PickleballMain`.

**Command line**
```bash
cd PickleballSystem_Fixeddd
javac -d build/classes -sourcepath src src/pickleballsystem/PickleballMain.java
cp -r src/pickleballsystem/images build/classes/pickleballsystem/
java -cp build/classes pickleballsystem.PickleballMain
```
Run from the project root so the `data/` and `receipts/` folders are found.


