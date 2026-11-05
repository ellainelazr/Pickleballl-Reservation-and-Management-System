package pickleballsystem.ui;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordChangePanel extends JPanel {
    private final JPasswordField currentPasswordField = new JPasswordField(18);
    private final JPasswordField newPasswordField = new JPasswordField(18);
    private final JPasswordField confirmPasswordField = new JPasswordField(18);

    public PasswordChangePanel() {
        setLayout(new GridLayout(3, 2, 10, 10));
        add(new JLabel("Current password:"));
        add(currentPasswordField);
        add(new JLabel("New password:"));
        add(newPasswordField);
        add(new JLabel("Confirm password:"));
        add(confirmPasswordField);
    }

    public String getCurrentPassword() {
        return new String(currentPasswordField.getPassword());
    }

    public String getNewPassword() {
        return new String(newPasswordField.getPassword());
    }

    public String getConfirmPassword() {
        return new String(confirmPasswordField.getPassword());
    }
}
