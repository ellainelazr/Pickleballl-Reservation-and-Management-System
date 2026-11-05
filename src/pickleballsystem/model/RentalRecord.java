package pickleballsystem.model;

public class RentalRecord {
    private String itemName;
    private double hourlyRate;
    private int quantity;

    public RentalRecord(String itemName, double hourlyRate, int quantity) {
        this.itemName = itemName;
        this.hourlyRate = hourlyRate;
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public int getQuantity() {
        return quantity;
    }
}
