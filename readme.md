# HPDBSCAN: High-Performance Parallel DBSCAN

**Team 8** | **Author: Yash Gupta**

## Overview

HPDBSCAN is a high-performance, parallel implementation of the DBSCAN (Density-Based Spatial Clustering of Applications with Noise) clustering algorithm. Unlike traditional DBSCAN, which processes data sequentially and can be slow on large datasets, HPDBSCAN leverages spatial indexing and parallel processing to achieve significant speedups.

### What is DBSCAN?
DBSCAN is a popular density-based clustering algorithm that groups together points that are closely packed while marking outliers as noise. It requires two parameters:
- **Epsilon (ε)**: The maximum distance between two points for them to be considered neighbors.
- **MinPoints**: The minimum number of points required to form a dense region (core point).

### Key Features of HPDBSCAN
- **Parallel Processing**: Uses Java's ForkJoinPool for multi-core execution, processing grid cells in parallel.
- **Spatial Indexing**: Employs a hypergrid-based index to efficiently query neighbors, reducing computational complexity.
- **Scalability**: Handles large datasets (e.g., 10 million points) with improved performance over sequential DBSCAN.
- **Memory Efficient**: Avoids loading all points into memory for neighbor searches by using localized grid-based queries.
- **Configurable Parallelism**: Easily adjust the number of threads for optimal performance on your hardware.
- **Output Formats**: Generates CSV files with clustered results, including cluster labels and statistics.

## Prerequisites
- **Java**: JDK 11 or later (tested with JDK 25).
- **Python**: 3.7+ (for the verification script and visualization).
- **Libraries**: tqdm, matplotlib (install via `pip install tqdm matplotlib`).
- **OS**: Windows/Linux/Mac (commands provided for Windows PowerShell).

## Installation
1. Clone or download the repository.
2. Ensure Java and Python are installed and in your PATH.
3. For Python wrapper: Create a virtual environment and install dependencies:
   ```bash
   python -m venv .venv
   .venv\Scripts\activate  # On Windows
   pip install tqdm matplotlib
   ```

## Usage

### Option 1: Python Wrapper (Recommended for Batch Runs)
The Python script `verify_hpdbscan.py` automates compilation, execution, and result collection for multiple parameter combinations.

1. Update paths in `verify_hpdbscan.py` (lines 13-14):
   ```python
   JAVAC_PATH = r"Y:/Java/jdk-25.0.2/bin/javac.exe"  # Adjust to your javac path
   JAVA_PATH  = r"Y:/Java/jdk-25.0.2/bin/java.exe"    # Adjust to your java path
   ```

2. Customize parameters (lines 18-20):
   ```python
   epsilon_val = [0.005, 0.01, 0.015]  # List of epsilon values to test
   minPoint_val = [5, 10, 20]          # List of minPoints values
   data_set = ["datasets/densired_2.csv"]  # List of CSV files
   ```

3. Run the script:
   ```bash
   python .\src\verify_hpdbscan.py
   ```

### Option 2: Direct Java Execution
For quick tests or integration into other systems.

1. Compile the Java code:
   ```bash
   javac -d bin src\hpdbscan\*.java
   ```

2. Run HPDBSCAN:
   ```bash
   java -cp bin hpdbscan.Main <csv_file_path> <epsilon> <minPoints> <output_dir>
   ```
   - `<csv_file_path>`: Path to input CSV (e.g., `datasets/densired_3.csv`).
   - `<epsilon>`: Epsilon value (e.g., `0.2`).
   - `<minPoints>`: Minimum points (e.g., `50`).
   - `<output_dir>`: Directory for output files (e.g., `run_001`).

   Example:
   ```bash
   java -cp bin hpdbscan.Main datasets/densired_3.csv 0.2 50 run_001
   ```

### Quick Access Commands (Windows PowerShell)
1. Set Java PATH: `$env:Path = "Y:\java\jdk-25.0.2\bin;"` (adjust path).
2. Clean bin: `rm -Recurse -Force bin`
3. Create bin: `mkdir bin`
4. Compile: `javac -d bin src\hpdbscan\*.java`
5. Run: `java -cp bin hpdbscan.Main datasets/densired_3.csv 2 60`
6. Run with more memory: `java -Xmx8g -cp bin hpdbscan.Main datasets/densired_3.csv 0.25 60`

## Configuring Number of Cores/Threads
HPDBSCAN uses parallel streams for processing. By default, it uses all available CPU cores. To customize:

### Via Python Wrapper
- Comment out line 82 in `verify_hpdbscan.py`.
- Uncomment line 83 and set the desired thread count (e.g., 8):
  ```python
  run_cmd = [JAVA_PATH, "-Djava.util.concurrent.ForkJoinPool.common.parallelism=8", "-cp", "bin", "hpdbscan.Main", ...]
  ```

### Via Direct Java
- Add the JVM property:
  ```bash
  java -Djava.util.concurrent.ForkJoinPool.common.parallelism=8 -cp bin hpdbscan.Main ...
  ```

### Advanced: Custom ForkJoinPool in Code
- In `HPDBSCAN.java`, comment lines 22 and 35.
- Uncomment lines 21 and 37-38 to use a custom pool.
- Pass parallelism as an argument to the constructor.

**Tip**: Match thread count to your CPU cores (physical + hyper-threaded). Monitor for memory issues with large datasets.

## Class Descriptions

### Point.java
Represents a data point in N-dimensional space.
- Stores coordinates (double[]), unique ID, cluster label, and flags (visited, isCore).
- Provides distance calculation and string representation.

### GridCell.java
Implements spatial indexing using a hypergrid.
- Maps grid cells (keyed by discretized coordinates) to lists of points.
- Supports neighbor cell computation for efficient range queries.

### HPDBSCAN.java
Core algorithm implementation.
- Builds the grid index.
- Runs parallel local DBSCAN on cells.
- Merges cluster labels across cells.
- Uses Union-Find for conflict resolution.

### Main.java
Command-line interface.
- Parses arguments and loads CSV data.
- Executes HPDBSCAN and measures performance.
- Outputs results to CSV with statistics.

## Example Output
```
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
MinPoint:50
Epsilon:0.2
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Phase 3: Merging cluster labels...
Wrote clustering result to 0312_2254\hpdbscan_0.2_50_densired_3.csv
/----Stats----/
==== Clustering completed in 363930ms ====
=== Total Number of: ===
Clusters (label > 0): 19
Cluster points: 8890811 (88.91%)
Noise points (label = 0): 1109189 (11.09%)

   Executed Successfully

1. Compiling Java HPDBSCAN...
   Compilation successful.
2. Running Java HPDBSCAN...
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
MinPoint:80
Epsilon:0.2
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Phase 3: Merging cluster labels...
Wrote clustering result to 0312_2254\hpdbscan_0.2_80_densired_3.csv
/----Stats----/
==== Clustering completed in 358029ms ====
=== Total Number of: ===
Clusters (label > 0): 10
Cluster points: 8460639 (84.61%)
Noise points (label = 0): 1539361 (15.39%)

   Executed Successfully

1. Compiling Java HPDBSCAN...
   Compilation successful.
2. Running Java HPDBSCAN...
=== HPDBSCAN Main starting... ===
Loading points from: datasets/densired_3.csv
Loaded 10000000 points.
=== Running HPDBSCAN... ===
MinPoint:100
Epsilon:0.2
Phase 1: Indexing points into grid...
Phase 2: Running Parallel Local DBSCAN...
Phase 3: Merging cluster labels...
Wrote clustering result to 0312_2254\hpdbscan_0.2_100_densired_3.csv
/----Stats----/
==== Clustering completed in 349329ms ====
=== Total Number of: ===
Clusters (label > 0): 39
Cluster points: 8168496 (81.68%)
Noise points (label = 0): 1831504 (18.32%)

   Executed Successfully

Processing:  83%|██████████████████████████████████████████████████████████████████▋             | 5/6 [1:01:10<17:18, 1038.83s/it]1. Compiling Java HPDBSCAN...
   Compilation successful.
2. Running Java HPDBSCAN...
⚠ Java OutOfMemoryError detected. Skipping.
1. Compiling Java HPDBSCAN...
   Compilation successful.
2. Running Java HPDBSCAN...
⚠ Java OutOfMemoryError detected. Skipping.
```
