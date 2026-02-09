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
    # Command: Y:/Java/bin/java.exe -cp bin hpdbscan.Main data.csv 1.5 4
    run_cmd = [JAVA_PATH, "-cp", "bin", "hpdbscan.Main", "./datasets/densired_2.csv", "0.025", "10"]
    
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
    for line in output_lines:
        if "Point{" in line:
            coords_match = re.search(r'coords=\[(.*?)\]', line)
            label_match = re.search(r'label=(-?\d+)', line)
            
            if coords_match and label_match:
                coords_str = coords_match.group(1)
                # handle spaces after commas if present
                coords = [float(x) for x in coords_str.split(',')]
                label = int(label_match.group(1))
                
                if label not in clusters: clusters[label] = []
                clusters[label].append(coords)

    if not clusters:
        print("   Warning: No clusters parsed. Did the Java output format change?")
        print("   Sample Output:", output_lines[:3])
        return

    print(f"   Found {len(clusters)} groups (including noise). Plotting...")
    
    colors = ['gray', 'red', 'blue', 'green', 'orange', 'purple', 'cyan', 'magenta']
    
    plt.figure(figsize=(10, 6))
    for label, points in clusters.items():
        xs = [p[0] for p in points]
        ys = [p[1] for p in points]
        
        # Color logic: Noise (0 or -1 depending on impl) is usually gray/black
        if label <= 0:
            color = 'black'
            marker = 'x'
            name = "Noise"
            alpha = 0.5
        else:
            color = colors[label % len(colors)]
            marker = 'o'
            name = f"Cluster {label}"
            alpha = 1.0
            
        plt.scatter(xs, ys, c=color, marker=marker, label=name, alpha=alpha, s=30)

    plt.title("HPDBSCAN Result (Java implementation)")
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    output_img = "cluster_result.png"
    plt.savefig(output_img)
    print(f"   Success! Plot saved to {output_img}")

if __name__ == "__main__":
    run_pipeline()
