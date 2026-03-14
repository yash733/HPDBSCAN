import subprocess
import csv
import random
import os
import glob
import matplotlib.pyplot as plt
import re
from datetime import datetime
from tqdm import tqdm

# --- CONFIGURATION ---
# Your specific paths
JAVAC_PATH = r"Y:/Java/jdk-25.0.2/bin/javac.exe"
JAVA_PATH  = r"Y:/Java/jdk-25.0.2/bin/java.exe"
# ---------------------

def run_pipeline():
    epsilon_val = [0.005, 0.01, 0.015, 0.03, 0.065, 0.085, 0.1, 0.2, 0.3]
    minPoint_val = [5, 10, 20, 50, 80, 100]
    data_set = ["datasets/densired_2.csv", "datasets/densired_2_shrink.csv", "datasets/densired_3.csv"]

    current_run = datetime.now().strftime("%m%d_%H%M")
    os.makedirs(current_run, exist_ok=True)
    for sample_data in data_set:
        for epsilon in tqdm(epsilon_val, desc="Processing: "):
            for minpoint in minPoint_val:
                if epsilon == 0.03 and minpoint == 5:
                    continue  
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
                print("1. Compiling Java HPDBSCAN...")
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
                print("2. Running Java HPDBSCAN...")
                run_cmd = [JAVA_PATH, "-cp", "bin", "hpdbscan.Main", sample_data, str(epsilon), str(minpoint), current_run]

                try:
                    result = subprocess.run(run_cmd, capture_output=True, text=True, check=True, env=env)
                    if "OutOfMemoryError" in result.stderr:
                        print("⚠ Java ran out of memory. Skipping this configuration.")
                        continue

                    print(result.stdout)
                    print("   Executed Successfully\n")

                except subprocess.CalledProcessError as e:
                    if "OutOfMemoryError" in e.stderr:
                        print("⚠ Java OutOfMemoryError detected. Skipping.")
                        continue
                    else:
                        print("Java Runtime Error:")
                        print(e.stdout)
                        print(e.stderr)
                        return
        
            # 4. Parse & Plot Results
            # print("3. Parsing and Plotting results...")

    # csv_path = "hpdbscan_output.csv"
    # clusters = {}  # label -> (xs, ys)

    # with open(csv_path, newline="") as f:
    #     reader = csv.reader(f)
    #     for row in reader:
    #         if len(row) < 3:
    #             continue
    #         x = float(row[0])
    #         y = float(row[1])
    #         label = int(row[2])

    #         if label not in clusters:
    #             clusters[label] = ([], [])
    #         clusters[label][0].append(x)
    #         clusters[label][1].append(y)

    # print("4. Found cluster labels:", list(clusters.keys()))

    # # now plot ALL clustered points
    # plt.figure(figsize=(8, 6))
    # for label, (xs, ys) in clusters.items():
    #     if label == -1:
    #         plt.scatter(xs, ys, s=5, c="black", marker="x", label="noise")
    #     else:
    #         plt.scatter(xs, ys, s=5, label=f"cluster {label}")

    # plt.xlabel("x")
    # plt.ylabel("y")
    # plt.title("5. HPDBSCAN clustering result (all points)")
    # plt.legend(markerscale=2, fontsize="small", loc="best")
    # plt.tight_layout()
    
    # output_img = "cluster_result.png"
    # plt.savefig(output_img)
    # print(f"   Success! Plot saved to {output_img}")

if __name__ == "__main__":
    run_pipeline()
