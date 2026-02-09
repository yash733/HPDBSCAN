package hpdbscan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GridCell {
    public final List<Long> cellKey; // Coordinates of the cell in the grid (e.g., [2, 5, 1])
    public final List<Point> points = new ArrayList<>();

    public GridCell(List<Long> cellKey) {
        this.cellKey = cellKey;
    }

    // Determine which cell a point belongs to based on epsilon
    public static List<Long> getCellKey(Point p, double epsilon) {
        List<Long> key = new ArrayList<>();
        for (double coord : p.coordinates) {
            key.add((long) Math.floor(coord / epsilon));
        }
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridCell gridCell = (GridCell) o;
        return Objects.equals(cellKey, gridCell.cellKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cellKey);
    }
}
