import csv
import os
import random
import matplotlib.pyplot as plt

class Plot:
    dir_name = "C:/Users/yashg/Downloads/123" # change 
    clusters = {}

    def muti_plot(self):
        epsilon_val = [0.005, 0.01, 0.015, 0.03, 0.065, 0.085, 0.1, 0.2, 0.3]
        minPoint_val = [5, 10, 20, 50, 80, 100]
        MAX_POINTS_PER_LABEL = 100000

        for epsilon in epsilon_val:
            for minpoint in minPoint_val:
                name_file = "hpdbscan_"+str(epsilon)+"_"+str(minpoint)+"_densired_2_shrink"
                csv_path = name_file + ".csv"
                csv_path = os.path.join(self.dir_name,csv_path)
                self.clusters = {}  # label -> (xs, ys)

                if not os.path.exists(csv_path):
                    print(f"   Warning: CSV not found: {csv_path} (skipping)")
                    continue

                try:
                    with open(csv_path, newline="") as f:
                        reader = csv.reader(f)
                        for row in reader:
                            if len(row) < 3:
                                continue
                            try:
                                x = float(row[0])
                                y = float(row[1])
                                label = int(row[2])
                            except ValueError:
                                continue

                            if label not in self.clusters:
                                self.clusters[label] = ([], [])

                            # Simple reservoir-lite sampling: keep only up to MAX_POINTS_PER_LABEL
                            if len(self.clusters[label][0]) < MAX_POINTS_PER_LABEL:
                                self.clusters[label][0].append(x)
                                self.clusters[label][1].append(y)
                            # else: skip additional points for this label to limit memory/time
                except Exception as e:
                    print(f"   Error reading {csv_path}: {e}")
                    continue

                #print("4. Found cluster labels:", list(clusters.keys()))

                # now plot ALL clustered points
                plt.figure(figsize=(8, 6))
                for label, (xs, ys) in self.clusters.items():
                    if label == -1:
                        plt.scatter(xs, ys, s=5, c="black", marker="x", label="noise")
                    else:
                        plt.scatter(xs, ys, s=5, label=f"cluster {label}")

                plt.xlabel("x")
                plt.ylabel("y")
                plt.title(f"5. HPDBSCAN clustering result [ep-{epsilon}, min-{minpoint}]")
                plt.legend(markerscale=2, fontsize="small", loc="best")
                # plt.tight_layout()

                plot_dir_ = self.dir_name + "_plot_graph"
                os.makedirs(plot_dir_, exist_ok=True)
                plot_img = name_file + ".png"
                out_path = os.path.join(plot_dir_, plot_img)
                plt.savefig(out_path)
                plt.close()
                print(f"   Success! Plot saved to {out_path}")

    def single_plot(self,csv_path):
        if not os.path.exists(csv_path):
            print(f"CSV not found: {csv_path}")
            return

        with open(csv_path, newline="") as f:
            reader = csv.reader(f)
            for row in reader:
                if len(row) < 3:
                    continue
                try:
                    x = float(row[0])
                    y = float(row[1])
                    label = int(row[2])
                except ValueError:
                    continue

                if label not in self.clusters:
                    self.clusters[label] = ([], [])
                self.clusters[label][0].append(x)
                self.clusters[label][1].append(y)

        #print("4. Found cluster labels:", list(clusters.keys()))

        # now plot ALL clustered points
        plt.figure(figsize=(8, 6))
        for label, (xs, ys) in self.clusters.items():
            if label == -1:
                plt.scatter(xs, ys, s=5, c="black", marker="x", label="noise")
            else:
                plt.scatter(xs, ys, s=5, label=f"cluster {label}")

        plt.xlabel("x")
        plt.ylabel("y")
        plt.title(f"5. HPDBSCAN clustering result {csv_path}")
        plt.legend(markerscale=2, fontsize="small", loc="best")
        # plt.tight_layout()

        plot_dir_ = self.dir_name + "_plot"
        os.makedirs(plot_dir_, exist_ok=True)
        plot_img = os.path.basename(csv_path).replace('.csv', '.png')
        out_path = os.path.join(plot_dir_, plot_img)
        plt.savefig(out_path)
        plt.close()
        print(f"   Success! Plot saved to {out_path}")

if __name__ == '__main__':
    graph = Plot()
    # graph.single_plot(r'0217_2143\hpdbscan_0.01_50.csv')
    graph.muti_plot()