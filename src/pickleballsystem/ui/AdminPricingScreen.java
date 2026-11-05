
package pickleballsystem.ui;

import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.JTextField;
import javax.swing.DefaultCellEditor;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import pickleballsystem.PickleballMain;
import pickleballsystem.model.Court;

import static pickleballsystem.ui.UiHelper.*;

public class AdminPricingScreen extends BaseScreen {

    public AdminPricingScreen(PickleballMain app) {
        super(app);
    }

    public void showAdminPricingScreen() {
        JFrame pricingFrame = new JFrame(
                "SmashPoint Pickleball - Pricing and Expenses");

        pricingFrame.setSize(850, 600);
        pricingFrame.setLocationRelativeTo(null);
        pricingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(18, 18));
        contentPanel.setBackground(INTRO_MINT);
        contentPanel.setBorder(
                BorderFactory.createEmptyBorder(28, 32, 24, 32));

        JLabel title = new JLabel("Pricing and Expenses");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(INTRO_TEAL);

        JLabel instructions = new JLabel(
                "Select an amount in the table to update it.");
        instructions.setFont(new Font("Arial", Font.PLAIN, 14));
        instructions.setForeground(INTRO_MUTED);

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 6));
        heading.setBackground(INTRO_MINT);
        heading.add(title);
        heading.add(instructions);

        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{
                    {"Regular court rate per hour",
                        data.getRegularHourlyRate()},
                    {"Peak court rate per hour",
                        data.getPeakHourlyRate()},
                    {"Operating expenses",
                        data.getOperatingExpense()}
                },
                new String[]{"Price / Expense", "Amount (PHP)"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        JTable priceTable = new JTable(model);
        priceTable.setRowHeight(42);
        priceTable.setFont(new Font("Arial", Font.PLAIN, 14));
        priceTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 14));
        priceTable.getTableHeader().setBackground(TEAL);
        priceTable.getTableHeader().setForeground(WHITE);
        priceTable.getColumnModel().getColumn(0).setPreferredWidth(280);

        // Allow only digits and one decimal point in the Amount column.
        JTextField amountField = new JTextField();

        ((AbstractDocument) amountField.getDocument()).setDocumentFilter(
                new DocumentFilter() {
            private boolean isValidInput(String current, String inserted,
                    int offset, int length) {

                String result = current.substring(0, offset)
                        + inserted
                        + current.substring(offset + length);

                return result.matches("[0-9]*\\.?[0-9]*");
            }

            @Override
            public void insertString(FilterBypass fb, int offset,
                    String string, AttributeSet attr)
                    throws BadLocationException {

                if (string != null && isValidInput(
                        fb.getDocument().getText(0,
                                fb.getDocument().getLength()),
                        string, offset, 0)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length,
                    String text, AttributeSet attrs)
                    throws BadLocationException {

                String inserted = text == null ? "" : text;
                String current = fb.getDocument().getText(
                        0, fb.getDocument().getLength());

                if (isValidInput(current, inserted, offset, length)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        priceTable.getColumnModel().getColumn(1).setCellEditor(
                new DefaultCellEditor(amountField));

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 8));
        buttons.setBackground(INTRO_MINT);

        JButton backButton = createAdminActionButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            pricingFrame.dispose();
            app.showAdminDashboard();
        });

        JButton saveButton = createAdminActionButton("Save Changes");
        saveButton.addActionListener(e -> {
            if (priceTable.isEditing()
                    && !priceTable.getCellEditor().stopCellEditing()) {
                return;
            }

            try {
                double newRegularRate = Double.parseDouble(
                        model.getValueAt(0, 1).toString().trim());

                double newPeakRate = Double.parseDouble(
                        model.getValueAt(1, 1).toString().trim());

                double newOperatingExpense = Double.parseDouble(
                        model.getValueAt(2, 1).toString().trim());

                if (!data.isValidNonNegativeAmount(newRegularRate)
                        || !data.isValidNonNegativeAmount(newPeakRate)
                        || !data.isValidNonNegativeAmount(
                                newOperatingExpense)) {
                    throw new NumberFormatException();
                }

                data.setRegularHourlyRate(newRegularRate);
                data.setPeakHourlyRate(newPeakRate);
                data.setOperatingExpense(newOperatingExpense);

                for (Court court : data.getCourts()) {
                    court.setRegularRatePerHour(
                            data.getRegularHourlyRate());
                    court.setPeakRatePerHour(
                            data.getPeakHourlyRate());
                }

                try {
                    data.saveSystemSettings();
                    JOptionPane.showMessageDialog(pricingFrame,
                            "Pricing and expenses have been saved.",
                            "Saved", JOptionPane.INFORMATION_MESSAGE);
                    pricingFrame.dispose();
                    app.showAdminDashboard();
                } catch (IOException saveError) {
                    JOptionPane.showMessageDialog(pricingFrame,
                            "Changes were applied but could not be saved to the data folder.\\n"
                            + saveError.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        pricingFrame,
                        "Enter a valid non-negative number for every amount.",
                        "Invalid Amount",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttons.add(backButton);
        buttons.add(saveButton);

        contentPanel.add(heading, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(priceTable), BorderLayout.CENTER);
        contentPanel.add(buttons, BorderLayout.SOUTH);

        pricingFrame.add(
                app.createHeaderPanel(
                        "Pricing & Expenses", pricingFrame, true),
                BorderLayout.NORTH);

        pricingFrame.add(contentPanel, BorderLayout.CENTER);
        pricingFrame.setVisible(true);
    }
}
