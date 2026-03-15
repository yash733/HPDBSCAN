package hpdbscan;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HPDBSCAN {
    private final double epsilon;
    private final int minPoints;
    private final List<Point> points;
    private final int parallelism;
    
    
    // Spatial Index: Map Cell Key -> GridCell
    private final Map<List<Long>, GridCell> grid = new ConcurrentHashMap<>();
    
    // Global Union-Find for merging cluster labels
    private final Map<Integer, Integer> clusterMerges = new ConcurrentHashMap<>();
    
    // public HPDBSCAN(List<Point> points, double epsilon, int minPoints, int parallelism) {
    public HPDBSCAN(List<Point> points, double epsilon, int minPoints) {
        this.points = points;
        this.epsilon = epsilon;
        this.minPoints = minPoints;
        //this.parallelism = parallelism;
    }

    public void run() {
        System.out.println("Phase 1: Indexing points into grid...");
        buildGrid();

        System.out.println("Phase 2: Running Parallel Local DBSCAN...");
        // Parallel stream mimics distributing work to processors
        grid.values().parallelStream().forEach(this::processCellLocally);
        // To handel No. of Cores to run on
        // ForkJoinPool pool = new ForkJoinPool(parallelism);
        // pool.submit(() -> grid.values().parallelStream().forEach(this::processCellLocally)).join();

        System.out.println("Phase 3: Merging cluster labels...");
        resolveMerges();
    }

    private void buildGrid() {
        for (Point p : points) {
            List<Long> key = GridCell.getCellKey(p, epsilon);
            grid.computeIfAbsent(key, k -> new GridCell(k)).points.add(p); 
        }
    }

    // Run DBSCAN logic on a single cell (no more contextPoints giant list!)
    private void processCellLocally(GridCell cell) {
        for (Point p : cell.points) {
            if (p.visited) continue;
            p.visited = true;

            // Query neighbors directly from grid (no giant list)
            List<Point> neighbors = getNeighbors(p, cell);

            if (neighbors.size() < minPoints) {
                if (p.clusterLabel == -1) p.clusterLabel = 0; // Noise
            } else {
                p.isCore = true;
                int currentClusterId;

                synchronized (p) {
                    if (p.clusterLabel <= 0) {
                        p.clusterLabel = (int) p.id; // core-point as cluster label
                    }
                    currentClusterId = p.clusterLabel;
                }

                expandCluster(p, neighbors, currentClusterId, cell);
            }
        }
    }

    private void expandCluster(Point core, List<Point> neighbors, int clusterId, GridCell cell) {
        Queue<Point> queue = new LinkedList<>(neighbors);
        
        while (!queue.isEmpty()) {
            Point q = queue.poll();
            
            // Conflict Detection: merge if different cluster labels
            if (q.clusterLabel != -1 && q.clusterLabel != 0 && q.clusterLabel != clusterId) {
                recordMerge(clusterId, q.clusterLabel);
                continue; 
            }

            if (q.clusterLabel == 0) q.clusterLabel = clusterId; // noise -> border
            if (q.clusterLabel != -1) continue; // Already processed
            q.clusterLabel = clusterId;
            q.visited = true;

            // Query neighbors directly from grid (no giant list)
            List<Point> q_Neighbors = getNeighbors(q, cell);
            if (q_Neighbors.size() >= minPoints) {
                q.isCore = true;
                queue.addAll(q_Neighbors);
            }
        }
    }

    // NEW: Scan neighbor cells on-the-fly, NO giant contextPoints list
    private List<Point> getNeighbors(Point p, GridCell centerCell) {
        List<Point> neighbors = new ArrayList<>();
        
        // Get all neighbor cell keys (27 total: self + 26 neighbors)
        List<List<Long>> neighborKeys = generateNeighborKeys(centerCell.cellKey);
        neighborKeys.add(centerCell.cellKey);  // include self
        
        for (List<Long> key : neighborKeys) {
            GridCell gc = grid.get(key);
            if (gc == null) continue;
            
            // Scan points in this neighbor cell
            for (Point candidate : gc.points) {
                if (p.distanceTo(candidate) <= epsilon + 1e-9) {
                    neighbors.add(candidate);
                }
            }
        }
        return neighbors;
    }

    // Retrieve neighbor cell keys (unchanged)
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
            synchronized (clusterMerges) {
                clusterMerges.put(Math.max(rootA, rootB), Math.min(rootA, rootB));
            }
        }
    }

    private int findRoot(int label) {
        int curr = label;
        while (clusterMerges.containsKey(curr)) {
            curr = clusterMerges.get(curr);
        }
        return curr;
    }

    // Final pass: update all point labels to canonical roots
    private void resolveMerges() {
        for (Point p : points) {
            if (p.clusterLabel > 0) {
                p.clusterLabel = findRoot(p.clusterLabel);
            }
        }
    }
}
