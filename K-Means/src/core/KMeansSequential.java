package core;

import model.Cluster;
import model.Point;
import util.DistanceUtils;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KMeansSequential {

    private final KMeansConfig config;
    private final List<Point> points;
    private final List<Cluster> clusters;
    private int iterationsCompleted = 0;

    public KMeansSequential(KMeansConfig config, List<Point> points) {
        this.config = config;
        this.points = points;
        this.clusters = new ArrayList<>();
    }


    public void run() {
        // Only initialize if clusters are empty (allows for custom initialization)
        if (clusters.isEmpty()) {
            initializeClusters();
        }

        boolean converged = false;
        int iteration = 0;

        while (!converged && iteration < config.getMaxIterations()) {
            // 1. Clear previous points in each cluster
            clusters.forEach(Cluster::clearPoints);

            // 2. Assign each point to nearest cluster
            assignPointsToClusters();

            // 3. Handle empty clusters
            handleEmptyClusters();

            // 4. Recompute centroids and check convergence
            converged = recomputeCentroids();

            iteration++;
        }

        iterationsCompleted = iteration;
        System.out.println("Sequential K-Means finished in " + iteration + " iterations");
    }
    
    public int getIterationsCompleted() {
        return iterationsCompleted;
    }

    
    private void initializeClusters() {
        clusters.clear();
        List<Point> shuffled = new ArrayList<>(points);
        Collections.shuffle(shuffled, new Random());
        
        int k = Math.min(config.getK(), shuffled.size());

        for (int i = 0; i < k; i++) {
            clusters.add(new Cluster(shuffled.get(i)));
        }
    }

   
    public void setInitialClusters(List<Cluster> initialClusters) {
        int k = Math.min(config.getK(), initialClusters.size());

//        if (initialClusters == null || initialClusters.size() != config.getK()) {
//            throw new IllegalArgumentException("Initial clusters must be non-null and match k=" + config.getK());
//        }
        clusters.clear();
        // Create deep copies to avoid reference issues
        for (Cluster original : initialClusters) {
            Cluster copy = new Cluster(new Point(original.getCentroid().getCoordinates()));
            clusters.add(copy);
        }
    }

    
    private void assignPointsToClusters() {
        for (Point p : points) {
            Cluster nearest = null;
            double minDist = Double.MAX_VALUE;

            for (Cluster c : clusters) {
                double dist = DistanceUtils.distance(p, c.getCentroid());
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


    private void handleEmptyClusters() {
        // Find empty clusters
        List<Cluster> emptyClusters = new ArrayList<>();
        Cluster largestCluster = null;
        int maxSize = 0;

        for (Cluster c : clusters) {
            if (c.getPoints().isEmpty()) {
                emptyClusters.add(c);
            } else {
                // Track the largest cluster to steal a point from
                if (c.getPoints().size() > maxSize) {
                    maxSize = c.getPoints().size();
                    largestCluster = c;
                }
            }
        }

        // Reinitialize empty clusters
        for (Cluster emptyCluster : emptyClusters) {
            if (largestCluster != null && !largestCluster.getPoints().isEmpty()) {
                // Strategy 1: Move empty cluster to a random point from the largest cluster
                List<Point> largestPoints = largestCluster.getPoints();
                Point randomPoint = largestPoints.get(RandomUtils.nextInt(largestPoints.size()));
                emptyCluster.setCentroid(new Point(randomPoint.getCoordinates()));
            } else {
                // Strategy 2: If no non-empty clusters exist, move to a random point from dataset
                if (!points.isEmpty()) {
                    Point randomPoint = points.get(RandomUtils.nextInt(points.size()));
                    emptyCluster.setCentroid(new Point(randomPoint.getCoordinates()));
                }
            }
        }
    }

    private boolean recomputeCentroids() {
        boolean converged = true;

        for (Cluster c : clusters) {
            Point oldCentroid = c.getCentroid();
            c.recomputeCentroid();
            double movement = DistanceUtils.distance(oldCentroid, c.getCentroid());
            if (movement > config.getTolerance()) {
                converged = false;
            }
        }

        return converged;
    }

    public double computeSSE() {
        double sse = 0.0;
        for (Cluster c : clusters) {
            Point centroid = c.getCentroid();
            for (Point p : c.getPoints()) {
                double dist = DistanceUtils.distance(p, centroid);
                sse += dist * dist;
            }
        }
        return sse;
    }

    
    public List<Cluster> getClusters() {
        return clusters;
    }
}
