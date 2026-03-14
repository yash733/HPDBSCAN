package hpdbscan;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== HPDBSCAN Main starting... ===");

        if (args.length < 4 ) {
            System.out.println("Usage: java hpdbscan.Main <csv_file_path> <epsilon> <minPoints>");
            return;
        }

        String filePath = args[0];
        double epsilon = Double.parseDouble(args[1]);
        int minPoints = Integer.parseInt(args[2]);
        String current_run_dir = args[3];

        try {
            System.out.println("Loading points from: " + filePath);
            List<Point> points = loadPointsFromCSV(filePath);
            System.out.println("Loaded " + points.size() + " points.");

            long startTime = System.currentTimeMillis();

            HPDBSCAN algo = new HPDBSCAN(points, epsilon, minPoints);
            System.out.println("=== Running HPDBSCAN... ===");
            System.out.println("MinPoint:"+minPoints);
            System.out.println("Epsilon:"+epsilon);
            algo.run();
            
            // Ensure output directory exists
            File outputDir = new File(current_run_dir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
                System.out.println("Created output directory: " + current_run_dir);
            }
            
            // Extract just the filename from the filepath (remove directory path)
            String baseFileName = new File(filePath).getName();
            String file_name = current_run_dir + File.separator + "hpdbscan_" + epsilon + "_" + minPoints + "_" + baseFileName;
            try (PrintWriter pw = new PrintWriter(file_name)) {
                for (Point p : points) {
                    double[] c = p.getCoords();
                    // write: x,y,label (extend if higher‑dimensional)
                    pw.println(c[0] + "," + c[1] + "," + p.getLabel());
                }
            }
            System.out.println("Wrote clustering result to " + file_name);

            // System.out.println("Sample Results (First 10 points):");
            // for (int i = 0; i < Math.min(10, points.size()); i++) {
            //     System.out.println(points.get(i));
            // }

            // Count points per cluster
            Map<Integer, Integer> counts = new HashMap<>();
            for (Point p : points) {
                counts.merge(p.getLabel(), 1, Integer::sum);
            }

            // Compute number of clusters (labels > 0) and noise
            int noisePoints = counts.getOrDefault(0, 0);
            int clusterCount = 0;
            for (int label : counts.keySet()) {
                if (label > 0) {
                    clusterCount++;
                }
            }
            // System.out.println("=== Cluster summary: ===");
            // for (var e : counts.entrySet()) {
            //     int label = e.getKey();
            //     int size = e.getValue();
            //     if (label == -1) {
            //         System.out.println("  Noise: " + size + " points");
            //     } else {
            //         System.out.println("  Cluster " + label + ": " + size + " points");
            //     }
            // }

            System.out.println("/----Stats----/");

            long endTime = System.currentTimeMillis();
            System.out.println("==== Clustering completed in " + (endTime - startTime) + "ms ==== ");

            // Calculate total cluster points
            int totalClusterPoints = 0;
            for (int label : counts.keySet()) {
                if (label > 0) {
                    totalClusterPoints += counts.get(label);
                }
            }
            int totalPoints = points.size();
            double clusterPercentage = (double) totalClusterPoints / totalPoints * 100;
            double noisePercentage = (double) noisePoints / totalPoints * 100;

            System.out.println("=== Total Number of: ===");
            System.out.println("Clusters (label > 0): " + clusterCount);
            System.out.println("Cluster points: " + totalClusterPoints + " (" + String.format("%.2f", clusterPercentage) + "%)");
            System.out.println("Noise points (label = 0): " + noisePoints + " (" + String.format("%.2f", noisePercentage) + "%)");

        } catch (IOException e) {
            System.err.println("ERROR - File I/O Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR - Unexpected error during execution: " + e.getMessage());
            e.printStackTrace();
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
