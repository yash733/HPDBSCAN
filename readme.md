# HPDBSCAN
## Team : 8 
### "Yash Gupta"

### To run
1. Python: ```python .\src\verify_hpdbscan.py```<br>
        <b>change line '13'```JAVAC_PATH``` & line '14'```JAVA_PATH```</b>
2. Java: ```java -cp bin hpdbscan.Main _csv_file_path_ _epsilon_ _minPoints_```<br>
    ```
    java -cp bin hpdbscan.Main data.csv 1.5 2
    ```

### Python - 
##### Set Custom _csv_file_path_ , _epsilon_ , _minPoints_
- In ```verify_hpdbscan.py```, go to line 18,19,20 and change the values to iterate over, generating required results.

### Java, Quick Access Commands 
<ol>
<li>$env:Path = "Y:\java\jdk-25.0.2\bin;"</li> <b>change as per your sys config</b><br>
<li>rm -Recurse -Force bin</li>
<li>mkdir bin</li>
<li>javac -d bin src\hpdbscan\*.java</li>
<li>java -cp bin hpdbscan.Main datasets/densired_3.csv 2 60</li>
<li>java -Xmx8g -cp bin hpdbscan.Main datasets/densired_3.csv 0.25 60</li>
</ol>

### Custom Classes
-  Point      -> uses double[], int, boolean, etc.
-  GridCell   -> uses List<Point>, List<Long>
-  HPDBSCAN   -> uses Map<List<Long>, GridCell>, List<Point>

### Description:
<ul>
<li>Point.java</li>
<p>Represents a single data point in N-dimensional space, storing its unique id, coordinate array, and clustering metadata such as assigned cluster label and flags for core or noise points. It also provides helper methods like distance computation and a readable toString used by the Python visualization script.</p> 
<li>GridCell.java</li>
<p>Implements the grid-based spatial index used by HPDBSCAN, mapping a hypergrid cell (discretized by epsilon) to the list of points it contains. It supports computing cell neighbors and efficient lookup of candidate points for epsilon-neighborhood queries, which reduces the cost of range searches.</p>
<li>HPDBSCAN.java</li>
<p>Contains the actual HPDBSCAN clustering algorithm: building the hypergrid index, estimating cell costs, splitting space for parallelism, running the local DBSCAN-style pass, and merging partial clusters by rule-based relabeling. It assigns final cluster labels or noise to every Point.</p>
<li>Main.java</li>
<p>Command-line entry point. It parses arguments (<csv_file_path> <epsilon> <minPoints>), loads the dataset into Point objects, invokes HPDBSCAN with the chosen parameters, measures runtime, and prints a sample of the labeled points to standard output for further analysis or plotting.</p>
</ul>

### Config Number of Cores to oprate on?
In verify_hpdbscan.py change ```run_cmd = [JAVA_PATH, "-cp", "bin", "hpdbscan.Main", sample_data, str(epsilon), str(minpoint), current_run]``` to ```run_cmd = [JAVA_PATH, "-Djava.util.concurrent.ForkJoinPool.common.parallelism=8", "-cp", "bin", "hpdbscan.Main", sample_data, str(epsilon), str(minpoint), current_run]```
- Replace 8 with your desired number of threads (e.g., match your CPU's core count or hyper-threaded logical cores).
- Test incrementally—start with a value like 4 or 8 and monitor performance/memory usage, as too many threads can lead to overhead or out-of-memory errors (especially with large datasets).



#### Sample outputs
```
(.venv) PS D:\DB Scan> java -cp bin hpdbscan.Main datasets/densired_3.csv 0.025 50
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Phase 3: Merging cluster labels...
Wrote clustering result to hpdbscan_output.csv
=== Cluster summary: ===
  Cluster 0: 9999536 points
  Cluster 3356064: 50 points
  Cluster 3355223: 50 points
  Cluster 3355140: 61 points
  Cluster 3340309: 72 points
  Cluster 3324601: 50 points
  Cluster 3369646: 51 points
  Cluster 3330892: 130 points
 ==== Clustering completed in 37603ms ====
=== Total Number of: ===
Clusters (label > 0): 7
Noise points (label = 0): 9999536

(.venv) PS D:\DB Scan> java -cp bin hpdbscan.Main datasets/densired_3.csv 0.02 60
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Phase 3: Merging cluster labels...
Wrote clustering result to hpdbscan_output.csv
 ==== Clustering completed in 29389ms ==== 
=== Total Number of: ===
Clusters (label > 0): 0
Noise points (label = 0): 10000000

(.venv) PS D:\DB Scan> java -cp bin hpdbscan.Main datasets/densired_3.csv 0.02 15
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Phase 3: Merging cluster labels...
Wrote clustering result to hpdbscan_output.csv
 ==== Clustering completed in 37034ms ==== 
=== Total Number of: ===
Clusters (label > 0): 310
Noise points (label = 0): 9948533

(.venv) PS D:\DB Scan> java -cp bin hpdbscan.Main datasets/densired_3.csv 0.035 60
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Phase 3: Merging cluster labels...
Wrote clustering result to hpdbscan_output.csv
 ==== Clustering completed in 46883ms ==== 
=== Total Number of: ===
Clusters (label > 0): 12
Noise points (label = 0): 9945476

(.venv) PS D:\DB Scan> java -cp bin hpdbscan.Main datasets/densired_3.csv 2 60
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Exception in thread "main" java.lang.OutOfMemoryError
```
