package core;

import model.Cluster;
import model.Point;
import util.DistanceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KMeansSequential {

    private final KMeansConfig config;
    private final List<Point> points;
    private final List<Cluster> clusters;

    public KMeansSequential(KMeansConfig config, List<Point> points) {
        this.config = config;
        this.points = points;
        this.clusters = new ArrayList<>();
    }

    /**
     * Run the K-Means algorithm sequentially
     */
    public void run() {
        initializeClusters();

        boolean converged = false;
        int iteration = 0;

        while (!converged && iteration < config.getMaxIterations()) {
            // 1. Clear previous points in each cluster
            clusters.forEach(Cluster::clearPoints);

            // 2. Assign each point to nearest cluster
            assignPointsToClusters();

            // 3. Recompute centroids and check convergence
            converged = recomputeCentroids();

            iteration++;
        }

        System.out.println("Sequential K-Means finished in " + iteration + " iterations");
    }

    /**
     * Initialize clusters with random points from dataset as centroids
     */
    private void initializeClusters() {
        clusters.clear();
        List<Point> shuffled = new ArrayList<>(points);
        Collections.shuffle(shuffled, new Random());

        for (int i = 0; i < config.getK(); i++) {
            clusters.add(new Cluster(shuffled.get(i)));
        }
    }

    /**
     * Assign each point to the nearest cluster centroid
     */
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

    /**
     * Recompute centroids and return true if all centroid movements are below tolerance
     */
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

    /**
     * Compute SSE (sum of squared errors) for current clustering
     */
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

    /**
     * Get clusters after running algorithm
     */
    public List<Cluster> getClusters() {
        return clusters;
    }
}
