package pickleballsystem.model;

public class RentalItem {
    private String name;
    private double hourlyRate;
    private int totalStock;

    public RentalItem(String name, double hourlyRate, int totalStock) {
        this.name = name;
        this.hourlyRate = hourlyRate;
        this.totalStock = totalStock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }
}
