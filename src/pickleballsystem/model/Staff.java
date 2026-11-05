package pickleballsystem.model;

import pickleballsystem.model.User;

public class Staff extends User {
    private final String staffId;
    private boolean active;

    public Staff() {
        this("", "");
    }

    public Staff(String name) {
        this("", name);
    }

    public Staff(String staffId, String name) {
        super(name);
        this.staffId = staffId;
        this.active = true;
    }

    public String getStaffId() {
        return staffId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getRole() {
        return "Staff";
    }

    @Override
    public String toString() {
        return getName();
    }
}
