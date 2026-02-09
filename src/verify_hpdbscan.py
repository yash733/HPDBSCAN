import subprocess
import csv
import random
import os
import glob
import matplotlib.pyplot as plt
import re

# --- CONFIGURATION ---
# Your specific paths
JAVAC_PATH = r"Y:/Java/jdk-25.0.2/bin/javac.exe"
JAVA_PATH  = r"Y:/Java/jdk-25.0.2/bin/java.exe"
# ---------------------

def run_pipeline():
    env = os.environ.copy()
    env["PATH"] = r"Y:\java\jdk-25.0.2\bin;" + env["PATH"]
    
    # 0. Ensure bin folder exists
    if not os.path.exists("bin"):
        os.makedirs("bin")

    # # 1. Generate Dummy Data (2 Clusters + Noise)
    # print("1. Generating dummy data...")
    # data_points = []
    # # Cluster 1 (around 5,5)
    # for i in range(100):
    #     data_points.append([5 + random.gauss(0, 0.5), 5 + random.gauss(0, 0.5)])
    # # Cluster 2 (around 15,15)
    # for i in range(100):
    #     data_points.append([15 + random.gauss(0, 0.5), 15 + random.gauss(0, 0.5)])
    # # Noise
    # for i in range(20):
    #     data_points.append([random.uniform(0, 20), random.uniform(0, 20)])

    # # Save to CSV
    # csv_path = os.path.abspath("data.csv")
    # with open(csv_path, "w", newline="") as f:
    #     writer = csv.writer(f)
    #     writer.writerows(data_points)
    # print(f"   Data saved to {csv_path}")

    # 2. Compile Java Code
    print("2. Compiling Java HPDBSCAN...")
    # Find all java files in the folder
    java_files = glob.glob(os.path.join("src", "hpdbscan", "*.java"))
    
    if not java_files:
        print("   ERROR: No .java files found in src/hpdbscan/")
        print("   Please ensure your file structure is: src/hpdbscan/Main.java etc.")
        return

    # Build command: Y:/Java/bin/javac.exe -d bin src/hpdbscan/File1.java ...
    compile_cmd = [JAVAC_PATH, "-d", "bin"] + java_files
    
    try:
        subprocess.run(compile_cmd, check=True)
        print("   Compilation successful.")
    except FileNotFoundError:
        print(f"   ERROR: Could not find javac at: {JAVAC_PATH}")
        return
    except subprocess.CalledProcessError as e:
        print(f"   ERROR: Compilation failed.")
        return

    # 3. Run Java Application
    print("3. Running Java HPDBSCAN...")
    run_cmd = [JAVA_PATH, "-cp", "bin", "hpdbscan.Main", "data.csv", "0.1", "2"]
    
    try:
        result = subprocess.run(run_cmd, capture_output=True, text=True, check=True, env=env)
    except subprocess.CalledProcessError as e:
        print("   Java Runtime Error:")
        print(e.stderr)
        return

    # 4. Parse & Plot Results
    print("4. Parsing and Plotting results...")
    clusters = {}
    output_lines = result.stdout.splitlines()
    
    # Regex to parse Point.toString() output
    # Matches both standard "Point{...}" and the optimized "id,label" if you switched
    # We'll support the standard "Point{...}" format from the first provided code
    # Point{id=1, coords=[5.1, 5.2], label=1}
    lines = result.stdout.splitlines()

    clusters = {}   # label -> (xs, ys)
    for line in lines:
        if "Point{" not in line:
            continue

        coords_match = re.search(r"coords=\[(.*?)\]", line)
        label_match = re.search(r"label=(-?\d+)", line)
        if not coords_match or not label_match:
            continue

        coords_str = coords_match.group(1)
        parts = [float(p.strip()) for p in coords_str.split(",")]
        if len(parts) < 2:
            continue

        x, y = parts[0], parts[1]
        label = int(label_match.group(1))

        if label not in clusters:
            clusters[label] = ([], [])
        clusters[label][0].append(x)
        clusters[label][1].append(y)

    # now plot ALL clustered points
    plt.figure(figsize=(8, 6))
    for label, (xs, ys) in clusters.items():
        if label == -1:
            plt.scatter(xs, ys, s=5, c="black", marker="x", label="noise")
        else:
            plt.scatter(xs, ys, s=5, label=f"cluster {label}")

    plt.xlabel("x")
    plt.ylabel("y")
    plt.title("HPDBSCAN clustering result (all points)")
    plt.legend(markerscale=2, fontsize="small", loc="best")
    plt.tight_layout()
    plt.show()
    output_img = "cluster_result.png"
    plt.savefig(output_img)
    print(f"   Success! Plot saved to {output_img}")

if __name__ == "__main__":
    run_pipeline()
