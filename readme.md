# HPDBSCAN
## Team : 8 
### "Yash Gupta"

### To run
1. python .\src\verify_hpdbscan.py
2. java -cp bin hpdbscan.Main _csv_file_path_ _epsilon_ _minPoints_
    ```
    java -cp bin hpdbscan.Main data.csv 1.5 2
    ```

### Python - 
##### Set Custom _csv_file_path_ , _epsilon_ , _minPoints_
- Go to line no. 69 and change input values.

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


#### Quick Access Commands 
<ol>
<li>$env:Path = "Y:\java\jdk-25.0.2\bin;"</li>
<li>rm -Recurse -Force bin</li>
<li>mkdir bin</li>
<li>javac -d bin src\hpdbscan\*.java</li>
<li>java -cp bin hpdbscan.Main datasets/densired_3.csv 2 60</li>
<li>java -Xmx8g -cp bin hpdbscan.Main datasets/densired_3.csv 0.25 60</li>
</ol>

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
        at java.base/jdk.internal.reflect.DirectConstructorHandleAccessor.newInstance(DirectConstructorHandleAccessor.java:62)   
        at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:499)
        at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:483)
        at java.base/java.util.concurrent.ForkJoinTask.getException(ForkJoinTask.java:561)
        at java.base/java.util.concurrent.ForkJoinTask.reportException(ForkJoinTask.java:577)
        at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:667)
        at java.base/java.util.concurrent.ForkJoinTask.invoke(ForkJoinTask.java:681)
        at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateParallel(ForEachOps.java:162)
        at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateParallel(ForEachOps.java:176)
        at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:264)
        at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:632)
        at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:806)
        at hpdbscan.HPDBSCAN.run(HPDBSCAN.java:31)
        at hpdbscan.Main.main(Main.java:34)
Caused by: java.lang.OutOfMemoryError: Java heap space
        at java.base/java.util.Arrays.copyOf(Arrays.java:3478)
        at java.base/java.util.ArrayList.grow(ArrayList.java:238)
        at java.base/java.util.ArrayList.grow(ArrayList.java:245)
        at java.base/java.util.ArrayList.add(ArrayList.java:484)
        at java.base/java.util.ArrayList.add(ArrayList.java:497)
        at hpdbscan.HPDBSCAN.getNeighbors(HPDBSCAN.java:111)
        at hpdbscan.HPDBSCAN.expandCluster(HPDBSCAN.java:99)
        at hpdbscan.HPDBSCAN.processCellLocally(HPDBSCAN.java:75)
        at hpdbscan.HPDBSCAN$$Lambda/0x000000002d041460.accept(Unknown Source)
        at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:186)
        at java.base/java.util.concurrent.ConcurrentHashMap$ValueSpliterator.forEachRemaining(ConcurrentHashMap.java:3628)       
        at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:570)
        at java.base/java.util.stream.ForEachOps$ForEachTask.compute(ForEachOps.java:293)
        at java.base/java.util.concurrent.CountedCompleter.exec(CountedCompleter.java:759)
        at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)
        at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1450)
        at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2019)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:187)
```
