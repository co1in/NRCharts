package client;

public class NRDataPoint {
    private double value;
    private long timestamp;

    public NRDataPoint(long timestamp, double value) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
