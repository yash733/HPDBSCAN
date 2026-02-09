package hpdbscan;

import java.util.Arrays;

public class Point {
    public final long id;
    public final double[] coordinates;

    public int clusterLabel = -1;   // -1 = undefined, 0 = noise
    public boolean visited = false;
    public boolean isCore = false;

    public Point(long id, double[] coords) {
        this.id = id;
        this.coordinates = coords;
    }

    // Used by Main and Python export
    public double[] getCoords() {
        return coordinates;
    }

    public int getLabel() {
        return clusterLabel;
    }

    public void setLabel(int label) {
        this.clusterLabel = label;
    }

    public double distanceTo(Point other) {
        double sum = 0.0;
        for (int i = 0; i < this.coordinates.length; i++) {
            double diff = this.coordinates[i] - other.coordinates[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    @Override
    public String toString() {
        return "Point{id=" + id +
               ", coords=" + Arrays.toString(coordinates) +
               ", label=" + clusterLabel +
               ", visited=" + visited +
               ", isCore=" + isCore + "}";
    }
}
