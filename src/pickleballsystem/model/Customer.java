package pickleballsystem.model;

import pickleballsystem.model.User;

public class Customer extends User {

    public Customer() {
        super();
    }

    public Customer(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Customer";
    }

    @Override
    public String toString() {
        return getName();
    }
}
