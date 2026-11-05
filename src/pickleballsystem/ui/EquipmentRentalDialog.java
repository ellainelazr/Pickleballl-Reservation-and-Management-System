package pickleballsystem.ui;

import pickleballsystem.ui.BaseScreen;
import pickleballsystem.ui.UiHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import pickleballsystem.PickleballMain;
import pickleballsystem.model.RentalItem;

import static pickleballsystem.ui.UiHelper.*;

/** Dialog for renting paddles, balls, and other equipment with a booking. */
public class EquipmentRentalDialog extends BaseScreen {

    public EquipmentRentalDialog(PickleballMain app) {
        super(app);
    }

    public void showEquipmentRentalDialog(JFrame parentFrame, DefaultTableModel model,
                                           JTable availabilityTable, Runnable updateSelection) {
        JDialog rentalDialog = new JDialog(parentFrame, "Equipment Rental", true);
        rentalDialog.setSize(820, 700);
        rentalDialog.setMinimumSize(new Dimension(650, 580));
        rentalDialog.setLocationRelativeTo(parentFrame);
        rentalDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(INTRO_MINT);

        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setBackground(WHITE);
        headingPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        JLabel heading = new JLabel("Equipment Rental");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(DARK_TEXT);
        JLabel subtitle = new JLabel("Choose optional rentals for your selected court time.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(INTRO_MUTED);
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(subtitle);

        JPanel rentalContent = new JPanel();
        rentalContent.setLayout(new BoxLayout(rentalContent, BoxLayout.Y_AXIS));
        rentalContent.setBackground(INTRO_MINT);
        rentalContent.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel selectedTimeCard = createGuestBookingCard("Selected Date & Time");
        JLabel selectedTime = new JLabel(formatBookingDate(data.getSelectedReservationDate())
                + "  •  " + data.getSelectedCourtSlots().size() + " selected court-hour(s)");
        selectedTime.setFont(new Font("Arial", Font.PLAIN, 13));
        selectedTime.setForeground(DARK_TEXT);
        selectedTimeCard.add(selectedTime);
        rentalContent.add(selectedTimeCard);
        rentalContent.add(Box.createVerticalStrut(12));

        int maxPaddles = data.getMaximumAvailableEquipment(true);
        int maxBalls = data.getMaximumAvailableEquipment(false);
        JSpinner paddleSpinner = new JSpinner(new SpinnerNumberModel(
                Math.min(data.getSelectedPaddleQuantity(), maxPaddles), 0, maxPaddles, 1));
        JSpinner ballSpinner = new JSpinner(new SpinnerNumberModel(
                Math.min(data.getSelectedBallQuantity(), maxBalls), 0, maxBalls, 1));
        paddleSpinner.setPreferredSize(new Dimension(80, 32));
        ballSpinner.setPreferredSize(new Dimension(80, 32));

        JPanel equipmentSection = new JPanel(new BorderLayout(0, 10));
        equipmentSection.setBackground(INTRO_MINT);
        equipmentSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel equipmentHeading = new JLabel("Available Equipment");
        equipmentHeading.setFont(new Font("Arial", Font.BOLD, 16));
        equipmentHeading.setForeground(INTRO_TEAL);

        JPanel equipmentCards = new JPanel(new GridLayout(1, 2, 14, 0));
        equipmentCards.setBackground(INTRO_MINT);
        equipmentCards.add(createEquipmentCard("Pickleball Paddle",
                "₱" + String.format("%.2f", data.getPaddleRentalRate()) + " / hour",
                "Available per court: " + maxPaddles, "paddle.png", "Paddle image", paddleSpinner));
        equipmentCards.add(createEquipmentCard("Pickleball Ball",
                "₱" + String.format("%.2f", data.getBallRentalRate()) + " / hour",
                "Available per court: " + maxBalls, "ball.png", "Ball image", ballSpinner));
        equipmentSection.add(equipmentHeading, BorderLayout.NORTH);
        equipmentSection.add(equipmentCards, BorderLayout.CENTER);
        rentalContent.add(equipmentSection);
        rentalContent.add(Box.createVerticalStrut(12));

        ArrayList<RentalItem> extraRentalItems = data.getAdditionalRentalItems();
        while (data.getExtraRentalQuantities().size() < extraRentalItems.size()) {
            data.getExtraRentalQuantities().add(0);
        }
        JPanel extraRentalPanel = new JPanel(new GridLayout(0, 2, 12, 10));
        extraRentalPanel.setBackground(INTRO_MINT);
        ArrayList<JSpinner> extraRentalSpinners = new ArrayList<>();
        for (int i = 0; i < extraRentalItems.size(); i++) {
            RentalItem item = extraRentalItems.get(i);
            int maxAvailable = data.getMaximumAvailableEquipment(item);
            JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(
                    Math.min(data.getExtraRentalQuantities().get(i), maxAvailable), 0, maxAvailable, 1));
            quantitySpinner.setPreferredSize(new Dimension(80, 32));
            extraRentalSpinners.add(quantitySpinner);

            JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
            itemPanel.setBackground(WHITE);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(215, 232, 229)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            JPanel itemLabels = new JPanel();
            itemLabels.setLayout(new BoxLayout(itemLabels, BoxLayout.Y_AXIS));
            itemLabels.setBackground(WHITE);
            JLabel itemName = new JLabel(item.getName());
            itemName.setFont(new Font("Arial", Font.BOLD, 13));
            itemName.setForeground(INTRO_TEAL);
            JLabel itemRate = new JLabel("₱" + String.format("%.2f", item.getHourlyRate())
                    + " / hour  ·  Available: " + maxAvailable);
            itemRate.setFont(new Font("Arial", Font.PLAIN, 11));
            itemRate.setForeground(INTRO_MUTED);
            itemLabels.add(itemName);
            itemLabels.add(Box.createVerticalStrut(4));
            itemLabels.add(itemRate);
            itemPanel.add(itemLabels, BorderLayout.CENTER);
            itemPanel.add(createQuantityControl(quantitySpinner), BorderLayout.EAST);
            extraRentalPanel.add(itemPanel);
        }
        if (!extraRentalItems.isEmpty()) {
            JLabel extraRentalHeading = new JLabel("Other Rental Items");
            extraRentalHeading.setFont(new Font("Arial", Font.BOLD, 16));
            extraRentalHeading.setForeground(INTRO_TEAL);
            rentalContent.add(extraRentalHeading);
            rentalContent.add(Box.createVerticalStrut(8));
            rentalContent.add(extraRentalPanel);
            rentalContent.add(Box.createVerticalStrut(12));
        }

        JPanel totalCard = createGuestBookingCard("Equipment Summary");
        JLabel paddleTotal = new JLabel();
        JLabel ballTotal = new JLabel();
        JLabel equipmentTotal = new JLabel();
        ArrayList<JLabel> extraRentalTotals = new ArrayList<>();
        for (int i = 0; i < extraRentalItems.size(); i++) {
            JLabel itemTotal = new JLabel();
            itemTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
            extraRentalTotals.add(itemTotal);
            totalCard.add(itemTotal);
            totalCard.add(Box.createVerticalStrut(6));
        }
        paddleTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        ballTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        equipmentTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        equipmentTotal.setFont(new Font("Arial", Font.BOLD, 16));
        equipmentTotal.setForeground(TEAL);
        totalCard.add(paddleTotal);
        totalCard.add(Box.createVerticalStrut(6));
        totalCard.add(ballTotal);
        totalCard.add(Box.createVerticalStrut(10));
        totalCard.add(new JSeparator());
        totalCard.add(Box.createVerticalStrut(8));
        totalCard.add(equipmentTotal);
        rentalContent.add(totalCard);

        Runnable updateEquipmentTotals = () -> {
            data.setSelectedPaddleQuantity((Integer) paddleSpinner.getValue());
            data.setSelectedBallQuantity((Integer) ballSpinner.getValue());
            for (int i = 0; i < extraRentalSpinners.size(); i++) {
                data.getExtraRentalQuantities().set(i, (Integer) extraRentalSpinners.get(i).getValue());
                RentalItem item = extraRentalItems.get(i);
                extraRentalTotals.get(i).setText(item.getName() + ": "
                        + data.getExtraRentalQuantities().get(i) + " × ₱"
                        + String.format("%.2f", item.getHourlyRate()) + "/hour = ₱"
                        + String.format("%.2f", data.getExtraRentalTotal(item, data.getExtraRentalQuantities().get(i))));
            }
            paddleTotal.setText("Paddles: " + data.getSelectedPaddleQuantity() + " × ₱"
                    + String.format("%.2f", data.getPaddleRentalRate()) + "/hour = ₱"
                    + String.format("%.2f", data.getPaddleRentalTotal()));
            ballTotal.setText("Balls: " + data.getSelectedBallQuantity() + " × ₱"
                    + String.format("%.2f", data.getBallRentalRate()) + "/hour = ₱"
                    + String.format("%.2f", data.getBallRentalTotal()));
            equipmentTotal.setText("Equipment Total: ₱"
                    + String.format("%.2f", data.getEquipmentRentalTotal()));
        };
        paddleSpinner.addChangeListener(e -> updateEquipmentTotals.run());
        ballSpinner.addChangeListener(e -> updateEquipmentTotals.run());
        for (int i = 0; i < extraRentalSpinners.size(); i++) {
            int itemIndex = i;
            extraRentalSpinners.get(i).addChangeListener(e -> {
                data.getExtraRentalQuantities().set(itemIndex,
                        (Integer) extraRentalSpinners.get(itemIndex).getValue());
                updateEquipmentTotals.run();
            });
        }
        updateEquipmentTotals.run();

        JScrollPane rentalScrollPane = new JScrollPane(rentalContent);
        rentalScrollPane.setBorder(null);
        rentalScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rentalScrollPane.getViewport().setBackground(INTRO_MINT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(WHITE);
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(120, 42));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> rentalDialog.dispose());

        JButton summaryButton = new JButton("Next: Booking Summary");
        summaryButton.setFont(new Font("Arial", Font.BOLD, 13));
        summaryButton.setBackground(TEAL);
        summaryButton.setForeground(WHITE);
        summaryButton.setPreferredSize(new Dimension(220, 42));
        summaryButton.setFocusPainted(false);
        summaryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        summaryButton.addActionListener(e -> {
            updateEquipmentTotals.run();
            rentalDialog.dispose();
            app.showReservationSummaryDialog(parentFrame, model, availabilityTable, updateSelection);
        });
        buttonPanel.add(backButton);
        buttonPanel.add(summaryButton);

        contentPanel.add(headingPanel, BorderLayout.NORTH);
        contentPanel.add(rentalScrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        rentalDialog.setContentPane(contentPanel);
        rentalDialog.setVisible(true);
    }

    private JPanel createEquipmentCard(String name, String rate, String availability,
                                       String imageFile, String imageDescription,
                                       JSpinner quantitySpinner) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 232, 229), 2, true),
                BorderFactory.createEmptyBorder(14, 12, 14, 12)));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(190, 150));
        imageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(248, 252, 251));
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(234, 242, 240)));
        java.net.URL imageUrl = UiHelper.class.getResource("/pickleballsystem/images/" + imageFile);
        ImageIcon originalImage = imageUrl == null ? null : new ImageIcon(imageUrl);
        if (originalImage == null || originalImage.getIconWidth() <= 0) {
            File[] imagePaths = {
                new File(new File(new File("src"), "pickleballsystem"), "images"),
                new File(new File("pickleballsystem"), "images"),
                new File("images")
            };
            for (File imageDirectory : imagePaths) {
                File imageFileOnDisk = new File(imageDirectory, imageFile);
                if (imageFileOnDisk.isFile()) {
                    ImageIcon fileImage = new ImageIcon(imageFileOnDisk.getPath());
                    if (fileImage.getIconWidth() > 0) {
                        originalImage = fileImage;
                        break;
                    }
                }
            }
        }
        if (originalImage != null && originalImage.getIconWidth() > 0) {
            Image resizedImage = originalImage.getImage().getScaledInstance(150, 125, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(resizedImage));
            imageLabel.setToolTipText(imageFile + " equipment image");
        } else {
            System.err.println("Unable to load equipment image: " + imageFile
                    + ". Keep it in src/pickleballsystem/images or package it at /pickleballsystem/images.");
            imageLabel.setText(imageDescription + " - Add " + imageFile
                    + " to src/pickleballsystem/images");
            imageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            imageLabel.setForeground(INTRO_MUTED);
        }
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(INTRO_TEAL);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel rateLabel = new JLabel(rate);
        rateLabel.setFont(new Font("Arial", Font.BOLD, 13));
        rateLabel.setForeground(INTRO_TEAL);
        rateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel availabilityLabel = new JLabel(availability);
        availabilityLabel.setFont(new Font("Arial", Font.BOLD, 11));
        availabilityLabel.setForeground(new Color(38, 130, 76));
        availabilityLabel.setOpaque(true);
        availabilityLabel.setBackground(new Color(214, 255, 225));
        availabilityLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        availabilityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel quantityPanel = createQuantityControl(quantitySpinner);

        card.add(imageLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(rateLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(availabilityLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(quantityPanel);
        return card;
    }
}
