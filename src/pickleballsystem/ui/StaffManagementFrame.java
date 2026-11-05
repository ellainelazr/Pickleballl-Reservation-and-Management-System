package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.Staff;

public class StaffManagementFrame extends JFrame {
    private static final Color TEAL = new Color(0, 150, 136);
    private static final Color INTRO_MINT = new Color(234, 247, 245);
    private static final Color INTRO_TEAL = new Color(0, 91, 94);
    private static final Color WHITE = Color.WHITE;
    private final PickleballMain application;

    public StaffManagementFrame(PickleballMain application) {
        this.application = application;
        setTitle("SmashPoint Pickleball - Staff Management");
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildScreen();
    }

    private void buildScreen() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(INTRO_MINT);
        content.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));

        JLabel heading = new JLabel("Staff Management");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(INTRO_TEAL);
        content.add(heading, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Staff ID", "Staff Name", "Username", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable staffTable = new JTable(model);
        staffTable.setRowHeight(42);
        staffTable.setFont(new Font("Arial", Font.PLAIN, 14));
        staffTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        staffTable.getTableHeader().setBackground(TEAL);
        staffTable.getTableHeader().setForeground(WHITE);
        refreshStaffTable(model);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 232)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        tableCard.add(new JScrollPane(staffTable), BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        JButton addButton = createButton("+  Add Staff", TEAL);
        addButton.addActionListener(event -> {
            if (application.addStaffFromAdmin(this)) {
                refreshStaffTable(model);
            }
        });

        JButton editButton = createButton("Edit Staff", new Color(40, 125, 190));
        editButton.addActionListener(event -> {
            int row = staffTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a staff member first.",
                        "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (application.editStaffFromAdmin(this, application.getAccounts().getStaffAt(row))) {
                refreshStaffTable(model);
            }
        });

        JButton deleteButton = createButton("Delete Staff", new Color(244, 67, 54));
        deleteButton.addActionListener(event -> {
            int row = staffTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a staff member first.",
                        "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (application.deleteStaffFromAdmin(this, application.getAccounts().getStaffAt(row))) {
                refreshStaffTable(model);
            }
        });

        JButton backButton = createButton("Back to Dashboard", new Color(84, 101, 119));
        backButton.addActionListener(event -> {
            dispose();
            application.showAdminDashboard();
        });

        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setBackground(WHITE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 232)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(backButton);
        content.add(buttons, BorderLayout.SOUTH);

        JPanel page = new JPanel(new BorderLayout());
        page.add(application.createAdminSidebar(this, "Staff Management"), BorderLayout.WEST);
        page.add(content, BorderLayout.CENTER);
        add(application.createHeaderPanel("Staff Management", this, true), BorderLayout.NORTH);
        add(page, BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void refreshStaffTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (int index = 0; index < application.getAccounts().getStaffCount(); index++) {
            Staff staff = application.getAccounts().getStaffAt(index);
            model.addRow(new Object[]{
                staff.getStaffId(),
                staff.getName(),
                application.getAccounts().getStaffUsername(staff),
                staff.isActive() ? "Active" : "Inactive"
            });
        }
    }
}
