# SmashPoint Pickleball System

SmashPoint is a beginner-friendly Java Swing reservation and rental management application for a pickleball venue. It supports court bookings, equipment rental, staff check-in, admin overview reporting, and local account handling.

## Features
- Court booking and multi-slot reservations
- Rental pricing for paddles, balls, and extra inventory
- Staff and admin dashboards
- Reservation history and business reporting
- Admin/staff login with no public registration
- Admin approval for customer reservation requests
- Simple local account storage for this classroom project
- Waiting list queue for full time slots

## Build and Run
1. Install JDK 17 or newer and open the project folder in NetBeans.
2. In NetBeans, choose **Clean and Build**, then **Run Project**.
3. The main class is `pickleballsystem.PickleballMain`.

The project uses standard Java and Swing and does not require a database server
or extra libraries. Court and equipment images are stored in
`src/pickleballsystem/images`.

Typical command-line build:

```powershell
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object FullName
javac --release 17 -d build\classes $sources
java -cp build\classes pickleballsystem.PickleballMain
```

## Local Data Files
The application keeps working data in Java collections while it runs and saves
accounts and reservations to text files in the `data` folder in the project root.
It does not connect to MySQL, XAMPP, WAMP, or another physical database.

- `data/accounts.txt` — stored usernames and passwords; staff accounts also save the staff name and active/inactive status
- `data/reservations.txt` — saved reservation records for restart persistence

Customer bookings start as **Pending** and hold their selected court times while awaiting review. An admin can select a pending booking on the Reservations page and press **Confirm**. This approves all court times in that booking. Staff-created onsite bookings are confirmed directly.
Online reservation requests remain **Pending** until an admin confirms them. Only **Pending** cells in the customer and admin availability grids use the light yellow background; confirmed reservations remain **Booked**. Admins can open or close specific months from the Dashboard's **Month Availability** button or the admin sidebar's **Month Availability** item. October and November 2026 are open by default, and the customer calendar only lists open months. Month settings are saved in `data/open-booking-months.txt`; a previous `data/booking-month.txt` setting is imported if present. Staff names and active/inactive status are saved when staff accounts are added or edited.

The sample login accounts are:

- Admin: `admin` / `admin123`
- Staff: `staff` / `staff123`

Passwords are stored as plain text to keep this school project easy to understand. This is not safe for a real system; do not use real or reused passwords. Accounts saved by an earlier version with hashed passwords must have their passwords reset from Admin > Staff Management > Edit Staff.

## Data Structures Used
- ArrayList: booking, court, rental, and staff collections
- SinglyLinkedList: reservation activity log and recent-status tracking
- CustomStack: a simple last-in, first-out stack example
- CustomQueue: waitlist management when a court slot is already taken

## Data Structures and Algorithms (marked with `DSA` comments)
| Requirement | Where | Used for |
|---|---|---|
| Queue | `CustomQueue`, `SystemData.promoteWaitingList` | Waiting list. Cancelling a reservation takes the first customer waiting for that slot off the queue |
| Stack | `CustomStack` | Last-in, first-out stack built on `SinglyLinkedList` (covered by `DSATests`) |
| Linked list | `SinglyLinkedList`, `ReservationHistory` | Activity log |
| Sorting | `SortUtil.mergeSortByDate` | Admin booking history, oldest date first |
| Searching | `SearchUtil.findSameBooking` | Finds every slot of one booking when confirming or checking in |

## OOP Concepts Demonstrated
- Inheritance: `Admin`, `Staff`, and `Customer` extend `User`
- Encapsulation: private model fields with controlled accessors
- Polymorphism: role-based behavior using the user hierarchy
- Modular design: UI logic is separated from domain and persistence concerns

## Known Limitations
- The project continues to use local file storage rather than a formal external database.
- GUI actions are optimized for a desktop Swing environment and are designed for an interactive local workstation.
- Further business-rule tuning can be added as the venue grows beyond the current default court inventory.
