package hpdbscan;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HPDBSCAN {
    private final double epsilon;
    private final int minPoints;
    private final List<Point> points;
    
    // Spatial Index: Map Cell Key -> GridCell
    private final Map<List<Long>, GridCell> grid = new ConcurrentHashMap<>();
    
    // Global Union-Find for merging cluster labels
    private final Map<Integer, Integer> clusterMerges = new ConcurrentHashMap<>();

    public HPDBSCAN(List<Point> points, double epsilon, int minPoints) {
        this.points = points;
        this.epsilon = epsilon;
        this.minPoints = minPoints;
    }

    public void run() {
        System.out.println("Phase 1: Indexing points into grid...");
        buildGrid();

        System.out.println("Phase 2: Running Parallel Local DBSCAN...");
        // Parallel stream mimics distributing work to processors
        grid.values().parallelStream().forEach(this::processCellLocally);
        // how our grid looks 
        // grid = { grid_cell_coordinate : [ list of Point objects (each with coords, visited, clusterLabel, isCore) ] }

        System.out.println("Phase 3: Merging cluster labels...");
        resolveMerges();
    }

    private void buildGrid() {
        for (Point p : points) {
            List<Long> key = GridCell.getCellKey(p, epsilon);
            // Storing Coordinates of block under Grid map
            grid.computeIfAbsent(key, k -> new GridCell(k)).points.add(p); 
        }
    }

    // Run DBSCAN logic on a single cell, considering Halo blocks
    // input data type GridCell can var cell
    private void processCellLocally(GridCell cell) {
        List<Point> contextPoints = getHaloPoints(cell); // hot block + halo blocks

        for (Point p : cell.points) {
            if (p.visited) continue;
            p.visited = true;

            // Find neighbors within epsilon
            List<Point> neighbors = getNeighbors(p, contextPoints);

            if (neighbors.size() < minPoints) {
                if (p.clusterLabel == -1) p.clusterLabel = 0; // Noise
            } else {
                p.isCore = true;
                int currentClusterId;

                // Several threads might touch the same point p (because of halos and parallel cells).
                // We want to avoid two threads assigning two different cluster ids to p at the same time.
                // So we lock on p itself, for a very short time, to make this assignment safe.
                synchronized (p) {
                   if (p.clusterLabel <= 0) {
                       p.clusterLabel = (int) p.id; // setting core-point as cluster with lable, its own corr-
                   }
                   currentClusterId = p.clusterLabel;
                }

                expandCluster(p, neighbors, currentClusterId, contextPoints);
            }
        }
    }

    private void expandCluster(Point core, List<Point> neighbors, int clusterId, List<Point> context) {
        // Standard DBSCAN expansion, but record merges if we hit existing clusters
        Queue<Point> queue = new LinkedList<>(neighbors);
        
        while (!queue.isEmpty()) {
            Point q = queue.poll();
            
            // Conflict Detection (Phase 4 of paper):
            // If q already has a DIFFERENT label, we must merge these two clusters later.
            if (q.clusterLabel != -1 && q.clusterLabel != 0 && q.clusterLabel != clusterId) {
                recordMerge(clusterId, q.clusterLabel);
                continue; 
            }

            if (q.clusterLabel == 0) q.clusterLabel = clusterId; // noise >--to--> border
            if (q.clusterLabel != -1) continue; // Already processed
            q.clusterLabel = clusterId;
            q.visited = true;

            List<Point> q_Neighbors = getNeighbors(q, context);
            if (q_Neighbors.size() >= minPoints) {
                q.isCore = true;
                queue.addAll(q_Neighbors);
            }
        }
    }

    private List<Point> getNeighbors(Point p, List<Point> context) {
        List<Point> neighbors = new ArrayList<>();
        for (Point candidate : context) {
            if (p.distanceTo(candidate) <= epsilon) {
                neighbors.add(candidate);
            }
        }
        return neighbors;
    }

    // Retrieve points from this cell AND all 3^d - 1 neighbor cells
    private List<Point> getHaloPoints(GridCell cell) {
        List<Point> halo = new ArrayList<>(cell.points);
        List<List<Long>> neighborKeys = generateNeighborKeys(cell.cellKey);
        
        for (List<Long> key : neighborKeys) {
            GridCell neighbor = grid.get(key);
            if (neighbor != null) {
                halo.addAll(neighbor.points);
            }
        }
        return halo;
    }

    // Utility to generate keys for all adjacent grid cells
    private List<List<Long>> generateNeighborKeys(List<Long> center) {
        List<List<Long>> keys = new ArrayList<>();
        generateKeysRecursive(center, new ArrayList<>(), 0, keys);
        return keys;
    }

    private void generateKeysRecursive(List<Long> center, List<Long> current, int dim, List<List<Long>> result) {
        if (dim == center.size()) {
            if (!current.equals(center)) result.add(new ArrayList<>(current));
            return;
        }
        long val = center.get(dim);
        for (long offset = -1; offset <= 1; offset++) {
            current.add(val + offset);
            generateKeysRecursive(center, current, dim + 1, result);
            current.remove(current.size() - 1);
        }
    }

    private void recordMerge(int labelA, int labelB) {
        int rootA = findRoot(labelA);
        int rootB = findRoot(labelB);
        if (rootA != rootB) {
            // Simple union
            synchronized (clusterMerges) {
                clusterMerges.put(Math.max(rootA, rootB), Math.min(rootA, rootB));
            }
        }
    }

    // Find the canonical label for a cluster 
    private int findRoot(int label) {
        int curr = label;
        while (clusterMerges.containsKey(curr)) {
            curr = clusterMerges.get(curr);
        }
        return curr;
    }

    // Final pass: update all point labels to their canonical root label
    private void resolveMerges() {
        for (Point p : points) {
            if (p.clusterLabel > 0) {
                p.clusterLabel = findRoot(p.clusterLabel);
            }
        }
    }
}
