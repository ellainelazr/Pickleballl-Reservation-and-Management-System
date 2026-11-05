package pickleballsystem.model;

import pickleballsystem.model.User;

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public String toString() {
        return getName();
    }
}
