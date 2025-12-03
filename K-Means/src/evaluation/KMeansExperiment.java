package evaluation;

import bonus.MultiStartKMeans;
import bonus.MultiStartResult;
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
            List<Point> currentDataset = new ArrayList<>(dataset.subList(0, Math.min(size, dataset.size())));

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

    /**
     * Run experiments with MultiStart K-Means (bonus feature)
     * Tests multiple random restarts with optional k-means++ initialization
     * @param kValues array of K (number of clusters) to test
     * @param subsetSizes array of dataset sizes to test
     * @param numRestarts number of random restarts to perform
     * @param useKMeansPlusPlus whether to use k-means++ initialization
     * @param useParallel whether to use parallel implementation
     */
    public void runMultiStartExperiment(int[] kValues, int[] subsetSizes, 
                                       int numRestarts, boolean useKMeansPlusPlus, boolean useParallel) {
        String initType = useKMeansPlusPlus ? "k-means++" : "random";
        String implType = useParallel ? "Parallel" : "Sequential";
        System.out.println("\n=== MultiStart K-Means Experiment (" + implType + ", " + initType + " initialization) ===");
        System.out.println("Dataset size | K | Best SSE | Total Runtime(ms) | Avg Runtime(ms) | Best Restart");

        for (int size : subsetSizes) {
            // Take a subset of the dataset if needed
            List<Point> currentDataset = new ArrayList<>(dataset.subList(0, Math.min(size, dataset.size())));

            for (int k : kValues) {
                KMeansConfig config = new KMeansConfig(k, 1000, 0.00000000001);

                // Run MultiStart K-Means
                MultiStartKMeans multiStart = new MultiStartKMeans(config, currentDataset, 
                                                                  numRestarts, useKMeansPlusPlus, useParallel);
                MultiStartResult result = multiStart.run();

                // Print results in table row
                System.out.printf("%12d | %2d | %9.2f | %16d | %15d | %12d%n",
                        currentDataset.size(), k, result.getSSE(), result.getTotalTime(),
                        result.getTotalTime() / numRestarts, result.getBestRestart());
            }
        }
    }

    /**
     * Run comprehensive experiments including bonus features
     * Compares standard implementations with MultiStart variants
     * @param kValues array of K (number of clusters) to test
     * @param subsetSizes array of dataset sizes to test
     * @param numRestarts number of random restarts for MultiStart
     */
    public void runComprehensiveExperiment(int[] kValues, int[] subsetSizes, int numRestarts) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPREHENSIVE K-MEANS EXPERIMENT");
        System.out.println("=".repeat(80));

        // 1. Standard Sequential and Parallel
        System.out.println("\n--- Standard Implementations ---");
        runExperiment(kValues, subsetSizes);

        // 2. MultiStart Sequential with Random Initialization
        runMultiStartExperiment(kValues, subsetSizes, numRestarts, false, false);

        // 3. MultiStart Sequential with k-means++ Initialization
        runMultiStartExperiment(kValues, subsetSizes, numRestarts, true, false);

        // 4. MultiStart Parallel with Random Initialization
        runMultiStartExperiment(kValues, subsetSizes, numRestarts, false, true);

        // 5. MultiStart Parallel with k-means++ Initialization
        runMultiStartExperiment(kValues, subsetSizes, numRestarts, true, true);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("EXPERIMENT COMPLETE");
        System.out.println("=".repeat(80));
    }

    // Optional: main for quick testing
    public static void main(String[] args) {
        // Load both datasets
        List<Point> mallPoints = model.DataSetLoader.loadMallDataset();
        List<Point> bankPoints = model.DataSetLoader.loadBankDataset();

        int[] kValues = {20, 21, 22, 23, 24}; // test different cluster counts
        int numRestarts = 5; // number of restarts for MultiStart experiments

        // --- Mall Customers Dataset ---
        System.out.println("\n=== Mall Customers Dataset ===");
        KMeansExperiment mallExperiment = new KMeansExperiment(mallPoints);
        
        // Run standard experiment
        mallExperiment.runExperiment(kValues, new int[]{mallPoints.size()});
        
        // Run comprehensive experiment with bonus features
        System.out.println("\n--- Running Comprehensive Experiment with Bonus Features ---");
        mallExperiment.runComprehensiveExperiment(kValues, new int[]{mallPoints.size()}, numRestarts);

        // --- Bank / Credit Card Customers Dataset ---
        System.out.println("\n=== Bank Customers Dataset ===");
        KMeansExperiment bankExperiment = new KMeansExperiment(bankPoints);
        
        // Run standard experiment
        bankExperiment.runExperiment(kValues, new int[]{bankPoints.size()});
        
        // Run comprehensive experiment with bonus features
        System.out.println("\n--- Running Comprehensive Experiment with Bonus Features ---");
        bankExperiment.runComprehensiveExperiment(kValues, new int[]{bankPoints.size()}, numRestarts);
    }

}
