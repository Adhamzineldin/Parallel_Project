package evaluation;

import core.KMeansConfig;
import core.KMeansParallel;
import core.KMeansSequential;
import model.Point;

import java.util.ArrayList;
import java.util.List;

public class KMeansExperiment {

    private List<Point> dataset;

    public KMeansExperiment(List<Point> dataset) {
        this.dataset = dataset;
    }

    /**
     * Run experiments for multiple K values and dataset sizes
     * @param kValues array of K (number of clusters) to test
     * @param subsetSizes array of dataset sizes to test (e.g., 100, 500, full)
     */
    public void runExperiment(int[] kValues, int[] subsetSizes) {
        System.out.println("KMeans Experiment Results:");
        System.out.println("Dataset size | K | Sequential SSE | Seq Runtime(ms) | Parallel SSE | Par Runtime(ms)");

        for (int size : subsetSizes) {
            // Take a subset of the dataset if needed
            List<Point> currentDataset = dataset.subList(0, Math.min(size, dataset.size()));

            for (int k : kValues) {
                KMeansConfig config = new KMeansConfig(k, 1000, 0.00000000001);

                // --- Sequential ---
                KMeansSequential sequential = new KMeansSequential(config, currentDataset);
                long startSeq = System.currentTimeMillis();
                sequential.run();
                long endSeq = System.currentTimeMillis();
                double sseSeq = SSECalculator.computeSSE(sequential.getClusters());
                long runtimeSeq = endSeq - startSeq;

                // --- Parallel ---
                KMeansParallel parallel = new KMeansParallel(config, currentDataset);
                long startPar = System.currentTimeMillis();
                parallel.run();
                long endPar = System.currentTimeMillis();
                double ssePar = SSECalculator.computeSSE(parallel.getClusters());
                long runtimePar = endPar - startPar;

                // Print results in table row
                System.out.printf("%12d | %2d | %14.2f | %14d | %12.2f | %13d%n",
                        currentDataset.size(), k, sseSeq, runtimeSeq, ssePar, runtimePar);
            }
        }
    }

    // Optional: main for quick testing
    public static void main(String[] args) {
        // Load both datasets
        List<Point> mallPoints = model.DataSetLoader.loadMallDataset();
        List<Point> bankPoints = model.DataSetLoader.loadBankDataset();

        int[] kValues = {20, 21, 22, 23, 24}; // test different cluster counts

        // --- Mall Customers Dataset ---
        System.out.println("\n=== Mall Customers Dataset ===");
        KMeansExperiment mallExperiment = new KMeansExperiment(mallPoints);
        mallExperiment.runExperiment(kValues, new int[]{mallPoints.size()}); // full dataset only

        // --- Bank / Credit Card Customers Dataset ---
        System.out.println("\n=== Bank Customers Dataset ===");
        KMeansExperiment bankExperiment = new KMeansExperiment(bankPoints);
        bankExperiment.runExperiment(kValues, new int[]{bankPoints.size()}); // full dataset only
    }

}
