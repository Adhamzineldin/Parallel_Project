package gui;

import core.KMeansAssignTask;
import core.KMeansConfig;
import core.KMeansParallel;
import core.KMeansSequential;
import core.RecomputeCentroidsTask;
import model.Cluster;
import model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for animating centroid movement during K-Means iterations
 * Captures centroid positions at each iteration for visualization
 */
public class CentroidAnimationController {
    
    public interface AnimationListener {
        void onIterationComplete(List<Cluster> clusters, int iteration, double sse);
        void onAnimationComplete(List<Cluster> finalClusters, double finalSSE, long totalTime, int iterations);
    }
    
    private final KMeansConfig config;
    private final List<Point> points;
    private final boolean useParallel;
    private final boolean useKMeansPlusPlus;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int lastIterationCount = 0;
    
    public CentroidAnimationController(KMeansConfig config, List<Point> points, 
                                      boolean useParallel, boolean useKMeansPlusPlus) {
        this.config = config;
        this.points = new ArrayList<>(points);
        this.useParallel = useParallel;
        this.useKMeansPlusPlus = useKMeansPlusPlus;
    }
    
    public void runWithAnimation(AnimationListener listener) {
        if (isRunning.get()) {
            return;
        }
        
        isRunning.set(true);
        
        try {
            List<Cluster> clusters;
            long startTime = System.currentTimeMillis();
            
            if (useParallel) {
                clusters = runParallelWithAnimation(listener);
            } else {
                clusters = runSequentialWithAnimation(listener);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            double finalSSE = computeSSE(clusters);
            int finalIterations = lastIterationCount;
            
            if (listener != null) {
                listener.onAnimationComplete(clusters, finalSSE, totalTime, finalIterations);
            }
            
        } finally {
            isRunning.set(false);
        }
    }
    
    private List<Cluster> runSequentialWithAnimation(AnimationListener listener) {
        KMeansSequential kmeans = new KMeansSequential(config, points);
        
        if (useKMeansPlusPlus) {
            List<Cluster> initialClusters = bonus.KMeansPlusPlusInitializer.initializeClusters(points, config.getK());
            kmeans.setInitialClusters(initialClusters);
        }
        
        // Get initial clusters
        List<Cluster> clusters = deepCopyClusters(kmeans.getClusters());
        if (listener != null) {
            listener.onIterationComplete(clusters, 0, computeSSE(clusters));
        }
        
        boolean converged = false;
        int iteration = 0;
        
        while (!converged && iteration < config.getMaxIterations()) {
            // Clear points
            for (Cluster c : clusters) {
                c.clearPoints();
            }
            
            // Assign points
            assignPointsToClusters(clusters);
            
            // Recompute centroids
            converged = recomputeCentroids(clusters);
            
            iteration++;
            
            // Notify listener and wait for UI to update
            if (listener != null) {
                List<Cluster> currentClusters = deepCopyClusters(clusters);
                listener.onIterationComplete(currentClusters, iteration, computeSSE(currentClusters));
                
                // Wait for UI to update before continuing
                try {
                    Thread.sleep(300); // 300ms delay for better visibility
                    // Give Swing time to process the update
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // Still delay even without listener
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return clusters;
    }
    
    private List<Cluster> runParallelWithAnimation(AnimationListener listener) {
        KMeansParallel kmeans = new KMeansParallel(config, points);
        
        if (useKMeansPlusPlus) {
            List<Cluster> initialClusters = bonus.KMeansPlusPlusInitializer.initializeClusters(points, config.getK());
            kmeans.setInitialClusters(initialClusters);
        }
        
        // Get initial clusters - we need to access them, but KMeansParallel doesn't expose them before run()
        // So we'll create our own copy
        List<Cluster> clusters = new ArrayList<>();
        if (useKMeansPlusPlus) {
            clusters = bonus.KMeansPlusPlusInitializer.initializeClusters(points, config.getK());
        } else {
            // Initialize randomly
            java.util.Collections.shuffle(new ArrayList<>(points), new java.util.Random());
            for (int i = 0; i < config.getK(); i++) {
                clusters.add(new Cluster(new Point(points.get(i).getCoordinates())));
            }
        }
        
        if (listener != null) {
            listener.onIterationComplete(deepCopyClusters(clusters), 0, computeSSE(clusters));
        }
        
        boolean converged = false;
        int iteration = 0;
        java.util.concurrent.ForkJoinPool pool = java.util.concurrent.ForkJoinPool.commonPool();
        
        while (!converged && iteration < config.getMaxIterations()) {
            // Clear points
            for (Cluster c : clusters) {
                c.clearPoints();
            }
            
            // Parallel assignment
            pool.invoke(new KMeansAssignTask(points, 0, points.size(), clusters));
            
            // Parallel recomputation
            converged = pool.invoke(new RecomputeCentroidsTask(clusters, 0, clusters.size(), config));
            
            iteration++;
            lastIterationCount = iteration;
            
            // Notify listener and wait for UI to update
            if (listener != null) {
                List<Cluster> currentClusters = deepCopyClusters(clusters);
                listener.onIterationComplete(currentClusters, iteration, computeSSE(currentClusters));
                
                // Wait for UI to update before continuing
                try {
                    Thread.sleep(300); // 300ms delay for better visibility
                    // Give Swing time to process the update
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // Still delay even without listener
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return clusters;
    }
    
    private void assignPointsToClusters(List<Cluster> clusters) {
        for (Point p : points) {
            Cluster nearest = null;
            double minDist = Double.MAX_VALUE;
            
            for (Cluster c : clusters) {
                double dist = p.distanceTo(c.getCentroid());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = c;
                }
            }
            
            if (nearest != null) {
                nearest.addPoint(p);
            }
        }
    }
    
    private boolean recomputeCentroids(List<Cluster> clusters) {
        boolean converged = true;
        
        for (Cluster c : clusters) {
            Point oldCentroid = c.getCentroid();
            c.recomputeCentroid();
            double movement = oldCentroid.distanceTo(c.getCentroid());
            if (movement > config.getTolerance()) {
                converged = false;
            }
        }
        
        return converged;
    }
    
    private double computeSSE(List<Cluster> clusters) {
        return evaluation.SSECalculator.computeSSE(clusters);
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
    
    public void stop() {
        isRunning.set(false);
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
    
    private int getLastIterationCount() {
        return lastIterationCount;
    }
}
