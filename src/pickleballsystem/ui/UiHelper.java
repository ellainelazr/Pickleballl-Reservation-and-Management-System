package pickleballsystem.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Shared colors, widget builders, and text formatting used by every screen. */
public final class UiHelper {

    public static final Color CYAN = new Color(0, 188, 212);
    public static final Color TEAL = new Color(0, 150, 136);
    public static final Color LIME = new Color(205, 220, 57);
    public static final Color RED = new Color(244, 67, 54);
    public static final Color GREEN = new Color(76, 175, 80);
    public static final Color LIGHT_GRAY = new Color(245, 245, 245);
    public static final Color WHITE = Color.WHITE;
    public static final Color PENDING_YELLOW = new Color(255, 240, 194);
    public static final Color DARK_TEXT = new Color(33, 33, 33);
    public static final Color LIGHT_GREEN = new Color(214, 255, 215);
    public static final Color LIGHT_RED = new Color(255, 220, 220);
    public static final Color SELECTED_BLUE = new Color(100, 200, 255);
    public static final Color INTRO_TEAL = new Color(0, 91, 94);
    public static final Color INTRO_DARK_TEAL = new Color(0, 78, 82);
    public static final Color INTRO_MINT = new Color(234, 247, 245);
    public static final Color INTRO_MUTED = new Color(91, 111, 121);
    public static final Color INTRO_YELLOW = new Color(220, 237, 74);

    private UiHelper() {
    }

    /** Gives a button the standard bold look used across the system. */
    static void styleButton(JButton button, Color background, Color foreground, int fontSize, int width, int height) {
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setPreferredSize(new Dimension(width, height));
    }

    /** Creates a table model whose cells cannot be edited by the user. */
    static DefaultTableModel readOnlyTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
    }

    static JPanel createIntroCard(String icon, String title, String description, Color iconColor) {
        JPanel cardPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        cardPanel.setBackground(WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 235, 238)),
                BorderFactory.createEmptyBorder(12, 20, 12, 16)));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 24));
        iconLabel.setForeground(iconColor);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setForeground(INTRO_TEAL);
        JTextArea descriptionLabel = createWrappedTextArea(
                description, new Font("Arial", Font.PLAIN, 13), INTRO_MUTED);
        cardPanel.add(iconLabel);
        cardPanel.add(titleLabel);
        cardPanel.add(descriptionLabel);
        return cardPanel;
    }

    static JTextArea createWrappedTextArea(String text, Font font, Color foreground) {
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(font);
        textArea.setForeground(foreground);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setOpaque(false);
        textArea.setBorder(null);
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        return textArea;
    }

    static JLabel createCourtImageLabel(int maxWidth, int maxHeight) {
        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(248, 252, 251));
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(215, 232, 229)));
        imageLabel.setPreferredSize(new Dimension(maxWidth, maxHeight));

        ImageIcon originalImage = loadProjectImage("Court.png");
        if (originalImage == null || originalImage.getIconWidth() <= 0) {
            imageLabel.setText("Court.png was not found in src/pickleballsystem/images.");
            imageLabel.setForeground(INTRO_MUTED);
            imageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            return imageLabel;
        }

        double scale = Math.min((double) maxWidth / originalImage.getIconWidth(),
                (double) maxHeight / originalImage.getIconHeight());
        int imageWidth = (int) (originalImage.getIconWidth() * scale);
        int imageHeight = (int) (originalImage.getIconHeight() * scale);
        Image resizedImage = originalImage.getImage()
                .getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(resizedImage));
        imageLabel.setToolTipText("Court layout");
        return imageLabel;
    }

    private static ImageIcon loadProjectImage(String imageName) {
        java.net.URL imageUrl = UiHelper.class.getResource("/pickleballsystem/images/" + imageName);
        if (imageUrl != null) {
            ImageIcon resourceImage = new ImageIcon(imageUrl);
            if (resourceImage.getIconWidth() > 0) {
                return resourceImage;
            }
        }

        File[] imageDirectories = {
            new File(new File("src", "pickleballsystem"), "images"),
            new File(new File("pickleballsystem"), "images"),
            new File("images")
        };
        for (File imageDirectory : imageDirectories) {
            File imageFile = new File(imageDirectory, imageName);
            if (imageFile.isFile()) {
                ImageIcon fileImage = new ImageIcon(imageFile.getPath());
                if (fileImage.getIconWidth() > 0) {
                    return fileImage;
                }
            }
        }
        return null;
    }

    static JPanel createQuantityControl(JSpinner quantitySpinner) {
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        quantityPanel.setBackground(WHITE);
        JButton decreaseButton = new JButton("-");
        JButton increaseButton = new JButton("+");
        for (JButton button : new JButton[] {decreaseButton, increaseButton}) {
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setBackground(TEAL);
            button.setForeground(WHITE);
            button.setPreferredSize(new Dimension(42, 38));
            button.setMinimumSize(new Dimension(42, 38));
            button.setMaximumSize(new Dimension(42, 38));
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        decreaseButton.setToolTipText("Decrease quantity");
        increaseButton.setToolTipText("Increase quantity");

        JLabel quantityLabel = new JLabel(quantitySpinner.getValue().toString(), SwingConstants.CENTER);
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        quantityLabel.setOpaque(true);
        quantityLabel.setBackground(WHITE);
        quantityLabel.setPreferredSize(new Dimension(54, 38));
        quantityLabel.setMinimumSize(new Dimension(54, 38));
        quantityLabel.setMaximumSize(new Dimension(54, 38));
        quantityLabel.setBorder(BorderFactory.createLineBorder(new Color(195, 207, 211)));
        quantityPanel.setPreferredSize(new Dimension(160, 38));
        quantityPanel.setMinimumSize(new Dimension(160, 38));
        quantityPanel.setMaximumSize(new Dimension(160, 38));

        SpinnerNumberModel quantityModel = (SpinnerNumberModel) quantitySpinner.getModel();
        Runnable updateQuantityControl = () -> {
            int quantity = (Integer) quantitySpinner.getValue();
            int minimum = (Integer) quantityModel.getMinimum();
            int maximum = (Integer) quantityModel.getMaximum();
            quantityLabel.setText(String.valueOf(quantity));
            decreaseButton.setEnabled(quantity > minimum);
            increaseButton.setEnabled(quantity < maximum);
        };
        quantitySpinner.addChangeListener(e -> updateQuantityControl.run());
        decreaseButton.addActionListener(e -> {
            int quantity = (Integer) quantitySpinner.getValue();
            if (quantity > (Integer) quantityModel.getMinimum()) {
                quantitySpinner.setValue(quantity - 1);
            }
        });
        increaseButton.addActionListener(e -> {
            int quantity = (Integer) quantitySpinner.getValue();
            int maximum = (Integer) quantityModel.getMaximum();
            if (quantity < maximum) {
                quantitySpinner.setValue(quantity + 1);
            }
        });
        quantityPanel.add(decreaseButton);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(increaseButton);
        updateQuantityControl.run();
        return quantityPanel;
    }

    static JPanel createGuestBookingCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 232)),
                BorderFactory.createEmptyBorder(14, 15, 14, 15)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(DARK_TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(11));
        return card;
    }

    static JCheckBox createGuestPolicyCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
        checkBox.setForeground(DARK_TEXT);
        checkBox.setBackground(WHITE);
        checkBox.setFocusPainted(false);
        return checkBox;
    }

    static JPanel createPolicyRow(JCheckBox checkBox, JButton policyButton) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(checkBox, BorderLayout.CENTER);
        if (policyButton != null) {
            row.add(policyButton, BorderLayout.EAST);
        }
        return row;
    }

    static JButton createPolicyLink(String buttonText, String title, String policyText) {
        JButton button = new JButton(buttonText);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(INTRO_TEAL);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> JOptionPane.showMessageDialog(
                button, policyText, title, JOptionPane.INFORMATION_MESSAGE));
        return button;
    }

    static JPanel createGuestInputRow(String labelText, JTextField inputField) {
        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setBackground(WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(DARK_TEXT);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setPreferredSize(new Dimension(300, 36));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(195, 207, 211)),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        row.add(label, BorderLayout.NORTH);
        row.add(inputField, BorderLayout.CENTER);
        return row;
    }

    static String formatBookingDate(String date) {
        return LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    static JLabel createConfirmationSectionHeading(String text) {
        JLabel heading = new JLabel(text);
        heading.setFont(new Font("Arial", Font.BOLD, 14));
        heading.setForeground(DARK_TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        return heading;
    }

    static JPanel createConfirmationRow(String label, String value, boolean emphasizeValue) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(new Color(245, 245, 245));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.PLAIN, 13));
        labelComponent.setForeground(INTRO_MUTED);
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", emphasizeValue ? Font.BOLD : Font.PLAIN, 13));
        valueComponent.setForeground(DARK_TEXT);
        valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.EAST);
        return row;
    }

    static JPanel createConfirmationRow(String label, String quantity, String amount) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(new Color(245, 245, 245));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JPanel itemDescription = new JPanel();
        itemDescription.setLayout(new BoxLayout(itemDescription, BoxLayout.Y_AXIS));
        itemDescription.setOpaque(false);
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        labelComponent.setForeground(DARK_TEXT);
        JLabel quantityComponent = new JLabel(quantity);
        quantityComponent.setFont(new Font("Arial", Font.PLAIN, 11));
        quantityComponent.setForeground(INTRO_MUTED);
        itemDescription.add(labelComponent);
        itemDescription.add(quantityComponent);
        JLabel amountComponent = new JLabel(amount);
        amountComponent.setFont(new Font("Arial", Font.BOLD, 13));
        amountComponent.setForeground(DARK_TEXT);
        amountComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(itemDescription, BorderLayout.WEST);
        row.add(amountComponent, BorderLayout.EAST);
        return row;
    }

    static JLabel createConfirmationMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(INTRO_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JSeparator createConfirmationDivider() {
        JSeparator divider = new JSeparator();
        divider.setForeground(new Color(220, 224, 222));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        return divider;
    }

    public static String formatConfirmationDate(String date) {
        return LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    public static String formatCurrency(double amount) {
        return "₱" + String.format("%.2f", amount);
    }

    // Helper method - Create a detail row panel
    static JPanel createDetailRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(WHITE);
        row.setMaximumSize(new Dimension(600, 28));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        labelComponent.setForeground(new Color(100, 100, 100));

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.BOLD, 12));
        valueComponent.setForeground(valueColor);
        valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.EAST);
        row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        return row;
    }

    static JButton createAdminSidebarButton(String text, boolean selected, Runnable action) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(selected ? INTRO_TEAL : INTRO_MUTED);
        button.setBackground(selected ? new Color(205, 239, 235) : INTRO_MINT);
        button.setBorder(BorderFactory.createEmptyBorder(13, 14, 13, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        return button;
    }

    static JPanel createAdminSummaryCard(String title, String value, String description, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 232, 229)),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(INTRO_MUTED);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(accent);
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descriptionLabel.setForeground(INTRO_MUTED);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descriptionLabel);
        return card;
    }

    static JButton createAdminActionButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, TEAL, WHITE, 13, 220, 42);
        return button;
    }

    static void styleAdminTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setGridColor(new Color(225, 235, 234));
        table.setShowVerticalLines(true);
        table.setSelectionBackground(new Color(205, 239, 235));
        table.setSelectionForeground(INTRO_TEAL);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 244, 244));
        table.getTableHeader().setForeground(INTRO_TEAL);
    }

    static String emptyAsDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    public static JButton createNavButton(String text, Color bgColor, Runnable action) {
        JButton button = new JButton(text);
        styleButton(button, bgColor, WHITE, 12, 180, 40);
        button.addActionListener(e -> action.run());
        return button;
    }
    
    
public static JTextField createIntegerOnlyField(int columns) {
    JTextField field = new JTextField(columns);

    ((AbstractDocument) field.getDocument()).setDocumentFilter(
        new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset,
                    String string, AttributeSet attr)
                    throws BadLocationException {
                if (string != null && string.matches("[0-9]+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length,
                    String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null || text.isEmpty()
                        || text.matches("[0-9]+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

    return field;
}

    public static JTextField createDecimalOnlyField(int columns) {
        JTextField field = new JTextField(columns);

        ((AbstractDocument) field.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset,
                        String string, AttributeSet attr)
                        throws BadLocationException {
                    if (string != null && string.matches("[0-9.]+")
                            && isValidDecimal(fb.getDocument().getText(0,
                                    fb.getDocument().getLength()), string,
                                    offset, 0)) {
                        super.insertString(fb, offset, string, attr);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs)
                        throws BadLocationException {
                    String current = fb.getDocument().getText(0,
                            fb.getDocument().getLength());

                    if (text == null || text.isEmpty()
                            || (text.matches("[0-9.]+")
                            && isValidDecimal(current, text, offset, length))) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });

        return field;
    }

    private static boolean isValidDecimal(String current, String inserted,
            int offset, int length) {
        String result = current.substring(0, offset)
                + inserted
                + current.substring(offset + length);

        return result.chars().filter(ch -> ch == '.').count() <= 1;
    }

}
