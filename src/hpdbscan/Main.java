package hpdbscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("HPDBSCAN Main starting...");

        if (args.length < 3) {
            System.out.println("Usage: java hpdbscan.Main <csv_file_path> <epsilon> <minPoints>");
            return;
        }

        String filePath = args[0];
        double epsilon = Double.parseDouble(args[1]);
        int minPoints = Integer.parseInt(args[2]);

        try {
            System.out.println("Loading points from: " + filePath);
            List<Point> points = loadPointsFromCSV(filePath);
            System.out.println("Loaded " + points.size() + " points.");

            long startTime = System.currentTimeMillis();

            HPDBSCAN algo = new HPDBSCAN(points, epsilon, minPoints);
            System.out.println("Running HPDBSCAN...");
            algo.run();

            long endTime = System.currentTimeMillis();
            System.out.println("Clustering completed in " + (endTime - startTime) + "ms");

            System.out.println("Sample Results (First 10 points):");
            for (int i = 0; i < Math.min(10, points.size()); i++) {
                System.out.println(points.get(i));
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static List<Point> loadPointsFromCSV(String path) throws IOException {
        List<Point> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            long id = 1;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                double[] coords = new double[parts.length];
                try {
                    for (int i = 0; i < parts.length; i++) {
                        coords[i] = Double.parseDouble(parts[i].trim());
                    }
                    points.add(new Point(id++, coords));
                } catch (NumberFormatException e) {
                    // skip non-numeric lines
                    continue;
                }
            }
        }
        return points;
    }
}
