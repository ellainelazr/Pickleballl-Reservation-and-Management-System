package pickleballsystem.model;

public class Court {
    private String courtName;
    private double regularRatePerHour;
    private double peakRatePerHour;
    private boolean available;

    public Court(String courtName, double regularRatePerHour, double peakRatePerHour) {
        this(courtName, regularRatePerHour, peakRatePerHour, true);
    }

    public Court(String courtName, double regularRatePerHour) {
        this(courtName, regularRatePerHour, regularRatePerHour, true);
    }

    public Court(String courtName, double regularRatePerHour, boolean available) {
        this(courtName, regularRatePerHour, regularRatePerHour, available);
    }

    public Court(String courtName, double regularRatePerHour, double peakRatePerHour, boolean available) {
        this.courtName = courtName;
        this.regularRatePerHour = regularRatePerHour;
        this.peakRatePerHour = peakRatePerHour;
        this.available = available;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public double getRegularRatePerHour() {
        return regularRatePerHour;
    }

    public void setRegularRatePerHour(double regularRatePerHour) {
        this.regularRatePerHour = regularRatePerHour;
    }

    public double getPeakRatePerHour() {
        return peakRatePerHour;
    }

    public void setPeakRatePerHour(double peakRatePerHour) {
        this.peakRatePerHour = peakRatePerHour;
    }

    public double getRatePerHour() {
        return regularRatePerHour;
    }

    public void setRatePerHour(double ratePerHour) {
        this.regularRatePerHour = ratePerHour;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return courtName + " - ₱" + String.format("%.0f", regularRatePerHour) + "/hour";
    }
}
