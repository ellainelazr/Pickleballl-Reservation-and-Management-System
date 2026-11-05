
package pickleballsystem.ui;

import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import pickleballsystem.PickleballMain;
import pickleballsystem.model.RentalItem;

import static pickleballsystem.ui.UiHelper.*;

public class AdminEquipmentScreen extends BaseScreen {

    public AdminEquipmentScreen(PickleballMain app) {
        super(app);
    }

    public void showAdminEquipmentScreen() {
        JFrame equipmentFrame = new JFrame(
                "SmashPoint Pickleball - Equipment Management");

        equipmentFrame.setSize(1320, 780);
        equipmentFrame.setMinimumSize(new Dimension(1050, 650));
        equipmentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        equipmentFrame.setLocationRelativeTo(null);

        JPanel page = new JPanel(new BorderLayout());
        page.add(app.createAdminSidebar(
                equipmentFrame, "Equipment Management"), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(INTRO_MINT);
        content.setBorder(
                BorderFactory.createEmptyBorder(28, 32, 24, 32));

        String[] columns = {
            "Equipment", "Total Stock", "Rented",
            "Available", "Price / Hour"
        };

        DefaultTableModel model = readOnlyTableModel(columns);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(INTRO_MINT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setBackground(INTRO_MINT);

        JLabel title = new JLabel("Equipment Management");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(INTRO_TEAL);

        JLabel subtitle = new JLabel(
                "Manage rental equipment, stock, and hourly prices.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(INTRO_MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);
        titlePanel.add(heading, BorderLayout.WEST);

        JTable equipmentTable = new JTable(model);
        styleAdminTable(equipmentTable);
        refreshEquipmentTable(model);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(
                BorderFactory.createLineBorder(new Color(215, 232, 229)));
        tablePanel.add(new JScrollPane(equipmentTable), BorderLayout.CENTER);

        JButton editButton = createAdminActionButton("Update Quantity");
        editButton.addActionListener(e -> {
            int row = equipmentTable.getSelectedRow();

            if (row < 0) {
                JOptionPane.showMessageDialog(
                        equipmentFrame,
                        "Select an equipment item first.",
                        "Selection Required",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int modelRow = equipmentTable.convertRowIndexToModel(row);
            updateEquipmentQuantity(
                    equipmentFrame, data.getRentalItems().get(modelRow));
            refreshEquipmentTable(model);
        });

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setBackground(INTRO_MINT);
        buttonPanel.add(editButton);

        content.add(titlePanel, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        page.add(content, BorderLayout.CENTER);

        equipmentFrame.add(
                app.createHeaderPanel(
                        "Equipment Management", equipmentFrame, true),
                BorderLayout.NORTH);
        equipmentFrame.add(page, BorderLayout.CENTER);
        equipmentFrame.setVisible(true);
    }

    private void refreshEquipmentTable(DefaultTableModel model) {
        if (model == null) {
            return;
        }

        model.setRowCount(0);

        for (RentalItem item : data.getRentalItems()) {
            int rented = data.getRentedItemCount(item);

            model.addRow(new Object[]{
                item.getName(),
                item.getTotalStock(),
                rented,
                Math.max(0, item.getTotalStock() - rented),
                "₱" + String.format("%.2f", item.getHourlyRate())
            });
        }
    }

    private void updateEquipmentQuantity(JFrame parent, RentalItem item) {
        JTextField stockField = createIntegerOnlyField(10);
        stockField.setText(String.valueOf(item.getTotalStock()));

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Equipment:"));
        panel.add(new JLabel(item.getName()));
        panel.add(new JLabel("Total quantity:"));
        panel.add(stockField);

        int answer = JOptionPane.showConfirmDialog(
                parent, panel, "Update Inventory Quantity",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int quantity = Integer.parseInt(stockField.getText().trim());
            if (quantity < 0 || quantity < data.getRentedItemCount(item)) {
                JOptionPane.showMessageDialog(parent,
                        "Quantity cannot be negative or less than the number currently rented.",
                        "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int oldQuantity = item.getTotalStock();
            item.setTotalStock(quantity);
            try {
                data.saveEquipment();
                JOptionPane.showMessageDialog(parent, "Inventory quantity updated.",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                item.setTotalStock(oldQuantity);
                JOptionPane.showMessageDialog(parent, "Could not save the new quantity.\n"
                        + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Enter a whole-number quantity.",
                    "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
        }
    }
}
