package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import pickleballsystem.ui.StaffManagementFrame;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.Staff;

/** Admin actions for adding, editing, and deleting staff accounts. */
public class AdminStaffScreen extends BaseScreen {

    public AdminStaffScreen(PickleballMain app) {
        super(app);
    }

    public void showAdminStaffScreen() {
        new StaffManagementFrame(app).setVisible(true);
    }

    public boolean addStaffFromAdmin(JFrame parent) {
        JTextField nameField = new JTextField(18);
        JTextField usernameField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);
        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 8));
        fields.add(new JLabel("Staff name:"));
        fields.add(nameField);
        fields.add(new JLabel("Username:"));
        fields.add(usernameField);
        fields.add(new JLabel("Password:"));
        fields.add(passwordField);

        int answer = JOptionPane.showConfirmDialog(parent, fields, "Add Staff Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return false;
        }

        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String usernameKey = username.toLowerCase(Locale.ROOT);
        if (name.isEmpty() || username.isEmpty() || password.isEmpty()
                || usernameKey.equals(accounts.getAdminUsername().toLowerCase(Locale.ROOT))
                || accounts.getStaffAccounts().containsKey(usernameKey)) {
            JOptionPane.showMessageDialog(parent,
                    "Enter a name, a unique username, and a password. The admin username is reserved.",
                    "Invalid Staff Details", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Staff staff = new Staff(accounts.createStaffId(), name);
        accounts.getStaffMembers().add(staff);
        accounts.getStaffAccounts().put(usernameKey, staff);
        accounts.getStaffPasswords().put(usernameKey, password);
        if (!accounts.persistAccountPasswords()) {
            accounts.getStaffMembers().remove(staff);
            accounts.getStaffAccounts().remove(usernameKey);
            accounts.getStaffPasswords().remove(usernameKey);
            return false;
        }
        return true;
    }

    public boolean editStaffFromAdmin(JFrame parent, Staff staff) {
        String oldUsername = accounts.getStaffUsername(staff);
        JTextField nameField = new JTextField(staff.getName(), 18);
        JTextField usernameField = new JTextField(oldUsername, 18);
        JPasswordField passwordField = new JPasswordField(18);
        JComboBox<String> statusPicker = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusPicker.setSelectedItem(staff.isActive() ? "Active" : "Inactive");
        JPanel fields = new JPanel(new GridLayout(4, 2, 8, 8));
        fields.add(new JLabel("Staff name:"));
        fields.add(nameField);
        fields.add(new JLabel("Username:"));
        fields.add(usernameField);
        fields.add(new JLabel("New password (leave blank to keep current):"));
        fields.add(passwordField);
        fields.add(new JLabel("Status:"));
        fields.add(statusPicker);

        int answer = JOptionPane.showConfirmDialog(parent, fields, "Edit Staff Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return false;
        }

        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String usernameKey = username.toLowerCase(Locale.ROOT);
        if (name.isEmpty() || username.isEmpty()
                || usernameKey.equals(accounts.getAdminUsername().toLowerCase(Locale.ROOT))
                || (accounts.getStaffAccounts().containsKey(usernameKey) && accounts.getStaffAccounts().get(usernameKey) != staff)) {
            JOptionPane.showMessageDialog(parent,
                    "Enter a name and a unique username. The admin username is reserved.",
                    "Invalid Staff Details", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String oldPassword = accounts.getStaffPasswords().get(oldUsername);
        String oldName = staff.getName();
        boolean oldActive = staff.isActive();
        if (!oldUsername.equals(usernameKey)) {
            String password = accounts.getStaffPasswords().remove(oldUsername);
            accounts.getStaffAccounts().remove(oldUsername);
            accounts.getStaffAccounts().put(usernameKey, staff);
            accounts.getStaffPasswords().put(usernameKey, password);
        }
        String newPassword = new String(passwordField.getPassword());
        if (!newPassword.isEmpty()) {
            accounts.getStaffPasswords().put(usernameKey, newPassword);
        }
        staff.setName(name);
        staff.setActive("Active".equals(statusPicker.getSelectedItem()));
        if (!accounts.persistAccountPasswords()) {
            if (!oldUsername.equals(usernameKey)) {
                accounts.getStaffAccounts().remove(usernameKey);
                accounts.getStaffPasswords().remove(usernameKey);
                accounts.getStaffAccounts().put(oldUsername, staff);
                accounts.getStaffPasswords().put(oldUsername, oldPassword);
            } else {
                accounts.getStaffPasswords().put(oldUsername, oldPassword);
            }
            staff.setName(oldName);
            staff.setActive(oldActive);
            return false;
        }
        return true;
    }

    
    public boolean deleteStaffFromAdmin(JFrame parent, Staff staff) {
        if (staff == null) {
            JOptionPane.showMessageDialog(parent,
                    "No staff account was selected.",
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Step 1: Confirm the deletion.
        int answer = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to delete the staff account for "
                        + staff.getName() + "?",
                "Confirm Staff Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return false;
        }

        // Step 2: Require the admin password.
        JPasswordField passwordField = new JPasswordField(18);

        int passwordAnswer = JOptionPane.showConfirmDialog(parent,
                new Object[]{
                    "Enter the administrator password to continue:",
                    passwordField
                },
                "Admin Password Verification",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (passwordAnswer != JOptionPane.OK_OPTION) {
            return false;
        }

        String enteredPassword = new String(passwordField.getPassword());

        if (!accounts.passwordMatchesStoredValue(
                enteredPassword, accounts.getAdminPassword())) {
            JOptionPane.showMessageDialog(parent,
                    "Incorrect administrator password. Staff account was not deleted.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Step 3: Delete the account only after successful verification.
        String username = accounts.getStaffUsername(staff);
        String password = accounts.getStaffPasswords().get(username);

        accounts.getStaffMembers().remove(staff);
        accounts.getStaffAccounts().remove(username);
        accounts.getStaffPasswords().remove(username);

        // Step 4: Restore the account if saving fails.
        if (!accounts.persistAccountPasswords()) {
            accounts.getStaffMembers().add(staff);
            accounts.getStaffAccounts().put(username, staff);

            if (password != null) {
                accounts.getStaffPasswords().put(username, password);
            }

            return false;
        }

        JOptionPane.showMessageDialog(parent,
                "Staff account deleted successfully.",
                "Deletion Successful",
                JOptionPane.INFORMATION_MESSAGE);

        return true;
    }

}
