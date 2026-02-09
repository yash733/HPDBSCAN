package hpdbscan;

import java.util.Arrays;

public class Point {
    public final long id;
    public final double[] coordinates;
    
    // Standard mutable fields that match HPDBSCAN.java logic
    public int clusterLabel = -1; // -1 = undefined, 0 = noise
    public boolean visited = false;
    public boolean isCore = false;

    public Point(long id, double[] coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }

    public double distanceTo(Point other) {
        double sum = 0;
        for (int i = 0; i < coordinates.length; i++) {
            double diff = this.coordinates[i] - other.coordinates[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    @Override
    public String toString() {
        return "Point{id=" + id + ", coords=" + Arrays.toString(coordinates) + ", label=" + clusterLabel + "}";
    }
}
