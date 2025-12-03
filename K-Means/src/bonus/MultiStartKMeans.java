package bonus;

import core.KMeansConfig;
import core.KMeansParallel;
import core.KMeansSequential;
import evaluation.SSECalculator;
import model.Cluster;
import model.Point;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;


public class MultiStartKMeans {

    private final KMeansConfig config;
    private final List<Point> points;
    private final int numRestarts;
    private final boolean useKMeansPlusPlus;
    private final boolean useParallel;

  
    public MultiStartKMeans(KMeansConfig config, List<Point> points, 
                           int numRestarts, boolean useKMeansPlusPlus, boolean useParallel) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be null or empty");
        }
        if (numRestarts <= 0) {
            throw new IllegalArgumentException("Number of restarts must be positive");
        }

        this.config = config;
        this.points = new ArrayList<>(points); // Create a copy
        this.numRestarts = numRestarts;
        this.useKMeansPlusPlus = useKMeansPlusPlus;
        this.useParallel = useParallel;
    }

    
    public MultiStartResult run() {
        List<Cluster> bestClusters = null;
        double bestSSE = Double.MAX_VALUE;
        int bestRestart = -1;
        long totalTime = 0;

        System.out.println("Running " + numRestarts + " restarts with " + 
                          (useParallel ? "parallel" : "sequential") + " implementation" +
                          (useKMeansPlusPlus ? " (k-means++ initialization)" : ""));

        for (int restart = 0; restart < numRestarts; restart++) {
            // Set different random seed for each restart to ensure variety
            RandomUtils.setSeed(System.currentTimeMillis() + restart);
            
            long startTime = System.currentTimeMillis();
            
            // Run K-means for this restart
            List<Cluster> clusters = runSingleRestart();
            
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            totalTime += elapsed;

            // Compute SSE for this clustering
            double sse = computeSSE(clusters);

            System.out.println("Restart " + (restart + 1) + "/" + numRestarts + 
                             ": SSE = " + String.format("%.4f", sse) + 
                             ", Time = " + elapsed + "ms");

            // Update best result if this is better
            if (sse < bestSSE) {
                bestSSE = sse;
                bestClusters = deepCopyClusters(clusters);
                bestRestart = restart + 1;
            }
        }

        System.out.println("\nBest result found at restart " + bestRestart + 
                         " with SSE = " + String.format("%.4f", bestSSE));
        System.out.println("Total time: " + totalTime + "ms, Average: " + 
                         (totalTime / numRestarts) + "ms per restart");

        return new MultiStartResult(bestClusters, bestSSE, bestRestart, totalTime);
    }
    
    private List<Cluster> runSingleRestart() {
        if (useParallel) {
            return runParallel();
        } else {
            return runSequential();
        }
    }

   
    private List<Cluster> runSequential() {
        KMeansSequential kmeans = new KMeansSequential(config, points);
        
        // Use k-means++ initialization if requested
        if (useKMeansPlusPlus) {
            List<Cluster> initialClusters = KMeansPlusPlusInitializer.initializeClusters(points, config.getK());
            kmeans.setInitialClusters(initialClusters);
        }
        
        kmeans.run();
        return kmeans.getClusters();
    }

    private List<Cluster> runParallel() {
        KMeansParallel kmeans = new KMeansParallel(config, points);
        
        // Use k-means++ initialization if requested
        if (useKMeansPlusPlus) {
            List<Cluster> initialClusters = KMeansPlusPlusInitializer.initializeClusters(points, config.getK());
            kmeans.setInitialClusters(initialClusters);
        }
        
        kmeans.run();
        return kmeans.getClusters();
    }

  
    private double computeSSE(List<Cluster> clusters) {
       return SSECalculator.computeSSE(clusters);
    }

   
    private List<Cluster> deepCopyClusters(List<Cluster> clusters) {
        List<Cluster> copy = new ArrayList<>();
        for (Cluster original : clusters) {
            Cluster clusterCopy = new Cluster(new Point(original.getCentroid().getCoordinates()));
            for (Point p : original.getPoints()) {
                clusterCopy.addPoint(new Point(p.getCoordinates()));
            }
            copy.add(clusterCopy);
        }
        return copy;
    }
    
}
